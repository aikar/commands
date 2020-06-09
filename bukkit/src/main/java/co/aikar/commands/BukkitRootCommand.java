/*
 * Copyright (c) 2016-2017 Daniel Ennis (Aikar) - MIT License
 *
 *  Permission is hereby granted, free of charge, to any person obtaining
 *  a copy of this software and associated documentation files (the
 *  "Software"), to deal in the Software without restriction, including
 *  without limitation the rights to use, copy, modify, merge, publish,
 *  distribute, sublicense, and/or sell copies of the Software, and to
 *  permit persons to whom the Software is furnished to do so, subject to
 *  the following conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 *  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 *  OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package co.aikar.commands;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class BukkitRootCommand extends Command implements RootCommand, PluginIdentifiableCommand {

    private final BukkitCommandManager manager;
    private final String name;
    private BaseCommand defCommand;
    private SetMultimap<String, RegisteredCommand> subCommands = HashMultimap.create();
    private List<BaseCommand> children = new ArrayList<>();
    boolean isRegistered = false;

    BukkitRootCommand(BukkitCommandManager manager, String name) {
        super(name);
        this.manager = manager;
        this.name = name;
    }

    @Override
    public String getDescription() {
        RegisteredCommand command = getDefaultRegisteredCommand();

        if (command != null && !command.getHelpText().isEmpty()) {
            return command.getHelpText();
        }
        if (command != null && command.scope.description != null) {
            return command.scope.description;
        }
        if (defCommand.description != null) {
            return defCommand.description;
        }
        return super.getDescription();
    }

    @Override
    public String getCommandName() {
        return name;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String commandLabel, String[] args) throws IllegalArgumentException {
        if (commandLabel.contains(":")) commandLabel = ACFPatterns.COLON.split(commandLabel, 2)[1];
        return getTabCompletions(manager.getCommandIssuer(sender), commandLabel, args);
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (commandLabel.contains(":")) commandLabel = ACFPatterns.COLON.split(commandLabel, 2)[1];
        execute(manager.getCommandIssuer(sender), commandLabel, args);
        return true;
    }

    @Override
    public boolean testPermissionSilent(CommandSender target) {
        return hasAnyPermission(manager.getCommandIssuer(target));
    }

    public void addChild(BaseCommand command) {
        if (this.defCommand == null || !command.subCommands.get(BaseCommand.DEFAULT).isEmpty()) {
            this.defCommand = command;
        }
        addChildShared(this.children, this.subCommands, command);
        setPermission(getUniquePermission());
    }

    @Override
    public CommandManager getManager() {
        return manager;
    }

    @Override
    public SetMultimap<String, RegisteredCommand> getSubCommands() {
        return this.subCommands;
    }

    @Override
    public List<BaseCommand> getChildren() {
        return children;
    }

    @Override
    public BaseCommand getDefCommand() {
        return defCommand;
    }

    @Override
    public Plugin getPlugin() {
        return manager.getPlugin();
    }
}
