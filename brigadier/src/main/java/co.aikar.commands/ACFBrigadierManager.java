package co.aikar.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Handles registering of commands into brigadier
 *
 * @param <S>
 * @author MiniDigger
 * @deprecated Unstable API
 */
@Deprecated
@UnstableAPI
public abstract class ACFBrigadierManager<S> implements SuggestionProvider<S> {

    private CommandManager<?, ?, ?, ?, ?, ?> manager;
    private CommandDispatcher<S> dispatcher;

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
    }

    public <T> void registerArgument(Class<T> clazz, ArgumentType<T> type) {
        arguments.put(clazz, type);
    }

    public void register(BaseCommand command) {
        System.out.println("* registering command " + command.commandName);
        CommandNode<S> baseCmd = LiteralArgumentBuilder.<S>literal(command.commandName).build();
        for (Map.Entry<String, RegisteredCommand> entry : command.getSubCommands().entries()) {
            if (entry.getKey().startsWith("__")) {
                // don't register stuff like __catchunknown
                continue;
            }
            System.out.println("* * registering subcommand " + entry.getKey());
            LiteralCommandNode<S> subCommandNode = LiteralArgumentBuilder.<S>literal(entry.getKey()).build();
            CommandNode<S> paramNode = subCommandNode;
            for (CommandParameter param : entry.getValue().parameters) {
                if (manager.isCommandIssuer(param.getType()) && !param.getFlags().containsKey("other")) {
                    continue;
                }
                paramNode.addChild(paramNode = RequiredArgumentBuilder.<S, Object>argument(param.getName(), getArgumentTypeByClazz(param.getType())).suggests(this).build());
                System.out.println("* * * registering param " + param.getName() + " of type " + param.getType().getSimpleName() + " with argument type " + ((ArgumentCommandNode) paramNode).getType().getClass().
                        getSimpleName());
            }
            baseCmd.addChild(subCommandNode);
            System.out.println("* * registered subcommand " + entry.getKey());
        }

        dispatcher.getRoot().addChild(baseCmd);

        System.out.println("* registered " + command.commandName);
    }

    private ArgumentType<Object> getArgumentTypeByClazz(Class<?> clazz) {
        //noinspection unchecked
        return (ArgumentType<Object>) arguments.getOrDefault(clazz, StringArgumentType.string());
    }

    @Override
    public abstract CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder) throws CommandSyntaxException;
}
