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

        // TODO support stuff like min max via brigadier?
        registerArgument(String.class, StringArgumentType.word());
        registerArgument(float.class, FloatArgumentType.floatArg());
        registerArgument(Float.class, FloatArgumentType.floatArg());
        registerArgument(double.class, DoubleArgumentType.doubleArg());
        registerArgument(Double.class, DoubleArgumentType.doubleArg());
        registerArgument(boolean.class, BoolArgumentType.bool());
        registerArgument(Boolean.class, BoolArgumentType.bool());
        registerArgument(int.class, IntegerArgumentType.integer());
        registerArgument(Integer.class, IntegerArgumentType.integer());
        // We use integer for long due to Bungee bug, plus should really be considered same on client
        registerArgument(long.class, IntegerArgumentType.integer());
        registerArgument(Long.class, IntegerArgumentType.integer());
    }

    <T> void registerArgument(Class<T> clazz, ArgumentType<?> type) {
        arguments.put(clazz, type);
    }

    ArgumentType<Object> getArgumentTypeByClazz(CommandParameter param) {
        if (param.consumesRest) {
            //noinspection unchecked
            return (ArgumentType<Object>) (ArgumentType<?>) StringArgumentType.greedyString();
        }
        //noinspection unchecked
        return (ArgumentType<Object>) arguments.getOrDefault(param.getType(), StringArgumentType.string());
    }

    /**
     * Registers the given RootCommand into the given brigadir command node, utilizing the provided suggestion provider, executor and permission predicate.<br>
     * <p>
     * It recreates the root command node!
     */
    LiteralCommandNode<S> register(RootCommand rootCommand,
                                   LiteralCommandNode<S> root,
                                   SuggestionProvider<S> suggestionProvider,
                                   Command<S> executor,
                                   BiPredicate<RootCommand, S> permCheckerRoot,
                                   BiPredicate<RegisteredCommand, S> permCheckerSub) {
        // recreate root to get rid of bukkits default arg
        LiteralArgumentBuilder<S> rootBuilder = LiteralArgumentBuilder.<S>literal(root.getLiteral())
                .requires(sender -> permCheckerRoot.test(rootCommand, sender));

        root = rootBuilder.build();
        boolean isForwardingCommand = rootCommand.getDefCommand() instanceof ForwardingCommand;

        for (Map.Entry<String, RegisteredCommand> subCommand : rootCommand.getSubCommands().entries()) {
            if ((BaseCommand.isSpecialSubcommand(subCommand.getKey()) && !isForwardingCommand) || (!subCommand.getKey().equals("help") && subCommand.getValue().prefSubCommand.equals("help"))) {
                // don't register stuff like __catchunknown and don't help command aliases
                continue;
            }

            // handle sub sub commands
            String commandName = subCommand.getKey();
            CommandNode<S> currentParent = root;
            CommandNode<S> subCommandNode;
            Predicate<S> subPermChecker = sender -> permCheckerSub.test(subCommand.getValue(), sender);
            if (!isForwardingCommand) {
                if (commandName.contains(" ")) {
                    String[] split = ACFPatterns.SPACE.split(commandName);
                    for (int i = 0; i < split.length - 1; i++) {
                        if (currentParent.getChild(split[i]) == null) {
                            LiteralCommandNode<S> sub = LiteralArgumentBuilder.<S>literal(split[i])
                                    .requires(subPermChecker).build();
                            currentParent.addChild(sub);
                            currentParent = sub;
                        } else {
                            currentParent = currentParent.getChild(split[i]);
                        }
                    }
                    commandName = split[split.length - 1];
                }

                subCommandNode = currentParent.getChild(commandName);
                if (subCommandNode == null) {
                    LiteralArgumentBuilder<S> argumentBuilder = LiteralArgumentBuilder.<S>literal(commandName)
                            .requires(subPermChecker);

                    // if we have no params, this command is actually executable
                    if (subCommand.getValue().consumeInputResolvers == 0) {
                        argumentBuilder.executes(executor);
                    }
                    subCommandNode = argumentBuilder.build();
                }
            } else {
                subCommandNode = root;
            }

            CommandNode<S> paramNode = subCommandNode;
            CommandParameter[] parameters = subCommand.getValue().parameters;
            for (int i = 0; i < parameters.length; i++) {
                CommandParameter param = parameters[i];
                CommandParameter nextParam = param.getNextParam();
                if (param.isCommandIssuer() || (param.canExecuteWithoutInput() && nextParam != null && !nextParam.canExecuteWithoutInput())) {
                    continue;
                }
                RequiredArgumentBuilder<S, Object> builder = RequiredArgumentBuilder
                        .<S, Object>argument(param.getName(), getArgumentTypeByClazz(param))
                        .suggests(suggestionProvider)
                        .requires(sender -> permCheckerSub.test(subCommand.getValue(), sender));

                if (nextParam != null && nextParam.canExecuteWithoutInput()) {
                    builder.executes(executor);
                }

                CommandNode<S> subSubCommand = builder.build();
                paramNode.addChild(subSubCommand);
                paramNode = subSubCommand;
            }

            if (!isForwardingCommand) {
                currentParent.addChild(subCommandNode);
            }
        }

        return root;
    }

}
