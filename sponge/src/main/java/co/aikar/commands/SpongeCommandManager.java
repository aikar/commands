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

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.plugin.PluginContainer;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class SpongeCommandManager extends CommandManager {

    protected final PluginContainer plugin;
    protected Map<String, SpongeRootCommand> registeredCommands = new HashMap<>();
    protected SpongeCommandContexts contexts;
    protected SpongeCommandCompletions completions;

    public SpongeCommandManager(PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isCommandIssuer(Class<?> type) {
        return CommandSource.class.isAssignableFrom(type);
    }

    @Override
    public synchronized CommandContexts<SpongeCommandExecutionContext> getCommandContexts() {
        if (this.contexts == null) {
            this.contexts = new SpongeCommandContexts(this);
        }
        return contexts;
    }

    @Override
    public synchronized CommandCompletions<CommandSource, SpongeCommandCompletionContext> getCommandCompletions() {
        if (this.completions == null) {
            this.completions = new SpongeCommandCompletions(this);
        }
        return completions;
    }

    @Override
    public boolean hasRegisteredCommands() {
        return !registeredCommands.isEmpty();
    }

    @Override
    public void registerCommand(BaseCommand command) {
        command.onRegister(this);

        for (Map.Entry<String, RootCommand> entry : command.registeredCommands.entrySet()) {
            String key = entry.getKey().toLowerCase();
            SpongeRootCommand value = (SpongeRootCommand) entry.getValue();
            if (!value.isRegistered) {
                Sponge.getCommandManager().register(this.plugin, value, value.name);
            }
            value.isRegistered = true;
            registeredCommands.put(key, value);
        }
    }

    public Timing createTiming(final SpongeRegisteredCommand command) {
        return Timings.of(this.plugin, "Command: " + command.command);
    }

    @Override
    public RootCommand createRootCommand(String cmd) {
        return new SpongeRootCommand(this, cmd);
    }

    @Override
    public CommandIssuer getCommandIssuer(Object issuer) {
        if (!(issuer instanceof CommandSource)) {
            throw new IllegalArgumentException(issuer.getClass().getName() + " is not a Command Issuer.");
        }
        return new SpongeCommandIssuer((CommandSource) issuer);
    }

    @Override
    public <R extends CommandExecutionContext> R createCommandContext(RegisteredCommand command, Parameter parameter, CommandIssuer sender, List<String> args, int i, Map<String, Object> passedArgs) {
        return (R) new SpongeCommandExecutionContext(command, parameter, sender, args, i, passedArgs);
    }

    @Override
    public CommandCompletionContext createCompletionContext(RegisteredCommand command, CommandIssuer sender, String input, String config, String[] args) {
        return new SpongeCommandCompletionContext(command, sender, input, config, args);
    }

    @Override
    public RegisteredCommand createRegisteredCommand(BaseCommand command, String cmdName, Method method, String prefSubCommand) {
        return new RegisteredCommand(command, cmdName, method, prefSubCommand);
    }
}
