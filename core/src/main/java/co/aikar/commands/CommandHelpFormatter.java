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

    public void printHelpHeader(CommandIssuer issuer, String commandName, int page, int totalPages, int totalResults) {
        issuer.sendMessage(MessageType.HELP, MessageKeys.HELP_HEADER, "{command}", commandName);
    }

    public void printHelpLine(CommandIssuer issuer, String commandName, HelpEntry page) {
        String formatted = this.manager.formatMessage(issuer, MessageType.HELP, MessageKeys.HELP_FORMAT, getFormatReplacements(page));
        for (String msg : ACFPatterns.NEWLINE.split(formatted)) {
            issuer.sendMessageInternal(ACFUtil.rtrim(msg));
        }
    }

    public void printHelpFooter(CommandIssuer issuer, String commandName, int page, int totalPages, int totalResults, boolean lastPage) {
        if(lastPage)return;
        issuer.sendMessage(MessageType.HELP, MessageKeys.HELP_PAGE_INFORMATION,
                "{page}", "" + page,
                "{totalpages}", ""+ totalPages,
                "{results}", "" + totalResults
        );
    }

    // ##########
    // # search #
    // ##########

    public void printSearchHeader(CommandIssuer issuer, String commandName, int page, int totalPages, int totalResults, List<String> search) {
        issuer.sendMessage(MessageType.HELP, MessageKeys.HELP_SEARCH_HEADER,
                "{command}", commandName,
                "{search}", String.join(" ", search));
    }

    public void printSearchLine(CommandIssuer issuer, String commandName, HelpEntry page, int score) {
        String formatted = this.manager.formatMessage(issuer, MessageType.HELP, MessageKeys.HELP_FORMAT, getFormatReplacements(page));
        for (String msg : ACFPatterns.NEWLINE.split(formatted)) {
            issuer.sendMessageInternal(ACFUtil.rtrim(msg));
        }
    }

    public void printSearchFooter(CommandIssuer issuer, String commandName, int page, int totalPages, int totalResults, List<String> search, boolean lastPage) {
        if(lastPage)return;
        issuer.sendMessage(MessageType.HELP, MessageKeys.HELP_PAGE_INFORMATION,
                "{page}", "" + page,
                "{totalpages}", ""+ totalPages,
                "{results}", "" + totalResults
        );
    }

    // ############
    // # detailed #
    // ############

    public void printDetailedHelpHeader(CommandIssuer issuer, String commandName, HelpEntry page) {
        issuer.sendMessage(MessageType.HELP, MessageKeys.HELP_DETAILED_HEADER, "{command}", page.getCommand());
    }

    public void printDetailedHelpLine(CommandIssuer issuer, String commandName, HelpEntry page, String subCommand, String paramDescription) {
        String formattedMsg = this.manager.formatMessage(issuer, MessageType.HELP, MessageKeys.HELP_DETAILED_FORMAT, getDetailedFormatReplacements(subCommand, paramDescription, page));
        for (String msg : ACFPatterns.NEWLINE.split(formattedMsg)) {
            issuer.sendMessageInternal(ACFUtil.rtrim(msg));
        }
    }

    public void printDetailedHelpFooter(CommandIssuer issuer, String commandName, HelpEntry page) {
        // default doesn't have a footer
    }

    /**
     * Override this to control replacements
     * @param e
     * @return
     */
    @NotNull
    public String[] getFormatReplacements(HelpEntry e) {
        //{command} {parameters} {separator} {description}
        return new String[] {
                "{command}", e.getCommand(),
                "{parameters}", e.getParameterSyntax(),
                "{separator}", e.getDescription().isEmpty() ? "" : "-",
                "{description}", e.getDescription()
        };
    }

    /**
     * Override this to control replacements
     * @param cmd
     * @param help
     * @param page
     * @return
     */
    @NotNull
    public String[] getDetailedFormatReplacements(String cmd, String help, HelpEntry page) {
        //{name} {description}
        return new String[] {
                "{name}", cmd,
                "{description}", help
        };
    }
}
