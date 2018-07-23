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

import java.util.HashMap;
import java.util.Map;

@Deprecated
@UnstableAPI
public class ACFBrigadierManager<S> {

    private CommandManager<?,?,?,?,?,?> manager;
    private CommandDispatcher<S> dispatcher;

    private Map<Class<?>, ArgumentType> arguments = new HashMap<>();

    public ACFBrigadierManager(CommandManager<?,?,?,?,?,?>  manager, CommandDispatcher<S> dispatcher) {
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
        for (Map.Entry<String, RegisteredCommand> entry : command.getSubCommands().entries()) {
            LiteralArgumentBuilder<S> builder = LiteralArgumentBuilder.literal(entry.getKey());
            for (CommandParameter param : entry.getValue().parameters) {
                if(manager.isCommandIssuer(param.getType())) continue;
                builder = builder.then(RequiredArgumentBuilder.argument(param.getName(), getArgumentTypeByCall(param.getType())));
            }
            dispatcher.register(builder);
            System.out.println(command.commandName + " registered " + entry.getKey());
        }
    }

    private ArgumentType<?> getArgumentTypeByCall(Class<?> clazz){
        return arguments.getOrDefault(clazz, StringArgumentType.string());
    }
}
