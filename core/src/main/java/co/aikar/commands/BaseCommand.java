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

import co.aikar.commands.CommandRouter.RouteSearch;
import co.aikar.commands.annotation.CatchAll;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.PreCommand;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.UnknownHandler;
import co.aikar.commands.apachecommonslang.ApacheCommonsLangUtil;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Base command is defined as a command group of related commands.
 * A BaseCommand does not imply nor enforce that they use the same root command.
 * <p>
 * It is up to the end user how to organize their command. you could use 1 base command per
 * command in your application.
 * <p>
 * Optionally (and encouraged), you can use the base command to represent a root command, and
 * then each actionable command is a sub command
 */

public abstract class BaseCommand {

    /**
     * This is a field which contains the magic key in the {@link #subCommands} map for the method to catch any unknown
     * argument to command states.
     */
    static final String CATCHUNKNOWN = "__catchunknown";
    /**
     * This is a field which contains the magic key in the {@link #subCommands} map for the method which is default for the
     * entire base command.
     */
    static final String DEFAULT = "__default";

    /**
     * A map of all the registered commands for this base command, keyed to each potential subcommand to access it.
     */
    final SetMultimap<String, RegisteredCommand> subCommands = HashMultimap.create();
    final Set<BaseCommand> subScopes = new HashSet<>();

    /**
     * A map of flags to pass to Context Resolution for every parameter of the type. This is like an automatic @Flags on each.
     */
    final Map<Class<?>, String> contextFlags = new HashMap<>();

    /**
     * What method was annoated with {@link PreCommand} to execute before commands.
     */
    @Nullable
    private Method preCommandHandler;

    /**
     * What root command the user actually entered to access the currently executing command
     */
    @SuppressWarnings("WeakerAccess")
    private String execLabel;
    /**
     * What subcommand the user actually entered to access the currently executing command
     */
    @SuppressWarnings("WeakerAccess")
    private String execSubcommand;
    /**
     * What arguments the user actually entered after the root command to access the currently executing command
     */
    @SuppressWarnings("WeakerAccess")
    private String[] origArgs;

    /**
     * The manager this is registered to
     */
    CommandManager<?, ?, ?, ?, ?, ?> manager = null;

    /**
     * The command which owns this. This may be null if there are no owners.
     */
    BaseCommand parentCommand;
    Map<String, RootCommand> registeredCommands = new HashMap<>();
    /**
     * The description of the command. This may be null if no description has been provided.
     * Used for help documentation
     */
    @Nullable String description;
    /**
     * The name of the command. This may be null if no name has been provided.
     */
    @Nullable String commandName;
    /**
     * The permission of the command. This may be null if no permission has been provided.
     */
    @Nullable String permission;
    /**
     * The conditions of the command. This may be null if no conditions has been provided.
     */
    @Nullable String conditions;
    /**
     * Identifies if the command has an explicit help command annotated with {@link HelpCommand}
     */
    boolean hasHelpCommand;

    /**
     * The handler of all uncaught exceptions thrown by the user's command implementation.
     */
    private ExceptionHandler exceptionHandler = null;
    /**
     * The last operative context data of this command. This may be null if this command hasn't been run yet.
     */
    private final ThreadLocal<CommandOperationContext> lastCommandOperationContext = new ThreadLocal<>();
    /**
     * If a parent exists to this command, and it has  a Subcommand annotation, prefix all subcommands in this class with this
     */
    @Nullable
    private String parentSubcommand;

    /**
     * The permissions of the command.
     */
    private final Set<String> permissions = new HashSet<>();

    public BaseCommand() {
    }

    /**
     * Constructor based defining of commands will be removed in the next version bump.
     *
     * @param cmd
     * @deprecated Please switch to {@link CommandAlias} for defining all root commands.
     */
    @Deprecated
    public BaseCommand(@Nullable String cmd) {
        this.commandName = cmd;
    }

    /**
     * Returns a reference to the last used CommandOperationContext.
     * This method is ThreadLocal, in that it can only be used on a thread that has executed a command
     *
     * @return
     */
    public CommandOperationContext getLastCommandOperationContext() {
        return lastCommandOperationContext.get();
    }

    /**
     * Gets the root command name that the user actually typed
     *
     * @return Name
     */
    public String getExecCommandLabel() {
        return execLabel;
    }

    /**
     * Gets the actual sub command name the user typed
     *
     * @return Name
     */
    public String getExecSubcommand() {
        return execSubcommand;
    }

