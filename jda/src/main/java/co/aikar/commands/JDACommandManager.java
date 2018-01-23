package co.aikar.commands;

import co.aikar.commands.apachecommonslang.ApacheCommonsExceptionUtil;
import com.google.common.collect.Maps;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JDACommandManager extends CommandManager<
        MessageReceivedEvent,
        JDACommandEvent,
        String,
        MessageFormatter<String>,
        JDACommandExecutionContext,
        JDAConditionContext
        > {

    private final JDA jda;

    private Logger logger;
    private JDACommandConfig defaultConfig;
    private JDACommandConfigProvider configProvider;
    protected JDACommandCompletions completions;
    protected JDACommandContexts contexts;
    protected JDALocales locales;

    protected Map<String, JDARootCommand> commands = Maps.newHashMap();

    public JDACommandManager(JDA jda) {
        this(jda, null, null);
    }
    public JDACommandManager(JDA jda, JDACommandConfig defaultConfig) {
        this(jda, defaultConfig, null);
    }
    public JDACommandManager(JDA jda, JDACommandConfigProvider configProvider) {
        this(jda, null, configProvider);
    }

    public JDACommandManager(JDA jda, JDACommandConfig defaultConfig, JDACommandConfigProvider configProvider) {
        this.jda = jda;
        jda.addEventListener(new JDAListener(this));
        this.defaultConfig = defaultConfig == null ? new JDACommandConfig() : defaultConfig;
        this.completions = new JDACommandCompletions(this);
        this.logger = Logger.getLogger(this.getClass().getSimpleName());
    }

    public JDA getJDA() {
        return jda;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public JDACommandConfig getDefaultConfig() {
        return defaultConfig;
    }

    public void setDefaultConfig(JDACommandConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

    public JDACommandConfigProvider getConfigProvider() {
        return configProvider;
    }

    public void setConfigProvider(JDACommandConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    @Override
    public CommandContexts<?> getCommandContexts() {
        if (this.contexts == null) {
            this.contexts = new JDACommandContexts(this);
        }
        return null;
    }

    @Override
    public CommandCompletions<?> getCommandCompletions() {
        return this.completions;
    }

    @Override
    public void registerCommand(BaseCommand command) {
        for (Map.Entry<String, RootCommand> entry : command.registeredCommands.entrySet()) {
            String commandName = entry.getKey().toLowerCase();
            JDARootCommand cmd = (JDARootCommand) entry.getValue();
            if (!cmd.isRegistered) {
                cmd.isRegistered = true;
                commands.put(commandName, cmd);
            }
        }
    }

    @Override
    public boolean hasRegisteredCommands() {
        return !this.commands.isEmpty();
    }

    @Override
    public boolean isCommandIssuer(Class<?> type) {
        return JDACommandEvent.class.isAssignableFrom(type);
    }

    @Override
    public JDACommandEvent getCommandIssuer(Object issuer) {
        if (!(issuer instanceof MessageReceivedEvent)) {
            throw new IllegalArgumentException(issuer.getClass().getName() + " is not a Message Received Event.");
        }
        return new JDACommandEvent(this, (MessageReceivedEvent) issuer);
    }

    @Override
    public RootCommand createRootCommand(String cmd) {
        return new JDARootCommand(this, cmd);
    }

    @Override
    public Locales getLocales() {
        if (this.locales == null) {
            this.locales = new JDALocales(this);
            this.locales.loadLanguages();
        }
        return null;
    }

    @Override
    public CommandExecutionContext createCommandContext(RegisteredCommand command, Parameter parameter, CommandIssuer sender, List<String> args, int i, Map<String, Object> passedArgs) {
        return new JDACommandExecutionContext(command, parameter, (JDACommandEvent) sender, args, i, passedArgs);
    }

    @Override
    public CommandCompletionContext createCompletionContext(RegisteredCommand command, CommandIssuer sender, String input, String config, String[] args) {
        // Not really going to be used;
        //noinspection unchecked
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

    void dispatchEvent(MessageReceivedEvent event) {
        Message message = event.getMessage();
        String msg = message.getContentDisplay();

        JDACommandConfig config = this.defaultConfig;
        if (this.configProvider != null) {
            JDACommandConfig provided = this.configProvider.provide(event);
            if (provided != null) {
                config = provided;
            }
        }

        if (!msg.startsWith(config.startsWith)) {
            return;
        }

        String[] args = ACFPatterns.SPACE.split(msg.substring(1), -1);
        if (args.length == 0) {
            return;
        }
        String cmd = args[0].toLowerCase();
        JDARootCommand rootCommand = this.commands.get(cmd);
        if (rootCommand == null) {
            return;
        }
        if (args.length > 1) {
            args = Arrays.copyOfRange(args, 1, args.length);
        } else {
            args = new String[0];
        }
        rootCommand.execute(this.getCommandIssuer(event), cmd, args);
    }
}
