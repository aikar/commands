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

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RootCommand extends Command {

    private BaseCommand defCommand;
    private Map<String, BaseCommand> subCommands = new HashMap<>();
    private List<BaseCommand> children = new ArrayList<>();
    boolean isRegistered = false;

    RootCommand(String name) {
        super(name);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        Set<String> completions = new HashSet<>();
        this.children.forEach(child -> completions.addAll(child.tabComplete(sender, alias, args)));
        return new ArrayList<>(completions);
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        for (int i = args.length; i >= 0; i--) {
            String checkSub = StringUtils.join(args, " ", 0, i).toLowerCase();
            BaseCommand subHandler = this.subCommands.get(checkSub);
            if (subHandler != null) {
                if (!subHandler.testPermission(sender)) {
                    return true;
                }
                subHandler.execute(sender, commandLabel, args);
                return false;
            }
        }
        if (!this.defCommand.testPermission(sender)) {
            return true;
        }
        this.defCommand.execute(sender, commandLabel, args);
        return false;
    }

    void addChild(BaseCommand command) {
        if (this.defCommand == null || command.subCommands.get("__default") != null) {
            this.defCommand = command;
        }
        command.subCommands.keySet().forEach(key -> {
            if (key.equals(BaseCommand.DEFAULT) || key.equals(BaseCommand.UNKNOWN)) {
                return;
            }
            BaseCommand regged = this.subCommands.get(key);
            if (regged != null) {
                ACFLog.severe("ACF Error: " + command.getLabel() + " registered subcommand " + key + " - but it is already defined in " + regged.getLabel());
                ACFLog.severe("2 subcommands of the same prefix may not be spread over 2 different classes. Ignoring this.");
                return;
            }
            this.subCommands.put(key, command);
        });
        this.children.add(command);
    }
}