    /**
     * Gets the actual args in string form the user typed
     *
     * @return Args
     */
    public String[] getOrigArgs() {
        return origArgs;
    }

    /**
     * This should be called whenever the command gets registered.
     * It sets all required fields correctly and injects dependencies.
     *
     * @param manager The manager to register as this command's owner and handler.
     */
    void onRegister(CommandManager manager) {
        onRegister(manager, this.commandName);
    }

    /**
     * This should be called whenever the command gets registered.
     * It sets all required fields correctly and injects dependencies.
     *
     * @param manager The manager to register as this command's owner and handler.
     * @param cmd     The command name to use register with.
     */
    private void onRegister(CommandManager manager, String cmd) {
        manager.injectDependencies(this);
        this.manager = manager;

        final Annotations annotations = manager.getAnnotations();
        final Class<? extends BaseCommand> self = this.getClass();

        String[] cmdAliases = annotations.getAnnotationValues(self, CommandAlias.class, Annotations.REPLACEMENTS | Annotations.LOWERCASE | Annotations.NO_EMPTY);

        if (cmd == null && cmdAliases != null) {
            cmd = cmdAliases[0];
        }

        this.commandName = cmd != null ? cmd : self.getSimpleName().toLowerCase();
        this.permission = annotations.getAnnotationValue(self, CommandPermission.class, Annotations.REPLACEMENTS);
        this.description = annotations.getAnnotationValue(self, Description.class, Annotations.NO_EMPTY | Annotations.REPLACEMENTS);
        this.parentSubcommand = getParentSubcommand(self);
        this.conditions = annotations.getAnnotationValue(self, Conditions.class, Annotations.REPLACEMENTS | Annotations.NO_EMPTY);

        computePermissions(); // Must be before any subcommands so they can inherit permissions
        registerSubcommands();
        registerSubclasses(cmd);

        if (cmdAliases != null) {
            Set<String> cmdList = new HashSet<>();
            Collections.addAll(cmdList, cmdAliases);
            cmdList.remove(cmd);
            for (String cmdAlias : cmdList) {
                register(cmdAlias, this);
            }
        }

        if (cmd != null) {
            register(cmd, this);
        }
    }

    /**
     * This recursively registers all subclasses of the command as subcommands, if they are of type {@link BaseCommand}.
     *
     * @param cmd The command name of this command.
     */
    private void registerSubclasses(String cmd) {
        for (Class<?> clazz : this.getClass().getDeclaredClasses()) {
            if (BaseCommand.class.isAssignableFrom(clazz)) {
                try {
                    BaseCommand subScope = null;
                    Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();
                    for (Constructor<?> declaredConstructor : declaredConstructors) {

                        declaredConstructor.setAccessible(true);
                        Parameter[] parameters = declaredConstructor.getParameters();
                        if (parameters.length == 1) {
                            subScope = (BaseCommand) declaredConstructor.newInstance(this);
                        } else {
                            manager.log(LogLevel.INFO, "Found unusable constructor: " + declaredConstructor.getName() + "(" + Stream.of(parameters).map(p -> p.getType().getSimpleName() + " " + p.getName()).collect(Collectors.joining("<c2>,</c2> ")) + ")");
                        }
                    }
                    if (subScope != null) {
                        subScope.parentCommand = this;
                        this.subScopes.add(subScope);
                        subScope.onRegister(manager, cmd);
                        this.subCommands.putAll(subScope.subCommands);
                        this.registeredCommands.putAll(subScope.registeredCommands);
                    } else {
                        this.manager.log(LogLevel.ERROR, "Could not find a subcommand ctor for " + clazz.getName());
                    }
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    this.manager.log(LogLevel.ERROR, "Error registering subclass", e);
                }
            }
        }
    }

