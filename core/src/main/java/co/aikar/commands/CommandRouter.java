/*
 * Copyright (c) 2016-2019 Daniel Ennis (Aikar) - MIT License
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

import co.aikar.commands.apachecommonslang.ApacheCommonsLangUtil;
import com.google.common.collect.SetMultimap;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static co.aikar.commands.BaseCommand.CATCHUNKNOWN;
import static co.aikar.commands.BaseCommand.DEFAULT;

class CommandRouter {

    private final CommandManager manager;

    CommandRouter(CommandManager manager) {
        this.manager = manager;
    }

    CommandRouteResult matchCommand(RouteSearch search, boolean completion) {
        Set<RegisteredCommand> cmds = search.commands;
        String[] args = search.args;
        if (!cmds.isEmpty()) {
            if (cmds.size() == 1) {
                return new CommandRouteResult(ACFUtil.getFirstElement(cmds), search);
            } else {
                Optional<RegisteredCommand> optCmd = cmds.stream()
                        .filter(c -> isProbableMatch(c, args, completion))
                        .min((c1, c2) -> {
                    int a = c1.consumeInputResolvers;
                    int b = c2.consumeInputResolvers;

                    if (a == b) {
                        return 0;
                    }
                    return a < b ? 1 : -1;
                });
                if (optCmd.isPresent()) {
                    return new CommandRouteResult(optCmd.get(), search);
                }
            }
        }
        return null;
    }

    /**
     * @param c
     * @param args
     * @param completion
     * @return
     * @TODO: Improve this to be more accurate like @Default handling.
     */
    private boolean isProbableMatch(RegisteredCommand c, String[] args, boolean completion) {
        int required = c.requiredResolvers;
        int optional = c.optionalResolvers;
        return args.length <= required + optional && (completion || args.length >= required);
    }

    RouteSearch routeCommand(RootCommand command, String commandLabel, String[] args, boolean completion) {
        SetMultimap<String, RegisteredCommand> subCommands = command.getSubCommands();
        int argLength = args.length;
        for (int i = argLength; i >= 0; i--) {
            String subcommand = ApacheCommonsLangUtil.join(args, " ", 0, i).toLowerCase();
            Set<RegisteredCommand> cmds = subCommands.get(subcommand);

            if (!cmds.isEmpty()) {
                return new RouteSearch(cmds, Arrays.copyOfRange(args, i, argLength), commandLabel, subcommand, completion);
            }
        }

        Set<RegisteredCommand> defaultCommands = subCommands.get(DEFAULT);
        Set<RegisteredCommand> unknownCommands = subCommands.get(CATCHUNKNOWN);
        if (!defaultCommands.isEmpty()) {
            Set<RegisteredCommand> matchedDefault = new HashSet<>();
            for (RegisteredCommand c : defaultCommands) {
                int required = c.requiredResolvers;
                int optional = c.optionalResolvers;
                CommandParameter lastParam = c.parameters.length > 0 ? c.parameters[c.parameters.length - 1] : null;
                if (argLength <= required + optional || (
                        lastParam != null && (
                                lastParam.getType() == String[].class
                                        ||
                                        (argLength >= required && lastParam.consumesRest)
                        )
                )) {
                    matchedDefault.add(c);
                }
            }
            if (!matchedDefault.isEmpty()) {
                return new RouteSearch(matchedDefault, args, commandLabel, null, completion);
            }
        }

        if (!unknownCommands.isEmpty()) {
            return new RouteSearch(unknownCommands, args, commandLabel, null, completion);
        }

        return null;
    }

    static class CommandRouteResult {
        final RegisteredCommand cmd;
        final String[] args;
        final String commandLabel;
        final String subcommand;

        CommandRouteResult(RegisteredCommand cmd, RouteSearch search) {
            this(cmd, search.args, search.subcommand, search.commandLabel);
        }

        CommandRouteResult(RegisteredCommand cmd, String[] args, String subcommand, String commandLabel) {
            this.cmd = cmd;
            this.args = args;
            this.commandLabel = commandLabel;
            this.subcommand = subcommand;
        }

    }

    static class RouteSearch {
        final String[] args;
        final Set<RegisteredCommand> commands;
        final String commandLabel;
        final String subcommand;

        RouteSearch(Set<RegisteredCommand> commands, String[] args, String commandLabel, String subcommand, boolean completion) {
            this.commands = commands;
            this.args = args;
            this.commandLabel = commandLabel.toLowerCase();
            this.subcommand = subcommand;
        }
    }

}
