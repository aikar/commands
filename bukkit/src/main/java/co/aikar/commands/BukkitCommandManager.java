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

import co.aikar.commands.apachecommonslang.ApacheCommonsExceptionUtil;
import co.aikar.timings.lib.MCTiming;
import co.aikar.timings.lib.TimingManager;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("WeakerAccess")
public class BukkitCommandManager extends CommandManager<CommandSender, BukkitCommandIssuer, ChatColor, BukkitMessageFormatter> {

    @SuppressWarnings("WeakerAccess")
    protected final Plugin plugin;
    private final CommandMap commandMap;
    private final TimingManager timingManager;
    private final BukkitTask localeTask;
    protected Map<String, Command> knownCommands = new HashMap<>();
    protected Map<String, BukkitRootCommand> registeredCommands = new HashMap<>();
    protected BukkitCommandContexts contexts;
    protected BukkitCommandCompletions completions;
    MCTiming commandTiming;
    protected BukkitLocales locales;
    private boolean cantReadLocale = false;
    protected Map<UUID, Locale> issuersLocale = Maps.newConcurrentMap();

    @SuppressWarnings("JavaReflectionMemberAccess")
    public BukkitCommandManager(Plugin plugin) {
        this.plugin = plugin;
        this.timingManager = TimingManager.of(plugin);
        this.commandTiming = this.timingManager.of("Commands");
        this.commandMap = hookCommandMap();
        this.formatters.put(MessageType.ERROR, defaultFormatter = new BukkitMessageFormatter(ChatColor.RED, ChatColor.YELLOW, ChatColor.RED));
        this.formatters.put(MessageType.SYNTAX, new BukkitMessageFormatter(ChatColor.YELLOW, ChatColor.GREEN, ChatColor.WHITE));
        this.formatters.put(MessageType.INFO, new BukkitMessageFormatter(ChatColor.BLUE, ChatColor.DARK_GREEN, ChatColor.GREEN));
        this.formatters.put(MessageType.HELP, new BukkitMessageFormatter(ChatColor.AQUA, ChatColor.GREEN, ChatColor.YELLOW));
        Bukkit.getPluginManager().registerEvents(new ACFBukkitListener(plugin), plugin);
        getLocales(); // auto load locales
        this.localeTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (cantReadLocale) {
                return;
            }
            Bukkit.getOnlinePlayers().forEach(this::readPlayerLocale);
        }, 5, 5);
    }

    @NotNull private CommandMap hookCommandMap() {
        CommandMap commandMap = null;
        try {
            Server server = Bukkit.getServer();
            Method getCommandMap = server.getClass().getDeclaredMethod("getCommandMap");
            getCommandMap.setAccessible(true);
            commandMap = (CommandMap) getCommandMap.invoke(server);
            if (!SimpleCommandMap.class.isAssignableFrom(commandMap.getClass())) {
                this.log(LogLevel.ERROR, "ERROR: CommandMap has been hijacked! Offending command map is located at: " + commandMap.getClass().getName());
                this.log(LogLevel.ERROR, "We are going to try to hijack it back and resolve this, but you are now in dangerous territory.");
                this.log(LogLevel.ERROR, "We can not guarantee things are going to work.");
                Field cmField = server.getClass().getDeclaredField("commandMap");
                commandMap = new ProxyCommandMap(this, commandMap);
                cmField.set(server, commandMap);
                this.log(LogLevel.INFO, "Injected Proxy Command Map... good luck...");
            }
            Field knownCommands = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommands.setAccessible(true);
            //noinspection unchecked
            this.knownCommands = (Map<String, Command>) knownCommands.get(commandMap);
        } catch (Exception e) {
            this.log(LogLevel.ERROR, "Failed to get Command Map. ACF will not function.");
            ACFUtil.sneaky(e);
        }
        return commandMap;
    }

    public Plugin getPlugin() {
        return this.plugin;
    }

    @Override
    public boolean isCommandIssuer(Class<?> type) {
        return CommandSender.class.isAssignableFrom(type);
    }

    @Override
    public synchronized CommandContexts<BukkitCommandExecutionContext> getCommandContexts() {
        if (this.contexts == null) {
            this.contexts = new BukkitCommandContexts(this);
        }
        return contexts;
    }

    @Override
    public synchronized CommandCompletions<BukkitCommandCompletionContext> getCommandCompletions() {
        if (this.completions == null) {
            this.completions = new BukkitCommandCompletions(this);
        }
        return completions;
    }


    @Override
    public BukkitLocales getLocales() {
        if (this.locales == null) {
            this.locales = new BukkitLocales(this);
            this.locales.loadLanguages();
        }
        return locales;
    }


    @Override
    public boolean hasRegisteredCommands() {
        return !registeredCommands.isEmpty();
    }

    public void registerCommand(BaseCommand command, boolean force) {
        final String plugin = this.plugin.getName().toLowerCase();
        command.onRegister(this);
        for (Map.Entry<String, RootCommand> entry : command.registeredCommands.entrySet()) {
            String commandName = entry.getKey().toLowerCase();
            BukkitRootCommand bukkitCommand = (BukkitRootCommand) entry.getValue();
            if (!bukkitCommand.isRegistered) {
                if (force && knownCommands.containsKey(commandName)) {
                    Command oldCommand = commandMap.getCommand(commandName);
                    knownCommands.remove(commandName);
                    for (Map.Entry<String, Command> ce : knownCommands.entrySet()) {
                        String key = ce.getKey();
                        Command value = ce.getValue();
                        if (key.contains(":") && oldCommand.equals(value)) {
                            String[] split = ACFPatterns.COLON.split(key, 2);
                            if (split.length > 1) {
                                oldCommand.unregister(commandMap);
                                oldCommand.setLabel(split[0] + ":" + command.getName());
                                oldCommand.register(commandMap);
                            }
                        }
                    }
                }
                commandMap.register(commandName, plugin, bukkitCommand);
            }
            bukkitCommand.isRegistered = true;
            registeredCommands.put(commandName, bukkitCommand);
        }
    }

    @Override
    public void registerCommand(BaseCommand command) {
        registerCommand(command, false);
    }

    public void unregisterCommand(BaseCommand command) {
        for (RootCommand rootcommand : command.registeredCommands.values()) {
            BukkitRootCommand bukkitCommand = (BukkitRootCommand) rootcommand;
            bukkitCommand.getSubCommands().values().removeAll(command.subCommands.values());
            if (bukkitCommand.isRegistered && bukkitCommand.getSubCommands().isEmpty()) {
                unregisterCommand(bukkitCommand);
                bukkitCommand.isRegistered = false;
            }
        }
    }

    /**
     * @deprecated Use unregisterCommand(BaseCommand) - this will be visibility reduced later.
     * @param command
     */
    @Deprecated
    public void unregisterCommand(BukkitRootCommand command) {
        final String plugin = this.plugin.getName().toLowerCase();
        command.unregister(commandMap);
        String key = command.getName();
        Command registered = knownCommands.get(key);
        if (command.equals(registered)) {
            knownCommands.remove(key);
        }
        knownCommands.remove(plugin + ":" + key);
    }

    public void unregisterCommands() {
        for (Map.Entry<String, BukkitRootCommand> entry : registeredCommands.entrySet()) {
            unregisterCommand(entry.getValue());
        }
        this.registeredCommands.clear();
    }


    private Field getEntityField(Player player) throws NoSuchFieldException {
        Class cls = player.getClass();
        while (cls != Object.class) {
            if (cls.getName().endsWith("CraftEntity")) {
                Field field = cls.getDeclaredField("entity");
                field.setAccessible(true);
                return field;
            }
            cls = cls.getSuperclass();
        }
        return null;
    }

    private void readPlayerLocale(Player player) {
        if (!player.isOnline() || cantReadLocale) {
            return;
        }
        try {
            Field entityField = getEntityField(player);
            if (entityField == null) {
                return;
            }
            Object nmsPlayer = entityField.get(player);
            if (nmsPlayer != null) {
                Field localeField = nmsPlayer.getClass().getField("locale");
                Object localeString = localeField.get(nmsPlayer);
                if (localeString != null && localeString instanceof String) {
                    String[] split = ACFPatterns.UNDERSCORE.split((String) localeString);
                    Locale locale = split.length > 1 ? new Locale(split[0], split[1]) : new Locale(split[0]);
                    Locale prev = issuersLocale.put(player.getUniqueId(), locale);
                    if (!Objects.equals(locale, prev)) {
                        this.notifyLocaleChange(getCommandIssuer(player), prev, locale);
                    }
                }
            }
        } catch (Exception e) {
            cantReadLocale = true;
            this.localeTask.cancel();
            this.log(LogLevel.INFO, "Can't read players locale, you will be unable to automatically detect players language. Only Bukkit 1.7+ is supported for this.", e);
        }
    }

    private class ACFBukkitListener implements Listener {
        private final Plugin plugin;

        public ACFBukkitListener(Plugin plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void onPluginDisable(PluginDisableEvent event) {
            if (!(plugin.getName().equalsIgnoreCase(event.getPlugin().getName()))) {
                return;
            }
            unregisterCommands();
        }
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            readPlayerLocale(player);
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> readPlayerLocale(player), 20);
        }

        @EventHandler
        public void onPlayerJoin(PlayerQuitEvent event) {
            issuersLocale.remove(event.getPlayer().getUniqueId());
        }
    }

    public TimingManager getTimings() {
        return timingManager;
    }

    @Override
    public RootCommand createRootCommand(String cmd) {
        return new BukkitRootCommand(this, cmd);
    }

    @Override
    public BukkitCommandIssuer getCommandIssuer(Object issuer) {
        if (!(issuer instanceof CommandSender)) {
            throw new IllegalArgumentException(issuer.getClass().getName() + " is not a Command Issuer.");
        }
        return new BukkitCommandIssuer(this, (CommandSender) issuer);
    }

    @Override
    public <R extends CommandExecutionContext> R createCommandContext(RegisteredCommand command, Parameter parameter, CommandIssuer sender, List<String> args, int i, Map<String, Object> passedArgs) {
        //noinspection unchecked
        return (R) new BukkitCommandExecutionContext(command, parameter, (BukkitCommandIssuer) sender, args, i, passedArgs);
    }

    @Override
    public CommandCompletionContext createCompletionContext(RegisteredCommand command, CommandIssuer sender, String input, String config, String[] args) {
        return new BukkitCommandCompletionContext(command, sender, input, config, args);
    }

    @Override
    public RegisteredCommand createRegisteredCommand(BaseCommand command, String cmdName, Method method, String prefSubCommand) {
        return new BukkitRegisteredCommand(command, cmdName, method, prefSubCommand);
    }

    @Override
    public void log(LogLevel level, String message, Throwable throwable) {
        Logger logger = this.plugin.getLogger();
        Level logLevel = level == LogLevel.INFO ? Level.INFO : Level.SEVERE;
        logger.log(logLevel, LogLevel.LOG_PREFIX + message);
        if (throwable != null) {
            for (String line : ACFPatterns.NEWLINE.split(ApacheCommonsExceptionUtil.getFullStackTrace(throwable))) {
                logger.log(logLevel, LogLevel.LOG_PREFIX + line);
            }
        }
    }

    @Override
    public Locale getIssuerLocale(CommandIssuer issuer) {
        UUID uniqueId = ((Player) issuer.getIssuer()).getUniqueId();
        Locale locale = issuersLocale.get(uniqueId);
        if (locale != null) {
            return locale;
        }
        return super.getIssuerLocale(issuer);
    }
}
