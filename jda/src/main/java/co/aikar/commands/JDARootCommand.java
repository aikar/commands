package co.aikar.commands;

import co.aikar.util.MapSet;

import java.util.ArrayList;
import java.util.List;

public class JDARootCommand implements RootCommand {

    private final String name;
    boolean isRegistered = false;
    private JDACommandManager manager;
    private BaseCommand defCommand;
    private MapSet<String, RegisteredCommand> subCommands = new MapSet<>();
    private List<BaseCommand> children = new ArrayList<>();

    JDARootCommand(JDACommandManager manager, String name) {
        this.manager = manager;
        this.name = name;
    }

    @Override

    public void addChild(BaseCommand command) {
        if (this.defCommand == null || command.subCommands.has(BaseCommand.DEFAULT)) {
            this.defCommand = command;
        }
        addChildShared(this.children, this.subCommands, command);
    }

    @Override
    public CommandManager getManager() {
        return this.manager;
    }

    @Override
    public MapSet<String, RegisteredCommand> getSubCommands() {
        return this.subCommands;
    }

    @Override
    public List<BaseCommand> getChildren() {
        return this.children;
    }

    @Override
    public String getCommandName() {
        return this.name;
    }

    @Override
    public BaseCommand getDefCommand() {
        return defCommand;
    }

}
