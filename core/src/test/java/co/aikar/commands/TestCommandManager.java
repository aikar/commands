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

import co.aikar.commands.apachecommonslang.ApacheCommonsExceptionUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestCommandManager extends CommandManager<
        TestCommandSender,
        TestCommandIssuer,
        String,
        MessageFormatter<String>,
        TestCommandExecutionContext,
        TestConditionContext> {

    private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());
    private final Map<String, TestRootCommand> commands = new HashMap<>();
    private final CommandCompletions<CommandCompletionContext<TestCommandIssuer>> completions = new CommandCompletions<>(this);

    protected TestCommandContexts contexts;
    protected Locales locales;

    @Override
    public CommandContexts<?> getCommandContexts() {
        if (contexts == null) {
            contexts = new TestCommandContexts(this);
        }
        return contexts;
    }

    @Override
    public CommandCompletions<?> getCommandCompletions() {
        return completions;
    }

    @Override
    public void registerCommand(BaseCommand command) {
        command.onRegister(this);
        for (Map.Entry<String, RootCommand> entry : command.registeredCommands.entrySet()) {
            String commandName = entry.getKey().toLowerCase();
            TestRootCommand cmd = (TestRootCommand) entry.getValue();
            if (!cmd.isRegistered) {
                cmd.isRegistered = true;
                commands.put(commandName, cmd);
            }
        }
    }

    public void unregisterCommand(BaseCommand command) {
        for (Map.Entry<String, RootCommand> entry : command.registeredCommands.entrySet()) {
            String commandName = entry.getKey().toLowerCase();
            TestRootCommand cmd = (TestRootCommand) entry.getValue();
            cmd.getSubCommands().values().removeAll(command.subCommands.values());
            if (cmd.isRegistered && cmd.getSubCommands().isEmpty()) {
                cmd.isRegistered = false;
                commands.remove(commandName);
            }
        }
    }

    @Override
    public boolean hasRegisteredCommands() {
        return !commands.isEmpty();
    }

    @Override
    public boolean isCommandIssuer(Class<?> type) {
        return TestCommandIssuer.class.isAssignableFrom(type);
    }

    @Override
    public TestCommandIssuer getCommandIssuer(Object issuer) {
        if (!(issuer instanceof TestCommandSender)) {
            throw new IllegalArgumentException(issuer.getClass().getName() + " is not a TestCommandSender.");
        }
        return new TestCommandIssuer(this, (TestCommandSender) issuer);
    }

    @Override
    public RootCommand createRootCommand(String cmd) {
        return new TestRootCommand(this, cmd);
    }

    @Override
    public Locales getLocales() {
        if (locales == null) {
            locales = new Locales(this);
            locales.loadLanguages();
        }
        return locales;
    }

    @Override
    public CommandExecutionContext createCommandContext(RegisteredCommand command, CommandParameter parameter, CommandIssuer sender, List<String> args, int i, Map<String, Object> passedArgs) {
        return new TestCommandExecutionContext(command, parameter, (TestCommandIssuer) sender, args, i, passedArgs);
    }

    @Override
    public CommandCompletionContext createCompletionContext(RegisteredCommand command, CommandIssuer sender, String input, String config, String[] args) {
        return new CommandCompletionContext(command, sender, input, config, args);
    }

    @Override
    public void log(LogLevel level, String message, Throwable throwable) {
        Level logLevel = level == LogLevel.INFO ? Level.INFO : Level.SEVERE;
        logger.log(logLevel, LogLevel.LOG_PREFIX + message);
        if (throwable != null) {
            for (String line : ACFPatterns.NEWLINE.split(ApacheCommonsExceptionUtil.getFullStackTrace(throwable))) {
                logger.log(logLevel, LogLevel.LOG_PREFIX + line);
            }
        }
    }

    @Override
    public Collection<RootCommand> getRegisteredRootCommands() {
        return Collections.unmodifiableCollection(commands.values());
    }

    public void dispatchCommand(TestCommandSender sender, String command) {
        String[] args = ACFPatterns.SPACE.split(command, -1);
        if (args.length == 0) {
            return;
        }
        String cmd = args[0].toLowerCase();
        TestRootCommand rootCommand = commands.get(cmd);
        if (rootCommand == null) {
            return;
        }
        if (args.length > 1) {
            args = Arrays.copyOfRange(args, 1, args.length);
        } else {
            args = new String[0];
        }
        rootCommand.execute(this.getCommandIssuer(sender), cmd, args);
    }
}