    /**
     * This registers all subcommands of the command.
     */
    private void registerSubcommands() {
        final Annotations annotations = manager.getAnnotations();
        boolean foundCatchUnknown = false;
        boolean isParentEmpty = parentSubcommand == null || parentSubcommand.isEmpty();
        Set<Method> methods = new LinkedHashSet<>();
        Collections.addAll(methods, this.getClass().getDeclaredMethods());
        Collections.addAll(methods, this.getClass().getMethods());

        for (Method method : methods) {
            method.setAccessible(true);
            String sublist = null;
            String sub = getSubcommandValue(method);
            final String helpCommand = annotations.getAnnotationValue(method, HelpCommand.class, Annotations.NOTHING);
            final String commandAliases = annotations.getAnnotationValue(method, CommandAlias.class, Annotations.NOTHING);

            if (annotations.hasAnnotation(method, Default.class)) {
                if (!isParentEmpty) {
                    sub = parentSubcommand;
                } else {
                    registerSubcommand(method, DEFAULT);
                }
            }

            if (sub != null) {
                sublist = sub;
            } else if (commandAliases != null) {
                sublist = commandAliases;
            } else if (helpCommand != null) {
                sublist = helpCommand;
                hasHelpCommand = true;
            }

            boolean preCommand = annotations.hasAnnotation(method, PreCommand.class);
            boolean hasCatchUnknown = annotations.hasAnnotation(method, CatchUnknown.class) ||
                    annotations.hasAnnotation(method, CatchAll.class) ||
                    annotations.hasAnnotation(method, UnknownHandler.class);

            if (hasCatchUnknown || (!foundCatchUnknown && helpCommand != null)) {
                if (!foundCatchUnknown) {
                    if (hasCatchUnknown) {
                        this.subCommands.get(CATCHUNKNOWN).clear();
                        foundCatchUnknown = true;
                    }
                    registerSubcommand(method, CATCHUNKNOWN);
                } else {
                    ACFUtil.sneaky(new IllegalStateException("Multiple @CatchUnknown/@HelpCommand commands, duplicate on " + method.getDeclaringClass().getName() + "#" + method.getName()));
                }
            } else if (preCommand) {
                if (this.preCommandHandler == null) {
                    this.preCommandHandler = method;
                } else {
                    ACFUtil.sneaky(new IllegalStateException("Multiple @PreCommand commands, duplicate on " + method.getDeclaringClass().getName() + "#" + method.getName()));
                }
            }
            if (Objects.equals(method.getDeclaringClass(), this.getClass()) && sublist != null) {
                registerSubcommand(method, sublist);
            }
        }
    }

    /**
     * This registers all the permissions required to execute this command.
     */
    private void computePermissions() {
        this.permissions.clear();
        if (this.permission != null && !this.permission.isEmpty()) {
            this.permissions.addAll(Arrays.asList(ACFPatterns.COMMA.split(this.permission)));
        }
        if (this.parentCommand != null) {
            this.permissions.addAll(this.parentCommand.getRequiredPermissions());
        }
        this.subCommands.values().forEach(RegisteredCommand::computePermissions);
        this.subScopes.forEach(BaseCommand::computePermissions);
    }

    /**
     * Gets the subcommand name of the method given.
     *
     * @param method The method to check.
     * @return The name of the subcommand. It returns null if the input doesn't have {@link Subcommand} attached.
     */
    private String getSubcommandValue(Method method) {
        final String sub = manager.getAnnotations().getAnnotationValue(method, Subcommand.class, Annotations.NOTHING);
        if (sub == null) {
            return null;
        }
        Class<?> clazz = method.getDeclaringClass();
        String parent = getParentSubcommand(clazz);
        return parent == null || parent.isEmpty() ? sub : parent + " " + sub;
    }

    private String getParentSubcommand(Class<?> clazz) {
        List<String> subList = new ArrayList<>();
        while (clazz != null) {
            String sub = manager.getAnnotations().getAnnotationValue(clazz, Subcommand.class, Annotations.NOTHING);
            if (sub != null) {
                subList.add(sub);
            }
            clazz = clazz.getEnclosingClass();
        }
        Collections.reverse(subList);
        return ACFUtil.join(subList, " ");
    }

    /**
     * Registers the given {@link BaseCommand cmd} as a child of the {@link RootCommand} linked to the name given.
     *
     * @param name Name of the parent to cmd.
     * @param cmd  The {@link BaseCommand} to add as a child to the {@link RootCommand} owned name field.
     */
    private void register(String name, BaseCommand cmd) {
        String nameLower = name.toLowerCase();
        RootCommand rootCommand = manager.obtainRootCommand(nameLower);
        rootCommand.addChild(cmd);

        this.registeredCommands.put(nameLower, rootCommand);
    }

