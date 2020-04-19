package co.aikar.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Handles registering of commands into brigadier
 *
 * @param <S>
 * @author MiniDigger
 * @deprecated Unstable API
 */
@Deprecated
@UnstableAPI
public abstract class ACFBrigadierManager<S> implements SuggestionProvider<S>, Command<S>, Predicate<S> {

    protected CommandManager<?, ?, ?, ?, ?, ?> manager;
    protected CommandDispatcher<S> dispatcher;

    private Map<Class<?>, ArgumentType<?>> arguments = new HashMap<>();

    /**
     * Constructs a new brigadier manager, utilizing the currently active command manager and an brigadier provider.
     *
     * @param manager
     * @param provider
     */
    public ACFBrigadierManager(CommandManager<?, ?, ?, ?, ?, ?> manager, ACFBrigadierProvider provider) {
        this.manager = manager;
        //noinspection unchecked
        this.dispatcher = (CommandDispatcher<S>) provider.getCommandDispatcher();

        manager.verifyUnstableAPI("brigadier");

        // TODO support stuff like min max via brigadier?
        registerArgument(String.class, StringArgumentType.string());
        registerArgument(float.class, FloatArgumentType.floatArg());
        registerArgument(double.class, DoubleArgumentType.doubleArg());
        registerArgument(boolean.class, BoolArgumentType.bool());
        registerArgument(int.class, IntegerArgumentType.integer());

        manager.getCommandContexts().contextMap.forEach((type, contextResolver) -> registerArgument(type, StringArgumentType.string()));
    }

    public <T> void registerArgument(Class<T> clazz, ArgumentType<?> type) {
        arguments.put(clazz, type);
    }

    public void register(BaseCommand command) {
        registerACF(command);
        registerBrigadier(command);
    }

    protected abstract void registerACF(BaseCommand command);

    protected void registerBrigadier(BaseCommand command) {
        CommandNode<S> baseCmd = LiteralArgumentBuilder.<S>literal(command.commandName).build();
        Set<RegisteredCommand> seen = new HashSet<>();
        for (Map.Entry<String, RegisteredCommand> entry : command.getSubCommands().entries()) {
            if (entry.getKey().startsWith("__") || (!entry.getKey().equals("help") && entry.getValue().prefSubCommand.equals("help"))) {
                // don't register stuff like __catchunknown and don't help command aliases
                continue;
            }
            LiteralCommandNode<S> subCommandNode = LiteralArgumentBuilder.<S>literal(entry.getKey()).build();
            CommandNode<S> paramNode = subCommandNode;
            for (CommandParameter param : entry.getValue().parameters) {
                if (manager.isCommandIssuer(param.getType()) && !param.getFlags().containsKey("other")) {
                    continue;
                }
                paramNode.addChild(paramNode = RequiredArgumentBuilder.<S, Object>argument(param.getName(), getArgumentTypeByClazz(param.getType())).suggests(this).executes(this).build());
            }
            baseCmd.addChild(subCommandNode);
            seen.add(entry.getValue());
        }

        dispatcher.getRoot().addChild(baseCmd);
    }

    private ArgumentType<Object> getArgumentTypeByClazz(Class<?> clazz) {
        //noinspection unchecked
        return (ArgumentType<Object>) arguments.getOrDefault(clazz, StringArgumentType.string());
    }
}
