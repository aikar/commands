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

import com.google.common.collect.SetMultimap;

import java.util.*;

public class CommandHelp {
    private final CommandManager manager;
    private final List<HelpEntry> helpEntries;
    private final CommandOperationContext currentContext;

    protected CommandHelp(CommandManager manager, BaseCommand command, CommandOperationContext currentContext) {
        this.manager = manager;
        this.currentContext = currentContext;

        List<HelpEntry> entries = new ArrayList<>();
        for (RootCommand root : command.registeredCommands.values()) {
            SetMultimap<String, RegisteredCommand> subCommands = root.getSubCommands();
            subCommands.entries().forEach(e -> {
                if (e.getKey().equals("__default") || e.getKey().equals("__unknown")){
                    return;
                }
                RegisteredCommand regCommand = e.getValue();
                entries.add(new HelpEntry(regCommand));

            });
        }

        this.helpEntries = entries;
    }

    public CommandManager getManager() {
        return manager;
    }

    public void showHelp() {
        showHelp(currentContext.getCommandIssuer());
    }

    public void showHelp(CommandIssuer issuer) {
        getHelpEntries().forEach(h -> {
            issuer.sendMessage(MessageType.HELP, MessageKeys.HELP_FORMAT,
                    //{command} {parameters} {seperator} {helptext}
                    "{command}", h.getCommand(),
                    "{parameters}", h.getParameterSyntax(),
                    "{seperator}", h.getHelpText().isEmpty() ? "" : " - ",
                    "{helptext}", h.getHelpText()
            );
        });

    }


    public List<HelpEntry> getHelpEntries() {
        return helpEntries;
    }

    public CommandOperationContext getCurrentContext() {
        return currentContext;
    }
}
