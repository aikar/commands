package co.aikar.commands;

import com.mojang.brigadier.Command;
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
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * Handles registering of commands into brigadier
 *
 * @param <S>
 * @author MiniDigger
 * @deprecated Unstable API
 */
@Deprecated
@UnstableAPI
public class ACFBrigadierManager<S> {

    protected final CommandManager<?, ?, ?, ?, ?, ?> manager;

    private final Map<Class<?>, ArgumentType<?>> arguments = new HashMap<>();

    /**
     * Constructs a new brigadier manager, utilizing the currently active command manager
     *
     * @param manager
     */
    ACFBrigadierManager(CommandManager<?, ?, ?, ?, ?, ?> manager) {
        manager.verifyUnstableAPI("brigadier");

        this.manager = manager;

        manager.getCommandContexts().contextMap.forEach((type, contextResolver) -> registerArgument(type, StringArgumentType.string()));

        // TODO support stuff like min max via brigadier?
        registerArgument(String.class, StringArgumentType.string());
        registerArgument(float.class, FloatArgumentType.floatArg());
        registerArgument(double.class, DoubleArgumentType.doubleArg());
        registerArgument(boolean.class, BoolArgumentType.bool());
        registerArgument(int.class, IntegerArgumentType.integer());
    }

    <T> void registerArgument(Class<T> clazz, ArgumentType<?> type) {
        arguments.put(clazz, type);
    }

    private ArgumentType<Object> getArgumentTypeByClazz(Class<?> clazz) {
        //noinspection unchecked
        return (ArgumentType<Object>) arguments.getOrDefault(clazz, StringArgumentType.string());
    }

    /**
     * Registers the given RootCommand into the given brigadir command node, utilizing the provided suggestion provider, executor and permission predicate.<br>
     * <p>
     * It recreates the root command node!
     */
    LiteralCommandNode<S> register(RootCommand acfCommand,
                                   LiteralCommandNode<S> root,
                                   SuggestionProvider<S> suggestionProvider,
                                   Command<S> executor,
                                   BiPredicate<RootCommand, S> permCheckerRoot,
                                   BiPredicate<RegisteredCommand, S> permCheckerSub) {
        // recreate root to get rid of bukkits default arg
        root = LiteralArgumentBuilder.<S>literal(root.getLiteral())
                .requires(sender -> permCheckerRoot.test(acfCommand, sender))
                .executes(executor)
                .build();

        for (Map.Entry<String, RegisteredCommand> subCommand : acfCommand.getSubCommands().entries()) {
            if (subCommand.getKey().startsWith("__") || (!subCommand.getKey().equals("help") && subCommand.getValue().prefSubCommand.equals("help"))) {
                // don't register stuff like __catchunknown and don't help command aliases
                continue;
            }
            LiteralCommandNode<S> subCommandNode = LiteralArgumentBuilder.<S>literal(subCommand.getKey())
                    .executes(executor)
                    .requires(sender -> permCheckerSub.test(subCommand.getValue(), sender))
                    .build();
            CommandNode<S> paramNode = subCommandNode;
            for (CommandParameter param : subCommand.getValue().parameters) {
                if (manager.isCommandIssuer(param.getType()) && !param.getFlags().containsKey("other")) {
                    continue;
                }
                CommandNode<S> subSubCommand = RequiredArgumentBuilder
                        .<S, Object>argument(param.getName(), getArgumentTypeByClazz(param.getType()))
                        .suggests(suggestionProvider)
                        .executes(executor)
                        .requires(sender -> permCheckerSub.test(subCommand.getValue(), sender))
                        .build();
                paramNode.addChild(subSubCommand);
                paramNode = subSubCommand;
            }
            root.addChild(subCommandNode);
        }

        return root;
    }
}
