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

import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import jdk.nashorn.internal.ir.ReturnNode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BukkitRootCommand extends Command implements RootCommand {

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
    public String getCommandName() {
        return name;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        return tabComplete(manager.getCommandIssuer(sender), alias, args);
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        execute(manager.getCommandIssuer(sender), commandLabel, args);
        return true;
    }

    private List<String> tabComplete(CommandIssuer sender, String alias, String[] args) throws IllegalArgumentException {
        Set<String> completions = new HashSet<>();
        this.children.forEach(child -> completions.addAll(child.tabComplete(sender, alias, args)));
        return new ArrayList<>(completions);
    }



    public void addChild(BaseCommand command) {
        if (this.defCommand == null || !command.subCommands.get("__default").isEmpty()) {
            this.defCommand = command;
            this.setPermission(command.permission);
            //this.setDescription(command.getDescription());
            //this.setUsage(command.getUsage());
        }
        addChildShared(this.children, this.subCommands, command);
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
    public BaseCommand getDefCommand(){
        return defCommand;
    }

    @Override
    public String getDescription() {
        final RegisteredCommand cmd = this.getDefaultRegisteredCommand();
        if (cmd != null) {
            return cmd.helpText;
        }
        BaseCommand defCommand = getDefCommand();
        if (defCommand != null) {
            Description descAnno = defCommand.getClass().getAnnotation(Description.class);
            if (descAnno != null) {
                return descAnno.value();
            }
        }
        return "";
    }

    @Override
    public String getUsage() {
        final RegisteredCommand cmd = this.getDefaultRegisteredCommand();
        if (cmd != null) {
            return cmd.syntaxText;
        }
        BaseCommand defCommand = getDefCommand();
        if (defCommand != null) {
            Syntax syntaxAnno = defCommand.getClass().getAnnotation(Syntax.class);
            if (syntaxAnno != null) {
                return syntaxAnno.value();
            }
        }
        return "";
    }
}
