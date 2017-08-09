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

import co.aikar.locales.MessageKeyProvider;
import com.google.common.collect.SetMultimap;
import org.jetbrains.annotations.NotNull;

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
        Set<RegisteredCommand> seen = new HashSet<>();
        subCommands.entries().forEach(e -> {
            String key = e.getKey();
            if (key.equals("__default") || key.equals("__unknown")){
                return;
            }
            RegisteredCommand regCommand = e.getValue();
            if (regCommand.hasPermission(issuer) && !seen.contains(regCommand)) {
                this.helpEntries.add(new HelpEntry(regCommand));
                seen.add(regCommand);
            }
        });
    }

    public CommandManager getManager() {
        return manager;
    }

    public void showHelp() {
        showHelp(issuer, MessageKeys.HELP_FORMAT);
    }

    public void showHelp(CommandIssuer issuer) {
        showHelp(issuer, MessageKeys.HELP_FORMAT);
    }

    public void showHelp(CommandIssuer issuer, MessageKeyProvider format) {
        getHelpEntries().forEach(e -> {
            String formatted = this.manager.formatMessage(issuer, MessageType.HELP, format, getFormatReplacements(e));
            for (String msg : ACFPatterns.NEWLINE.split(formatted)) {
                issuer.sendMessageInternal(ACFUtil.rtrim(msg));
            }
        });
    }

    /**
     * Override this to control replacements
     * @param e
     * @return
     */
    @NotNull
    public String[] getFormatReplacements(HelpEntry e) {
        //{command} {parameters} {seperator} {description}
        return new String[] {
                "{command}", e.getCommand(),
                "{parameters}", e.getParameterSyntax(),
                "{seperator}", e.getDescription().isEmpty() ? "" : "-",
                "{description}", e.getDescription()
        };
    }

    public List<HelpEntry> getHelpEntries() {
        return helpEntries;
    }
}