    /**
     * Registers the given {@link Method} as a subcommand.
     *
     * @param method     The method to register as a subcommand.
     * @param subCommand The subcommand's name(s).
     */
    private void registerSubcommand(Method method, String subCommand) {
        subCommand = manager.getCommandReplacements().replace(subCommand.toLowerCase());
        final String[] subCommandParts = ACFPatterns.SPACE.split(subCommand);
        // Must run getSubcommandPossibility BEFORE we rewrite it just after this.
        Set<String> cmdList = getSubCommandPossibilityList(subCommandParts);

        // Strip pipes off for auto complete addition
        for (int i = 0; i < subCommandParts.length; i++) {
            String[] split = ACFPatterns.PIPE.split(subCommandParts[i]);
            if (split.length == 0 || split[0].isEmpty()) {
                throw new IllegalArgumentException("Invalid @Subcommand configuration for " + method.getName() + " - parts can not start with | or be empty");
            }
            subCommandParts[i] = split[0];
        }
        String prefSubCommand = ApacheCommonsLangUtil.join(subCommandParts, " ");
        final String[] aliasNames = manager.getAnnotations().getAnnotationValues(method, CommandAlias.class, Annotations.REPLACEMENTS | Annotations.LOWERCASE);

        String cmdName = aliasNames != null ? aliasNames[0] : this.commandName + " ";
        RegisteredCommand cmd = manager.createRegisteredCommand(this, cmdName, method, prefSubCommand);

        for (String subcmd : cmdList) {
            subCommands.put(subcmd, cmd);
        }
        cmd.addSubcommands(cmdList);

        if (aliasNames != null) {
            for (String name : aliasNames) {
                register(name, new ForwardingCommand(this, cmd, subCommandParts));
            }
        }
    }

    /**
     * Takes a string like "foo|bar baz|qux" and generates a list of
     * - foo baz
     * - foo qux
     * - bar baz
     * - bar qux
     * <p>
     * For every possible sub command combination
     *
     * @param subCommandParts
     * @return List of all sub command possibilities
     */
    private static Set<String> getSubCommandPossibilityList(String[] subCommandParts) {
        int i = 0;
        Set<String> current = null;
        while (true) {
            Set<String> newList = new HashSet<>();

            if (i < subCommandParts.length) {
                for (String s1 : ACFPatterns.PIPE.split(subCommandParts[i])) {
                    if (current != null) {
                        newList.addAll(current.stream().map(s -> s + " " + s1).collect(Collectors.toList()));
                    } else {
                        newList.add(s1);
                    }
                }
            }

            if (i + 1 < subCommandParts.length) {
                current = newList;
                i = i + 1;
                continue;
            }

            return newList;
        }
    }

    void execute(CommandIssuer issuer, CommandRouter.CommandRouteResult command) {
        try {
            CommandOperationContext commandContext = preCommandOperation(issuer, command.commandLabel, command.args, false);
            execSubcommand = command.subcommand;
            executeCommand(commandContext, issuer, command.args, command.cmd);
        } finally {
            postCommandOperation();
        }
    }

    /**
     * This is ran after any command operation has been performed.
     */
    private void postCommandOperation() {
        CommandManager.commandOperationContext.get().pop();
        execSubcommand = null;
        execLabel = null;
        origArgs = new String[]{};
    }

    /**
     * This is ran before any command operation has been performed.
     *
     * @param issuer       The user who executed the command.
     * @param commandLabel The label the user used to execute the command. This is not the command name, but their input.
     *                     When there is multiple aliases, this is which alias was used
     * @param args         The arguments passed to the command when executing it.
     * @param isAsync      Whether the command is executed off of the main thread.
     * @return The context which is being registered to the {@link CommandManager}'s {@link
     * CommandManager#commandOperationContext thread local stack}.
     */
    private CommandOperationContext preCommandOperation(CommandIssuer issuer, String commandLabel, String[] args, boolean isAsync) {
        Stack<CommandOperationContext> contexts = CommandManager.commandOperationContext.get();
        CommandOperationContext context = this.manager.createCommandOperationContext(this, issuer, commandLabel, args, isAsync);
        contexts.push(context);
        lastCommandOperationContext.set(context);
        execSubcommand = null;
        execLabel = commandLabel;
        origArgs = args;
        return context;
    }

    /**
     * Gets the current command issuer.
     *
     * @return The current command issuer.
     */
    public CommandIssuer getCurrentCommandIssuer() {
        return CommandManager.getCurrentCommandIssuer();
    }

    /**
     * Gets the current command manager.
     *
     * @return The current command manager.
     */
    public CommandManager getCurrentCommandManager() {
        return CommandManager.getCurrentCommandManager();
    }

    private void executeCommand(CommandOperationContext commandOperationContext,
                                CommandIssuer issuer, String[] args, RegisteredCommand cmd) {
        if (cmd.hasPermission(issuer)) {
            commandOperationContext.setRegisteredCommand(cmd);
            if (checkPrecommand(commandOperationContext, cmd, issuer, args)) {
                return;
            }
            List<String> sargs = Arrays.asList(args);
            cmd.invoke(issuer, sargs, commandOperationContext);
        } else {
            issuer.sendMessage(MessageType.ERROR, MessageKeys.PERMISSION_DENIED);
        }
    }

