package co.aikar.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import java.util.Map;

@Deprecated
@UnstableAPI
public class ACFBrigadierManager<S> {

    private CommandManager manager;
    private CommandDispatcher<S> dispatcher;

    public ACFBrigadierManager(CommandManager manager, CommandDispatcher<S> dispatcher) {
        this.manager = manager;
        this.dispatcher = dispatcher;

        manager.verifyUnstableAPI("brigadier");
    }

    public void register(BaseCommand command) {
        for (Map.Entry<String, RegisteredCommand> entry : command.getSubCommands().entries()) {
            LiteralArgumentBuilder<S> builder = LiteralArgumentBuilder.literal(command.commandName + " " + entry.getValue().command);
            for (CommandParameter param : entry.getValue().parameters) {
                RequiredArgumentBuilder<S, ?> arg = RequiredArgumentBuilder.argument(param.getName(), getArgumentTypeByCall(param.getType()));
                builder = builder.then(arg.build());
            }
            dispatcher.register(builder);
        }
    }

    private ArgumentType<?> getArgumentTypeByCall(Class<?> clazz){
        return StringArgumentType.string(); //TODO maybe a map so that plugins can register their own types?
    }
}
