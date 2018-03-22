package co.aikar.commands;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import java.util.ArrayList;
import java.util.List;

public class JDARootCommand implements RootCommand {

    private JDACommandManager manager;
    private final String name;
    private BaseCommand defCommand;
    private SetMultimap<String, RegisteredCommand> subCommands = HashMultimap.create();
    private List<BaseCommand> children = new ArrayList<>();
    boolean isRegistered = false;

    JDARootCommand(JDACommandManager manager, String name) {
        this.manager = manager;
        this.name = name;
    }

    @Override

    public void addChild(BaseCommand command) {
        if (this.defCommand == null || !command.subCommands.get(BaseCommand.DEFAULT).isEmpty()) {
            this.defCommand = command;
        }
        addChildShared(this.children, this.subCommands, command);
    }

    @Override
    public CommandManager getManager() {
        return this.manager;
    }

    @Override
    public SetMultimap<String, RegisteredCommand> getSubCommands() {
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
