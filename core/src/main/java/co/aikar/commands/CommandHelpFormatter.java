/*
 * Copyright (c) 2016-2018 Daniel Ennis (Aikar) - MIT License
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

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommandHelpFormatter {

    private final CommandManager manager;

    public CommandHelpFormatter(CommandManager manager) {
        this.manager = manager;
    }

    // ########
    // # help #
    // ########

    public void printHelpHeader(CommandIssuer issuer, String command, int page, int totalPages, int totalResults) {
        issuer.sendMessage(MessageType.HELP, MessageKeys.HELP_HEADER,
                "{page}", "" + page,
                "{totalpages}", "" + totalPages,
                "{results}", "" + totalResults,
                "{command}", "" + command
        );
    }

    public void printHelpLine(CommandIssuer issuer, String command, HelpEntry page) {
        String formatted = this.manager.formatMessage(issuer, MessageType.HELP, MessageKeys.HELP_FORMAT, getFormatReplacements(page, command));
        for (String msg : ACFPatterns.NEWLINE.split(formatted)) {
            issuer.sendMessageInternal(ACFUtil.rtrim(msg));
        }
    }

    public void printHelpFooter(CommandIssuer issuer, String command, int page, int totalPages, int totalResults, boolean lastPage) {
        if (lastPage) {
            return;
        }
        issuer.sendMessage(MessageType.HELP, MessageKeys.HELP_PAGE_INFORMATION,
                "{page}", "" + page,
                "{totalpages}", "" + totalPages,
                "{results}", "" + totalResults,
                "{command}", "" + command
        );
    }

    // ##########
    // # search #
    // ##########

    public void printSearchHeader(CommandIssuer issuer, String command, int page, int totalPages, int totalResults, List<String> search) {
        issuer.sendMessage(MessageType.HELP, MessageKeys.HELP_SEARCH_HEADER, getPaginationFormatReplacements(command, page, totalPages, totalResults, search));
    }

    public void printSearchLine(CommandIssuer issuer, String command, HelpEntry page, int score) {
        String formatted = this.manager.formatMessage(issuer, MessageType.HELP, MessageKeys.HELP_FORMAT, getFormatReplacements(page, command));
        for (String msg : ACFPatterns.NEWLINE.split(formatted)) {
            issuer.sendMessageInternal(ACFUtil.rtrim(msg));
        }
    }

    public void printSearchFooter(CommandIssuer issuer, String command, int page, int totalPages, int totalResults, List<String> search, boolean lastPage) {
        if (lastPage) {
            return;
        }
        issuer.sendMessage(MessageType.HELP, MessageKeys.HELP_PAGE_INFORMATION, getPaginationFormatReplacements(command, page, totalPages, totalResults, search)
        );
    }


    // ############
    // # detailed #
    // ############

    public void printDetailedHelpHeader(CommandIssuer issuer, String command, HelpEntry entry) {
        issuer.sendMessage(MessageType.HELP, MessageKeys.HELP_DETAILED_HEADER,
                "{command}", entry.getCommand(),
                "{command}", command
        );
    }

    public void printDetailedHelpLine(CommandIssuer issuer, String rootCommand, HelpEntry entry, String subCommand, String paramDescription) {
        String formattedMsg = this.manager.formatMessage(issuer, MessageType.HELP, MessageKeys.HELP_DETAILED_FORMAT, getDetailedFormatReplacements(subCommand, paramDescription, entry, rootCommand));
        for (String msg : ACFPatterns.NEWLINE.split(formattedMsg)) {
            issuer.sendMessageInternal(ACFUtil.rtrim(msg));
        }
    }

    public void printDetailedHelpFooter(CommandIssuer issuer, String command, HelpEntry page) {
        // default doesn't have a footer
    }

    @NotNull
    public String[] getPaginationFormatReplacements(String command, int page, int totalPages, int totalResults, List<String> search) {
        return new String[]{
                "{search}", String.join(" ", search),
                "{command}", command,
                "{rootcommand}", command,
                "{page}", "" + page,
                "{totalpages}", "" + totalPages,
                "{results}", "" + totalResults
        };
    }

    /**
     * Override this to control replacements
     *
     * @param e
     * @param command
     * @return
     */
    public String[] getFormatReplacements(HelpEntry e, String command) {
        //{command} {parameters} {separator} {description}
        return new String[]{
                "{command}", e.getCommand(),
                "{rootcommand}", command,
                "{parameters}", e.getParameterSyntax(),
                "{separator}", e.getDescription().isEmpty() ? "" : "-",
                "{description}", e.getDescription()
        };
    }

    /**
     * Override this to control replacements
     *
     * @param name
     * @param description
     * @param page
     * @param rootCommand
     * @return
     */
    @NotNull
    public String[] getDetailedFormatReplacements(String name, String description, HelpEntry page, String rootCommand) {
        //{name} {description}
        return new String[]{
                "{name}", name,
                "{command}", page.getCommand(),
                "{rootcommand}", rootCommand,
                "{description}", description
        };
    }
}
