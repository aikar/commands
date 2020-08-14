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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import co.aikar.commands.apachecommonslang.ApacheCommonsExceptionUtil;
import net.kyori.text.format.TextColor;

public class VelocityCommandManager extends
        CommandManager<CommandSource, VelocityCommandIssuer, TextColor, VelocityMessageFormatter, VelocityCommandExecutionContext, VelocityConditionContext> {

    protected final ProxyServer proxy;
    protected final PluginContainer plugin;
    protected Map<String, VelocityRootCommand> registeredCommands = new HashMap<>();
    protected VelocityCommandContexts contexts;
    protected VelocityCommandCompletions completions;
    protected VelocityLocales locales;

    public VelocityCommandManager(ProxyServer proxy, Object plugin) {
        this.proxy = proxy;
        this.plugin = proxy.getPluginManager().getPlugin(plugin.getClass().getAnnotation(Plugin.class).id()).get();
        this.formatters.put(MessageType.ERROR, defaultFormatter = new VelocityMessageFormatter(TextColor.RED, TextColor.YELLOW, TextColor.RED));
        this.formatters.put(MessageType.SYNTAX, new VelocityMessageFormatter(TextColor.YELLOW, TextColor.GREEN, TextColor.WHITE));
        this.formatters.put(MessageType.INFO, new VelocityMessageFormatter(TextColor.BLUE, TextColor.DARK_GREEN, TextColor.GREEN));
        this.formatters.put(MessageType.HELP, new VelocityMessageFormatter(TextColor.AQUA, TextColor.GREEN, TextColor.YELLOW));

        getLocales();

        proxy.getEventManager().register(plugin, new ACFVelocityListener(this, this.plugin, proxy));

        registerDependency(plugin.getClass(), plugin);
        registerDependency(Plugin.class, plugin);
        registerDependency(ProxyServer.class, proxy);
    }

    public ProxyServer getProxy() {
        return this.proxy;
    }

    public PluginContainer getPlugin() {
        return this.plugin;
    }

    @Override
    public synchronized CommandContexts<VelocityCommandExecutionContext> getCommandContexts() {
        if (this.contexts == null) {
            this.contexts = new VelocityCommandContexts(proxy, this);
        }
        return contexts;
    }

    @Override
    public synchronized CommandCompletions<VelocityCommandCompletionContext> getCommandCompletions() {
        if (this.completions == null) {
            this.completions = new VelocityCommandCompletions(proxy, this);
        }
        return completions;
    }

    @Override
    public VelocityLocales getLocales() {
        if (this.locales == null) {
            this.locales = new VelocityLocales(this);
            this.locales.loadLanguages();
        }
        return locales;
    }

    public void readLocale(Player player) {
        if (!player.isActive()) {
            return;
        }

        //This can be null if we didn't received a settings packet
        Locale locale = player.getPlayerSettings().getLocale();
        if (locale != null) {
            setIssuerLocale(player, player.getPlayerSettings().getLocale());
        }
    }

    @Override
    public void registerCommand(BaseCommand command) {
        registerCommand(command, false);
    }

    public void registerCommand(BaseCommand command, boolean force) {
        command.onRegister(this);
        for (Map.Entry<String, RootCommand> entry : command.registeredCommands.entrySet()) {
            String commandName = entry.getKey().toLowerCase(Locale.ENGLISH);
            VelocityRootCommand velocityCommand = (VelocityRootCommand) entry.getValue();
            if (!velocityCommand.isRegistered) {
                if (force) {
                    proxy.getCommandManager().unregister(commandName);
                }
                proxy.getCommandManager().register(velocityCommand, commandName);
            }
            velocityCommand.isRegistered = true;
            registeredCommands.put(commandName, velocityCommand);
        }
    }

    public void unregisterCommand(BaseCommand command) {
        for (Map.Entry<String, RootCommand> entry : command.registeredCommands.entrySet()) {
            String commandName = entry.getKey().toLowerCase(Locale.ENGLISH);
            VelocityRootCommand velocityCommand = (VelocityRootCommand) entry.getValue();
            velocityCommand.getSubCommands().values().removeAll(command.subCommands.values());
            if (velocityCommand.getSubCommands().isEmpty() && velocityCommand.isRegistered) {
                unregisterCommand(velocityCommand);
                velocityCommand.isRegistered = false;
                registeredCommands.remove(commandName);
            }
        }
    }

    public void unregisterCommand(VelocityRootCommand command) {
        proxy.getCommandManager().unregister(command.getCommandName());
    }

    public void unregisterCommands() {
        for (Map.Entry<String, VelocityRootCommand> entry : registeredCommands.entrySet()) {
            unregisterCommand(entry.getValue());
        }
    }

    @Override
    public boolean hasRegisteredCommands() {
        return !registeredCommands.isEmpty();
    }

    @Override
    public boolean isCommandIssuer(Class<?> aClass) {
        return CommandSource.class.isAssignableFrom(aClass);
    }

    @Override
    public VelocityCommandIssuer getCommandIssuer(Object issuer) {
        if (!(issuer instanceof CommandSource)) {
            throw new IllegalArgumentException(issuer.getClass().getName() + " is not a Command Issuer.");
        }
        return new VelocityCommandIssuer(this, (CommandSource) issuer);
    }

    @Override
    public RootCommand createRootCommand(String cmd) {
        return new VelocityRootCommand(this, cmd);
    }

    @Override
    public Collection<RootCommand> getRegisteredRootCommands() {
        return Collections.unmodifiableCollection(registeredCommands.values());
    }

    @Override
    public VelocityCommandExecutionContext createCommandContext(RegisteredCommand command, CommandParameter parameter, CommandIssuer sender, List<String> args, int i, Map<String, Object> passedArgs) {
        return new VelocityCommandExecutionContext(command, parameter, (VelocityCommandIssuer) sender, args, i, passedArgs);
    }

    @Override
    public CommandCompletionContext createCompletionContext(RegisteredCommand command, CommandIssuer sender, String input, String config, String[] args) {
        return new VelocityCommandCompletionContext(command, (VelocityCommandIssuer) sender, input, config, args);
    }

    @Override
    public RegisteredCommand createRegisteredCommand(BaseCommand command, String cmdName, Method method, String prefSubCommand) {
        return new RegisteredCommand(command, cmdName, method, prefSubCommand);
    }

    @Override
    public VelocityConditionContext createConditionContext(CommandIssuer issuer, String config) {
        return new VelocityConditionContext((VelocityCommandIssuer) issuer, config);
    }

    @Override
    public void log(LogLevel level, String message, Throwable throwable) {
        // TODO: Find better solution
        Logger logger = LoggerFactory.getLogger(plugin.getClass());
        if (level == LogLevel.INFO) {
            logger.info(LogLevel.LOG_PREFIX + message);
        } else {
            logger.warn(LogLevel.LOG_PREFIX + message);
        }

        if (throwable != null) {
            for (String line : ACFPatterns.NEWLINE.split(ApacheCommonsExceptionUtil.getFullStackTrace(throwable))) {
                if (level == LogLevel.INFO) {
                    logger.info(LogLevel.LOG_PREFIX + line);
                } else {
                    logger.warn(LogLevel.LOG_PREFIX + line);
                }
            }
        }
    }


    @Override
    public String getCommandPrefix(CommandIssuer issuer) {
        return issuer.isPlayer() ? "/" : "";
    }
}