    /**
     * Please use command conditions for restricting execution
     *
     * @param issuer
     * @param cmd
     * @return
     * @deprecated See {@link CommandConditions}
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public boolean canExecute(CommandIssuer issuer, RegisteredCommand<?> cmd) {
        return true;
    }

    /**
     * Gets tab completed data from the given command from the user.
     *
     * @param issuer       The user who executed the tabcomplete.
     * @param commandLabel The label which is being used by the user.
     * @param args         The arguments the user has typed so far.
     * @return All possibilities in the tab complete.
     */
    public List<String> tabComplete(CommandIssuer issuer, String commandLabel, String[] args) {
        return tabComplete(issuer, commandLabel, args, false);
    }

    /**
     * Gets the tab complete suggestions from a given command. This will automatically find anything
     * which is valid for the specified command through the command's implementation.
     *
     * @param issuer       The issuer of the command.
     * @param commandLabel The command name as entered by the user instead of the ACF registered name.
     * @param args         All arguments entered by the user.
     * @param isAsync      Whether this is run off of the main thread.
     * @return The possibilities to tab complete in no particular order.
     */
    @SuppressWarnings("WeakerAccess")
    public List<String> tabComplete(CommandIssuer issuer, String commandLabel, String[] args, boolean isAsync)
            throws IllegalArgumentException {
        return tabComplete(issuer, manager.getRootCommand(commandLabel.toLowerCase()), args, isAsync);
    }

    List<String> tabComplete(CommandIssuer issuer, RootCommand rootCommand, String[] args, boolean isAsync)
            throws IllegalArgumentException {
        if (args.length == 0) {
            args = new String[]{""};
        }
        String commandLabel = rootCommand.getCommandName();
        try {
            CommandRouter router = manager.getRouter();

            preCommandOperation(issuer, commandLabel, args, isAsync);

            final RouteSearch search = router.routeCommand(rootCommand, commandLabel, args, true);

            final List<String> cmds = new ArrayList<>();
            if (search != null) {
                CommandRouter.CommandRouteResult result = router.matchCommand(search, true);
                if (result != null) {
                    cmds.addAll(completeCommand(issuer, result.cmd, result.args, commandLabel, isAsync));
                }
            }

            return filterTabComplete(args[args.length - 1], cmds);
        } finally {
            postCommandOperation();
        }
    }

    /**
     * Gets all subcommands which are possible to tabcomplete.
     *
     * @param issuer The command issuer.
     * @param args
     * @return
     */
    List<String> getCommandsForCompletion(CommandIssuer issuer, String[] args) {
        final Set<String> cmds = new HashSet<>();
        final int cmdIndex = Math.max(0, args.length - 1);
        String argString = ApacheCommonsLangUtil.join(args, " ").toLowerCase();
        for (Map.Entry<String, RegisteredCommand> entry : subCommands.entries()) {
            final String key = entry.getKey();
            if (key.startsWith(argString) && !CATCHUNKNOWN.equals(key) && !DEFAULT.equals(key)) {
                final RegisteredCommand value = entry.getValue();
                if (!value.hasPermission(issuer) || value.isPrivate) {
                    continue;
                }

                String[] split = ACFPatterns.SPACE.split(value.prefSubCommand);
                cmds.add(split[cmdIndex]);
            }
        }
        return new ArrayList<>(cmds);
    }

    /**
     * Complete a command properly per issuer and input.
     *
     * @param issuer       The user who executed this.
     * @param cmd          The command to be completed.
     * @param args         All arguments given by the user.
     * @param commandLabel The command name the user used.
     * @param isAsync      Whether the command was executed async.
     * @return All results to complete the command.
     */
    private List<String> completeCommand(CommandIssuer issuer, RegisteredCommand cmd, String[] args, String commandLabel, boolean isAsync) {
        if (!cmd.hasPermission(issuer) || args.length == 0) {
            return Collections.emptyList();
        }

        if (!cmd.parameters[cmd.parameters.length - 1].consumesRest && args.length > cmd.consumeInputResolvers) {
            return Collections.emptyList();
        }

        List<String> cmds = manager.getCommandCompletions().of(cmd, issuer, args, isAsync);
        return filterTabComplete(args[args.length - 1], cmds);
    }

