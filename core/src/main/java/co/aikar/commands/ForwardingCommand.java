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

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ForwardingCommand extends BaseCommand {
    private final BaseCommand command;
    private final String[] baseArgs;
    private final RegisteredCommand regCommand;

    ForwardingCommand(BaseCommand baseCommand, RegisteredCommand regCommand, String[] baseArgs) {
        this.regCommand = regCommand;
        this.commandName = baseCommand.commandName;
        this.command = baseCommand;
        this.baseArgs = baseArgs;
        this.manager = baseCommand.manager;
        this.subCommands.put(DEFAULT, regCommand);
    }

    @Override
    public List<RegisteredCommand> getRegisteredCommands() {
        return Collections.singletonList(regCommand);
    }

    @Override
    public CommandOperationContext getLastCommandOperationContext() {
        return command.getLastCommandOperationContext();
    }

    @Override
    public Set<String> getRequiredPermissions() {
        return command.getRequiredPermissions();
    }

    @Override
    public boolean hasPermission(Object issuer) {
        return command.hasPermission(issuer);
    }

    @Override
    public boolean requiresPermission(String permission) {
        return command.requiresPermission(permission);
    }

    @Override
    public boolean hasPermission(CommandIssuer sender) {
        return command.hasPermission(sender);
    }

    @Override
    public List<String> tabComplete(CommandIssuer issuer, RootCommand rootCommand, String[] args, boolean isAsync) throws IllegalArgumentException {
        return command.tabComplete(issuer, rootCommand, args, isAsync);
    }

    @Override
    public void execute(CommandIssuer issuer, CommandRouter.CommandRouteResult result) {
        result = new CommandRouter.CommandRouteResult(regCommand, result.args, ACFUtil.join(baseArgs), result.commandLabel);
        command.execute(issuer, result);
    }

    BaseCommand getCommand() {
        return command;
    }
}
