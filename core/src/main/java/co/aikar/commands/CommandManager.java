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

import co.aikar.commands.annotation.Conditions;
import co.aikar.locales.MessageKeyProvider;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

@SuppressWarnings("WeakerAccess")
public abstract class CommandManager <
        IT,
        I extends CommandIssuer,
        FT,
        MF extends MessageFormatter<FT>,
        CEC extends CommandExecutionContext<CEC, I>,
        CCC extends CommandCompletionContext,
        CC extends ConditionContext<I>,
        PCC extends ParameterConditionContext<?, CEC, I>
    > {

    /**
     * This is a stack incase a command calls a command
     */
    static ThreadLocal<Stack<CommandOperationContext>> commandOperationContext = ThreadLocal.withInitial(() -> {
        return new Stack<CommandOperationContext>() {
            @Override
            public synchronized CommandOperationContext peek() {
                return super.size() == 0 ? null : super.peek();
            }
        };
    });
    protected Map<String, RootCommand> rootCommands = new HashMap<>();
    protected final CommandReplacements replacements = new CommandReplacements(this);
    protected ExceptionHandler defaultExceptionHandler = null;

    protected boolean usePerIssuerLocale = false;
    protected List<IssuerLocaleChangedCallback<I>> localeChangedCallbacks = Lists.newArrayList();
    protected Set<Locale> supportedLanguages = Sets.newHashSet(Locales.ENGLISH, Locales.GERMAN, Locales.SPANISH, Locales.CZECH);
    protected Map<MessageType, MF> formatters = new IdentityHashMap<>();
    protected MF defaultFormatter;
    protected int defaultHelpPerPage = 10;

    private Set<String> unstableAPIs = Sets.newHashSet();

    public static CommandOperationContext getCurrentCommandOperationContext() {
        return commandOperationContext.get().peek();
    }

    public static CommandIssuer getCurrentCommandIssuer() {
        CommandOperationContext context = commandOperationContext.get().peek();
        return context != null ? context.getCommandIssuer() : null;
    }

    public static CommandManager getCurrentCommandManager() {
        CommandOperationContext context = commandOperationContext.get().peek();
        return context != null ? context.getCommandManager() : null;
    }

    public MF setFormat(MessageType type, MF formatter) {
        return formatters.put(type, formatter);
    }

    public MF getFormat(MessageType type) {
        return formatters.getOrDefault(type, defaultFormatter);
    }

    public void setFormat(MessageType type, FT... colors) {
        MF format = getFormat(type);
        for (int i = 0; i < colors.length; i++) {
            format.setColor(i, colors[i]);
        }
    }

    public void setFormat(MessageType type, int i, FT color) {
        MF format = getFormat(type);
        format.setColor(i, color);
    }

    public MF getDefaultFormatter() {
        return defaultFormatter;
    }

    public void setDefaultFormatter(MF defaultFormatter) {
        this.defaultFormatter = defaultFormatter;
    }

    /**
     * Gets the command contexts manager
     * @return Command Contexts
     */
    public abstract CommandContexts<?> getCommandContexts();

    /**
     * Gets the command completions manager
     * @return Command Completions
     */
    public abstract CommandCompletions<?> getCommandCompletions();

    /** @deprecated Unstable API */ @Deprecated @UnstableAPI
    public CommandHelp generateCommandHelp(@NotNull String command) {
        verifyUnstableAPI("help");
        CommandOperationContext context = getCurrentCommandOperationContext();
        if (context == null) {
            throw new IllegalStateException("This method can only be called as part of a command execution.");
        }
        return generateCommandHelp(context.getCommandIssuer(), command);
    }

    /** @deprecated Unstable API */ @Deprecated @UnstableAPI
    public CommandHelp generateCommandHelp(CommandIssuer issuer, @NotNull String command) {
        verifyUnstableAPI("help");
        return generateCommandHelp(issuer, obtainRootCommand(command));
    }

    /** @deprecated Unstable API */ @Deprecated @UnstableAPI
    public CommandHelp generateCommandHelp() {
        verifyUnstableAPI("help");
        CommandOperationContext context = getCurrentCommandOperationContext();
        if (context == null) {
            throw new IllegalStateException("This method can only be called as part of a command execution.");
        }
        String commandLabel = context.getCommandLabel();
        return generateCommandHelp(context.getCommandIssuer(), this.obtainRootCommand(commandLabel));
    }

    /** @deprecated Unstable API */ @Deprecated @UnstableAPI
    public CommandHelp generateCommandHelp(CommandIssuer issuer, RootCommand rootCommand) {
        verifyUnstableAPI("help");
        return new CommandHelp(this, rootCommand, issuer);
    }

    /** @deprecated Unstable API */ @Deprecated @UnstableAPI
    public int getDefaultHelpPerPage() {
        verifyUnstableAPI("help");
        return defaultHelpPerPage;
    }

    /** @deprecated Unstable API */ @Deprecated @UnstableAPI
    public void setDefaultHelpPerPage(int defaultHelpPerPage) {
        verifyUnstableAPI("help");
        this.defaultHelpPerPage = defaultHelpPerPage;
    }

    /**
     * Registers a command with ACF
     *
     * @param command The command to register
     * @return boolean
     */
    public abstract void registerCommand(BaseCommand command);
    public abstract boolean hasRegisteredCommands();
    public abstract boolean isCommandIssuer(Class<?> type);

    // TODO: Change this to I if we make a breaking change
    public abstract I getCommandIssuer(Object issuer);

    public abstract RootCommand createRootCommand(String cmd);

    /**
     * Returns a Locales Manager to add and modify language tables for your commands.
     * @return
     */
    public abstract Locales getLocales();

    public boolean usingPerIssuerLocale() {
        return usePerIssuerLocale;
    }

    public boolean usePerIssuerLocale(boolean setting) {
        boolean old = usePerIssuerLocale;
        usePerIssuerLocale = setting;
        return old;
    }
    public ConditionContext createConditionContext(CommandOperationContext context, Conditions conditions) {
        //noinspection unchecked
        return new ConditionContext<>(context.getRegisteredCommand(), context.getCommandIssuer(), conditions);
    }

    public <P> ParameterConditionContext createConditionContext(CommandOperationContext context, CEC execContext, Conditions conditions) {
        //noinspection unchecked
        return new ParameterConditionContext<P, CEC, I>(context.getRegisteredCommand(), (I) context.getCommandIssuer(), execContext, conditions);
    }

    public abstract CommandExecutionContext createCommandContext(RegisteredCommand command, Parameter parameter, CommandIssuer sender, List<String> args, int i, Map<String, Object> passedArgs);

    public abstract CommandCompletionContext createCompletionContext(RegisteredCommand command, CommandIssuer sender, String input, String config, String[] args);

    public abstract void log(final LogLevel level, final String message, final Throwable throwable);

    public void log(final LogLevel level, final String message) {
        log(level, message, null);
    }

    /**
     * Lets you add custom string replacements that can be applied to annotation values,
     * to reduce duplication/repetition of common values such as permission nodes and command prefixes.
     *
     * Any replacement registered starts with a %
     *
     * So for ex @CommandPermission("%staff")
     * @return Replacements Manager
     */
    public CommandReplacements getCommandReplacements() {
        return replacements;
    }

    public boolean hasPermission(CommandIssuer issuer, String permission) {
        if (permission == null || permission.isEmpty()) {
            return true;
        }
        for (String perm : ACFPatterns.COMMA.split(permission)) {
            if (!perm.isEmpty() && !issuer.hasPermission(perm)) {
                return false;
            }
        }
        return true;
    }

    BaseCommand getBaseCommand(String commandLabel, @NotNull String[] args) {
        RootCommand rootCommand = obtainRootCommand(commandLabel);
        if (rootCommand == null) {
            return null;
        }
        return rootCommand.getBaseCommand(args);
    }

    public synchronized RootCommand getRootCommand(@NotNull String cmd) {
        return rootCommands.get(ACFPatterns.SPACE.split(cmd.toLowerCase(), 2)[0]);
    }

    public synchronized RootCommand obtainRootCommand(@NotNull String cmd) {
        return rootCommands.computeIfAbsent(ACFPatterns.SPACE.split(cmd.toLowerCase(), 2)[0], this::createRootCommand);
    }

    public RegisteredCommand createRegisteredCommand(BaseCommand command, String cmdName, Method method, String prefSubCommand) {
        return new RegisteredCommand(command, cmdName, method, prefSubCommand);
    }

    /**
     * Sets the default {@link ExceptionHandler} that is called when an exception occurs while executing a command, if the command doesn't have it's own exception handler registered.
     *
     * @param exceptionHandler the handler that should handle uncaught exceptions
     */
    public void setDefaultExceptionHandler(ExceptionHandler exceptionHandler) {
        defaultExceptionHandler = exceptionHandler;
    }

    /**
     * Gets the current default exception handler, might be null.
     *
     * @return the default exception handler
     */
    public ExceptionHandler getDefaultExceptionHandler() {
        return defaultExceptionHandler;
    }

    protected boolean handleUncaughtException(BaseCommand scope, RegisteredCommand registeredCommand, CommandIssuer sender, List<String> args, Throwable t) {
        if (t instanceof InvocationTargetException && t.getCause() != null) {
            t = t.getCause();
        }
        boolean result = false;
        if (scope.getExceptionHandler() != null) {
            result = scope.getExceptionHandler().execute(scope, registeredCommand, sender, args, t);
        } else if (defaultExceptionHandler != null) {
            result = defaultExceptionHandler.execute(scope, registeredCommand, sender, args, t);
        }
        return result;
    }

    public void sendMessage(IT issuerArg, MessageType type, MessageKeyProvider key, String... replacements) {
        sendMessage(getCommandIssuer(issuerArg), type, key, replacements);
    }

    public void sendMessage(CommandIssuer issuer, MessageType type, MessageKeyProvider key, String... replacements) {
        String message = formatMessage(issuer, type, key, replacements);

        for (String msg : ACFPatterns.NEWLINE.split(message)) {
            issuer.sendMessageInternal(ACFUtil.rtrim(msg));
        }
    }

    public String formatMessage(CommandIssuer issuer, MessageType type, MessageKeyProvider key, String... replacements) {
        String message = getLocales().getMessage(issuer, key.getMessageKey());
        if (replacements.length > 0) {
            message = ACFUtil.replaceStrings(message, replacements);
        }

        message = getCommandReplacements().replace(message);

        MessageFormatter formatter = formatters.getOrDefault(type, defaultFormatter);
        if (formatter != null) {
            message = formatter.format(message);
        }
        return message;
    }

    public void onLocaleChange(IssuerLocaleChangedCallback<I> onChange) {
        localeChangedCallbacks.add(onChange);
    }

    public void notifyLocaleChange(I issuer, Locale oldLocale, Locale newLocale) {
        localeChangedCallbacks.forEach(cb -> {
            try {
                cb.onIssuerLocaleChange(issuer, oldLocale, newLocale);
            } catch (Exception e) {
                this.log(LogLevel.ERROR, "Error in notifyLocaleChange", e);
            }
        });
    }

    public Locale getIssuerLocale(CommandIssuer issuer) {
        return getLocales().getDefaultLocale();
    }

    CommandOperationContext<I> createCommandOperationContext(BaseCommand command, CommandIssuer issuer, String commandLabel, String[] args, boolean isAsync) {
        //noinspection unchecked
        return new CommandOperationContext<>(
                this,
                (I) issuer,
                command,
                commandLabel,
                args,
                isAsync
        );
    }

    /**
     * Gets a list of all currently supported languages for this manager.
     * These locales will be automatically loaded from
     * @return
     */
    public Set<Locale> getSupportedLanguages() {
        return supportedLanguages;
    }

    /**
     * Adds a new locale to the list of automatic Locales to load Message Bundles for.
     * All bundles loaded under the previous supported languages will now automatically load for this new locale too.
     *
     * @param locale
     */
    public void addSupportedLanguage(Locale locale) {
        supportedLanguages.add(locale);
        getLocales().loadMissingBundles();
    }

    /**
     * @deprecated Use this with caution! If you enable and use Unstable API's, your next compile using ACF
     * may require you to update your implementation to those unstable API's
     */
    @Deprecated
    public void enableUnstableAPI(String api) {
        unstableAPIs.add(api);
    }
    void verifyUnstableAPI(String api) {
        if (!unstableAPIs.contains(api)) {
            throw new IllegalStateException("Using an unstable API that has not been enabled ( " + api + "). See https://acfunstable.emc.gs");
        }
    }
}
