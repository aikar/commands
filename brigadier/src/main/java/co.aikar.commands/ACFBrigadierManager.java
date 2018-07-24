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
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import java.util.HashMap;
import java.util.Map;

@Deprecated
@UnstableAPI
public class ACFBrigadierManager<S> {

    private CommandManager<?, ?, ?, ?, ?, ?> manager;
    private CommandDispatcher<S> dispatcher;

    private Map<Class<?>, ArgumentType<?>> arguments = new HashMap<>();

    public ACFBrigadierManager(CommandManager<?, ?, ?, ?, ?, ?> manager, CommandDispatcher<S> dispatcher) {
        this.manager = manager;
        this.dispatcher = dispatcher;

        manager.verifyUnstableAPI("brigadier");

        //TODO FIGURE OUT WHERE SPIGOT IS OVERRIDING OUR STUFF!!!

        // TODO support stuff like min max via brigadier?
        arguments.put(String.class, StringArgumentType.string());
        arguments.put(float.class, FloatArgumentType.floatArg());
        arguments.put(double.class, DoubleArgumentType.doubleArg());
        arguments.put(boolean.class, BoolArgumentType.bool());
        arguments.put(int.class, IntegerArgumentType.integer());
    }

    public void register(BaseCommand command) {
        System.out.println("* registering command " + command.commandName);
        CommandNode<S> baseCmd = LiteralArgumentBuilder.<S>literal(command.commandName).build();
        for (Map.Entry<String, RegisteredCommand> entry : command.getSubCommands().entries()) {
            System.out.println("* * registering subcommand " + entry.getKey());
            LiteralCommandNode<S> subCommandNode = LiteralArgumentBuilder.<S>literal(entry.getKey()).build();
            CommandNode<S> paramNode = subCommandNode;
            for (CommandParameter param : entry.getValue().parameters) {
                if (manager.isCommandIssuer(param.getType())) continue;
                System.out.println("* * * registering param " + param.getName() + " of type " + param.getType().getSimpleName());
                paramNode.addChild(paramNode = RequiredArgumentBuilder.<S, Object>argument(param.getName(), getArgumentTypeByClazz(param.getType())).build());
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
}
