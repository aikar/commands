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

@SuppressWarnings("WeakerAccess")
public class CommandHelp {
    private final CommandManager manager;
    private final CommandIssuer issuer;
    private final List<HelpEntry> helpEntries = new ArrayList<>();

    public CommandHelp(CommandManager manager, RootCommand rootCommand, CommandIssuer issuer) {
        this.manager = manager;
        this.issuer = issuer;

        SetMultimap<String, RegisteredCommand> subCommands = rootCommand.getSubCommands();
        subCommands.entries().forEach(e -> {
            String key = e.getKey();
            if (key.equals("__default") || key.equals("__unknown")){
                return;
            }
            RegisteredCommand regCommand = e.getValue();
            if (regCommand.hasPermission(issuer)) {
                this.helpEntries.add(new HelpEntry(regCommand));
            }
        });
    }

    public CommandManager getManager() {
        return manager;
    }

    public void showHelp() {
        showHelp(issuer);
    }

    public void showHelp(CommandIssuer issuer) {
        getHelpEntries().forEach(e -> issuer.sendMessage(MessageType.HELP, MessageKeys.HELP_FORMAT,
                //{command} {parameters} {seperator} {helptext}
                "{command}", e.getCommand(),
                "{parameters}", e.getParameterSyntax(),
                "{seperator}", e.getHelpText().isEmpty() ? "" : " - ",
                "{helptext}", e.getHelpText()
        ));
    }

    public List<HelpEntry> getHelpEntries() {
        return helpEntries;
    }
}
