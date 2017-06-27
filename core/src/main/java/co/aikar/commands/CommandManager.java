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
import java.lang.reflect.Parameter;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
abstract class CommandManager {

    protected Map<String, RootCommand> rootCommands = new HashMap<>();
    protected CommandReplacements replacements = new CommandReplacements(this);
    protected Locales locales = new Locales(this);
    protected ExceptionHandler defaultExceptionHandler = null;
    protected EnumMap<MessageType, MessageFormatter> formatters = new EnumMap<>(MessageType.class);
    {
        MessageFormatter plain = message -> message;
        formatters.put(MessageType.INFO, plain);
        formatters.put(MessageType.SYNTAX, plain);
        formatters.put(MessageType.ERROR, plain);
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

    /**
     * Registers a command with ACF
     *
     * @param command The command to register
     * @return boolean
     */
    public abstract void registerCommand(BaseCommand command);
    public abstract boolean hasRegisteredCommands();
    public abstract boolean isCommandIssuer(Class<?> type);

    public abstract CommandIssuer getCommandIssuer(Object issuer);

    public abstract RootCommand createRootCommand(String cmd);

    public abstract <R extends CommandExecutionContext> R createCommandContext(RegisteredCommand command, Parameter parameter, CommandIssuer sender, List<String> args, int i, Map<String, Object> passedArgs);

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

    /**
     * Returns a Locales Manager to add and modify language tables for your commands.
     * @return
     */
    Locales getLocales() {
        return locales;
    }

    public boolean hasPermission(CommandIssuer issuer, String permission) {
        return permission == null || permission.isEmpty() || issuer.hasPermission(permission);
    }

    public synchronized RootCommand obtainRootCommand(String cmd) {
        return rootCommands.computeIfAbsent(cmd.toLowerCase(), this::createRootCommand);
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
        boolean result = false;
        if (scope.getExceptionHandler() != null) {
            result = scope.getExceptionHandler().execute(scope, registeredCommand, sender, args, t);
        } else if (defaultExceptionHandler != null) {
            result = defaultExceptionHandler.execute(scope, registeredCommand, sender, args, t);
        }
        return result;
    }

    protected void sendMessage(Object issuerArg, MessageType type, MessageKey key, String... replacements) {
        CommandIssuer issuer = issuerArg instanceof CommandIssuer ? (CommandIssuer) issuerArg : getCommandIssuer(issuerArg);
        Locale locale = getIssuerLocale(issuer);
        String message = getLocales().getMessage(locale, key);
        if (replacements.length > 0) {
            message = ACFUtil.replaceStrings(message, replacements);
        }
        issuer.sendMessage(type, message);
    }

    public Locale getIssuerLocale(CommandIssuer issuer) {
        return getLocales().getDefaultLocale();
    }
}
