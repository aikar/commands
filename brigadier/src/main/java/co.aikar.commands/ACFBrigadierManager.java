package co.aikar.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
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
        registerArgument(Float.class, FloatArgumentType.floatArg());
        registerArgument(double.class, DoubleArgumentType.doubleArg());
        registerArgument(Double.class, DoubleArgumentType.doubleArg());
        registerArgument(boolean.class, BoolArgumentType.bool());
        registerArgument(Boolean.class, BoolArgumentType.bool());
        registerArgument(int.class, IntegerArgumentType.integer());
        registerArgument(Integer.class, IntegerArgumentType.integer());
        registerArgument(long.class, LongArgumentType.longArg());
        registerArgument(Long.class, LongArgumentType.longArg());
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
        LiteralArgumentBuilder<S> rootBuilder = LiteralArgumentBuilder.<S>literal(root.getLiteral())
                .requires(sender -> permCheckerRoot.test(acfCommand, sender));

        // if we have no subcommands, this command is actually executable
        if (acfCommand.getSubCommands().size() == 0) {
            rootBuilder.executes(executor);
        }

        root = rootBuilder.build();

        for (Map.Entry<String, RegisteredCommand> subCommand : acfCommand.getSubCommands().entries()) {
            if (subCommand.getKey().startsWith("__") || (!subCommand.getKey().equals("help") && subCommand.getValue().prefSubCommand.equals("help"))) {
                // don't register stuff like __catchunknown and don't help command aliases
                continue;
            }

            // handle sub sub commands
            String commandName = subCommand.getKey();
            CommandNode<S> currentParent = root;
            if (commandName.contains(" ")) {
                String[] split = ACFPatterns.SPACE.split(commandName);
                for (int i = 0; i < split.length - 1; i++) {
                    if (currentParent.getChild(split[i]) == null) {
                        LiteralCommandNode<S> sub = LiteralArgumentBuilder.<S>literal(split[i])
                                .requires(sender -> permCheckerSub.test(subCommand.getValue(), sender)).build();
                        currentParent.addChild(sub);
                        currentParent = sub;
                    } else {
                        currentParent = currentParent.getChild(split[i]);
                    }
                }
                commandName = split[split.length - 1];
            }

            CommandNode<S> subCommandNode = currentParent.getChild(commandName);
            if (subCommandNode == null) {
                LiteralArgumentBuilder<S> argumentBuilder = LiteralArgumentBuilder.<S>literal(commandName)
                        .requires(sender -> permCheckerSub.test(subCommand.getValue(), sender));

                // if we have no params, this command is actually executable
                if (subCommand.getValue().consumeInputResolvers == 0) {
                    argumentBuilder.executes(executor);
                }
                subCommandNode = argumentBuilder.build();
            }

            CommandNode<S> paramNode = subCommandNode;
            CommandParameter[] parameters = subCommand.getValue().parameters;
            for (int i = 0; i < parameters.length; i++) {
                CommandParameter param = parameters[i];
                if (manager.isCommandIssuer(param.getType()) && !param.getFlags().containsKey("other")) {
                    continue;
                }
                RequiredArgumentBuilder<S, Object> builder = RequiredArgumentBuilder
                        .<S, Object>argument(param.getName(), getArgumentTypeByClazz(param.getType()))
                        .suggests(suggestionProvider)
                        .requires(sender -> permCheckerSub.test(subCommand.getValue(), sender));

                // last param -> execute
                if (i == parameters.length - 1) {
                    builder.executes(executor);
                }
                // current param is optional or next param is optional -> execute
                if (param.isOptional() || i < parameters.length - 1 && parameters[i + 1].isOptional()) {
                    builder.executes(executor);
                }

                CommandNode<S> subSubCommand = builder.build();
                paramNode.addChild(subSubCommand);
                paramNode = subSubCommand;
            }
            currentParent.addChild(subCommandNode);
        }

        return root;
    }
}