    /**
     * Gets the actual args in string form the user typed
     * This returns a list of all tab complete options which are possible with the given argument and commands.
     *
     * @param arg  Argument which was pressed tab on.
     * @param cmds The possibilities to return.
     * @return All possible options. This may be empty.
     */
    private static List<String> filterTabComplete(String arg, List<String> cmds) {
        return cmds.stream()
                .distinct()
                .filter(cmd -> cmd != null && (arg.isEmpty() || ApacheCommonsLangUtil.startsWithIgnoreCase(cmd, arg)))
                .collect(Collectors.toList());
    }

    /**
     * Executes the precommand and sees whether something is wrong. Ideally, you get false from this.
     *
     * @param commandOperationContext The context to use.
     * @param cmd                     The command executed.
     * @param issuer                  The issuer who executed the command.
     * @param args                    The arguments the issuer provided.
     * @return Whether something went wrong.
     */
    private boolean checkPrecommand(CommandOperationContext commandOperationContext, RegisteredCommand cmd, CommandIssuer issuer, String[] args) {
        Method pre = this.preCommandHandler;
        if (pre != null) {
            try {
                Class<?>[] types = pre.getParameterTypes();
                Object[] parameters = new Object[pre.getParameterCount()];
                for (int i = 0; i < parameters.length; i++) {
                    Class<?> type = types[i];
                    Object issuerObject = issuer.getIssuer();
                    if (manager.isCommandIssuer(type) && type.isAssignableFrom(issuerObject.getClass())) {
                        parameters[i] = issuerObject;
                    } else if (CommandIssuer.class.isAssignableFrom(type)) {
                        parameters[i] = issuer;
                    } else if (RegisteredCommand.class.isAssignableFrom(type)) {
                        parameters[i] = cmd;
                    } else if (String[].class.isAssignableFrom((type))) {
                        parameters[i] = args;
                    } else {
                        parameters[i] = null;
                    }
                }

                return (boolean) pre.invoke(this, parameters);
            } catch (IllegalAccessException | InvocationTargetException e) {
                this.manager.log(LogLevel.ERROR, "Exception encountered while command pre-processing", e);
            }
        }
        return false;
    }

    /**
     * @deprecated Unstable API
     */
    @Deprecated
    @UnstableAPI
    public CommandHelp getCommandHelp() {
        return manager.generateCommandHelp();
    }

    /**
     * @deprecated Unstable API
     */
    @Deprecated
    @UnstableAPI
    public void showCommandHelp() {
        getCommandHelp().showHelp();
    }

    public void help(Object issuer, String[] args) {
        help(manager.getCommandIssuer(issuer), args);
    }

    public void help(CommandIssuer issuer, String[] args) {
        issuer.sendMessage(MessageType.ERROR, MessageKeys.UNKNOWN_COMMAND);
    }

    public void doHelp(Object issuer, String... args) {
        doHelp(manager.getCommandIssuer(issuer), args);
    }

    public void doHelp(CommandIssuer issuer, String... args) {
        help(issuer, args);
    }

    public void showSyntax(CommandIssuer issuer, RegisteredCommand<?> cmd) {
        issuer.sendMessage(MessageType.SYNTAX, MessageKeys.INVALID_SYNTAX,
                "{command}", manager.getCommandPrefix(issuer) + cmd.command,
                "{syntax}", cmd.syntaxText
        );
    }

    public boolean hasPermission(Object issuer) {
        return hasPermission(manager.getCommandIssuer(issuer));
    }

    public boolean hasPermission(CommandIssuer issuer) {
        return this.manager.hasPermission(issuer, getRequiredPermissions());
    }

    public Set<String> getRequiredPermissions() {
        return this.permissions;
    }

    public boolean requiresPermission(String permission) {
        return getRequiredPermissions().contains(permission);
    }

    public String getName() {
        return commandName;
    }

    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public BaseCommand setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    public RegisteredCommand getDefaultRegisteredCommand() {
        return ACFUtil.getFirstElement(this.subCommands.get(DEFAULT));
    }

    public String setContextFlags(Class<?> cls, String flags) {
        return this.contextFlags.put(cls, flags);
    }

    public String getContextFlags(Class<?> cls) {
        return this.contextFlags.get(cls);
    }

    public List<RegisteredCommand> getRegisteredCommands() {
        List<RegisteredCommand> registeredCommands = new ArrayList<>();
        registeredCommands.addAll(this.subCommands.values());
        return registeredCommands;
    }
}
