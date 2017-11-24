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

import co.aikar.commands.annotation.*;
import co.aikar.commands.apachecommonslang.ApacheCommonsLangUtil;
import com.google.common.collect.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public abstract class BaseCommand {

    public static final String UNKNOWN = "__unknown";
    public static final String DEFAULT = "__default";
    final SetMultimap<String, RegisteredCommand> subCommands = HashMultimap.create();
    private Method preCommandHandler;

    @SuppressWarnings("WeakerAccess")
    private String execLabel;
    @SuppressWarnings("WeakerAccess")
    private String execSubcommand;
    @SuppressWarnings("WeakerAccess")
    private String[] origArgs;
    CommandManager<?, ?, ?, ?> manager = null;
    BaseCommand parentCommand;
    Map<String, RootCommand> registeredCommands = new HashMap<>();
    String description;
    String commandName;
    String usageMessage;
    String permission;

    private ExceptionHandler exceptionHandler = null;
    CommandOperationContext lastCommandOperationContext;

    public BaseCommand() {}
    public BaseCommand(String cmd) {
        this.commandName = cmd;
    }

    /**
     * Gets the root command name that the user actually typed
     * @return Name
     */
    public String getExecCommandLabel() {
        return execLabel;
    }

    /**
     * Gets the actual sub command name the user typed
     * @return Name
     */
    public String getExecSubcommand() {
        return execSubcommand;
    }

    /**
     * Gets the actual args in string form the user typed
     * @return Args
     */
    public String[] getOrigArgs() {
        return origArgs;
    }

    void setParentCommand(BaseCommand command) {
        this.parentCommand = command;
    }
    void onRegister(CommandManager manager) {
        onRegister(manager, this.commandName);
    }
    void onRegister(CommandManager manager, String cmd) {
        this.manager = manager;
        final Class<? extends BaseCommand> self = this.getClass();
        CommandAlias rootCmdAliasAnno = self.getAnnotation(CommandAlias.class);
        String rootCmdAlias = rootCmdAliasAnno != null ? manager.getCommandReplacements().replace(rootCmdAliasAnno.value()).toLowerCase() : null;
        if (cmd == null && rootCmdAlias != null) {
            cmd = ACFPatterns.PIPE.split(rootCmdAlias)[0];
        }
        this.commandName = cmd != null ? cmd : self.getSimpleName().toLowerCase();

        this.description = this.commandName + " commands";
        this.usageMessage = "/" + this.commandName;

        final CommandPermission perm = self.getAnnotation(CommandPermission.class);
        if (perm != null) {
            this.permission = manager.getCommandReplacements().replace(perm.value());
        }

        boolean foundDefault = false;
        boolean foundUnknown = false;
        for (Method method : self.getMethods()) {
            method.setAccessible(true);
            String sublist = null;
            String sub = getSubcommandValue(method);
            final Default def = method.getAnnotation(Default.class);
            final HelpCommand helpCommand = method.getAnnotation(HelpCommand.class);

            final CommandAlias commandAliases = method.getAnnotation(CommandAlias.class);

            if (def != null || (!foundDefault && helpCommand != null)) {
                if (!foundDefault) {
                    registerSubcommand(method, DEFAULT);
                    if (def != null) {
                        foundDefault = true;
                    }
                } else {
                    ACFUtil.sneaky(new IllegalStateException("Multiple @Default/@HelpCommand commands, duplicate on " + method.getDeclaringClass().getName() + "#" + method.getName()));
                }
            }

            if (sub != null) {
                sublist = sub;
            } else if (commandAliases != null) {
                sublist = commandAliases.value();
            } else if (helpCommand != null) {
                sublist = helpCommand.value();
            }

            UnknownHandler unknown    = method.getAnnotation(UnknownHandler.class);
            PreCommand     preCommand = method.getAnnotation(PreCommand.class);
            if (unknown != null || (!foundUnknown && helpCommand != null)) {
                if (!foundUnknown) {
                    registerSubcommand(method, UNKNOWN);
                    if (unknown != null) {
                        foundUnknown = true;
                    }
                } else {
                    ACFUtil.sneaky(new IllegalStateException("Multiple @UnknownHandler/@HelpCommand commands, duplicate on " + method.getDeclaringClass().getName() + "#" + method.getName()));
                }
            } else if (preCommand != null) {
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

        if (rootCmdAlias != null) {
            Set<String> cmdList = new HashSet<>();
            Collections.addAll(cmdList, ACFPatterns.PIPE.split(rootCmdAlias));
            cmdList.remove(cmd);
            for (String cmdAlias : cmdList) {
                register(cmdAlias, this);
            }
        }

        if (cmd != null) {
            register(cmd, this);
        }
        for (Class<?> clazz : this.getClass().getDeclaredClasses()) {
            if (BaseCommand.class.isAssignableFrom(clazz)) {
                try {
                    BaseCommand subCommand = null;
                    Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();
                    for (Constructor<?> declaredConstructor : declaredConstructors) {

                        declaredConstructor.setAccessible(true);
                        Parameter[] parameters = declaredConstructor.getParameters();
                        if (parameters.length == 1) {
                            subCommand = (BaseCommand) declaredConstructor.newInstance(this);
                        } else {
                            manager.log(LogLevel.INFO, "Found unusable constructor: " + declaredConstructor.getName() + "(" + Stream.of(parameters).map(p -> p.getType().getSimpleName() + " " + p.getName()).collect(Collectors.joining("<c2>,</c2> ")) + ")");
                        }
                    }
                    if (subCommand != null) {
                        subCommand.setParentCommand(this);
                        subCommand.onRegister(manager, cmd);
                        this.subCommands.putAll(subCommand.subCommands);
                        this.registeredCommands.putAll(subCommand.registeredCommands);
                    } else {
                        this.manager.log(LogLevel.ERROR, "Could not find a subcommand ctor for " + clazz.getName());
                    }
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private String getSubcommandValue(Method method) {
        final Subcommand sub = method.getAnnotation(Subcommand.class);
        if (sub == null) {
            return null;
        }
        List<String> subList = new ArrayList<>();
        subList.add(sub.value());
        Class<?> clazz = method.getDeclaringClass();
        while (clazz != null) {
            Subcommand classSub = clazz.getAnnotation(Subcommand.class);
            if (classSub != null) {
                subList.add(classSub.value());
            }
            clazz = clazz.getEnclosingClass();
        }
        Collections.reverse(subList);
        return ACFUtil.join(subList, " ");
    }

    private void register(String name, BaseCommand cmd) {
        String nameLower = name.toLowerCase();
        RootCommand rootCommand = manager.obtainRootCommand(nameLower);
        rootCommand.addChild(cmd);

        this.registeredCommands.put(nameLower, rootCommand);
    }

    private void registerSubcommand(Method method, String subCommand) {
        subCommand = manager.getCommandReplacements().replace(subCommand.toLowerCase());
        final String[] subCommandParts = ACFPatterns.SPACE.split(subCommand);
        // Must run getSubcommandPossibility BEFORE we rewrite it just after this.
        Set<String> cmdList = getSubCommandPossibilityList(subCommandParts);

        // Strip pipes off for auto complete addition
        for (int i = 0; i < subCommandParts.length; i++) {
            subCommandParts[i] = ACFPatterns.PIPE.split(subCommandParts[i])[0];
        }
        String prefSubCommand = ApacheCommonsLangUtil.join(subCommandParts, " ");
        final CommandAlias cmdAlias = method.getAnnotation(CommandAlias.class);

        final String[] aliasNames = cmdAlias != null ? ACFPatterns.PIPE.split(manager.getCommandReplacements().replace(cmdAlias.value().toLowerCase())) : null;
        String cmdName = aliasNames != null ? aliasNames[0] : this.commandName + " ";
        RegisteredCommand cmd = manager.createRegisteredCommand(this, cmdName, method, prefSubCommand);

        for (String subcmd : cmdList) {
            subCommands.put(subcmd, cmd);
        }
        cmd.addSubcommands(cmdList);

        if (aliasNames != null) {
            for (String name : aliasNames) {
                register(name, new ForwardingCommand(this, subCommandParts));
            }
        }
    }

    /**
     * Takes a string like "foo|bar baz|qux" and generates a list of
     * - foo baz
     * - foo qux
     * - bar baz
     * - bar qux
     *
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

    public void execute(CommandIssuer issuer, String commandLabel, String[] args) {
        commandLabel = commandLabel.toLowerCase();
        try {
            CommandOperationContext commandContext = preCommandOperation(issuer, commandLabel, args);

            if (args.length > 0) {
                CommandSearch cmd = findSubCommand(args);
                if (cmd != null) {
                    execSubcommand = cmd.getCheckSub();
                    final String[] execargs = Arrays.copyOfRange(args, cmd.argIndex, args.length);
                    executeCommand(commandContext, issuer, execargs, cmd.cmd);
                    return;
                }
            }


            if (subCommands.get(DEFAULT) != null && args.length == 0) {
                executeSubcommand(commandContext, DEFAULT, issuer, args);
            } else if (subCommands.get(UNKNOWN) != null) {
                if (!executeSubcommand(commandContext, UNKNOWN, issuer, args)) {
                    help(issuer, args);
                }
            } else if (subCommands.get(DEFAULT) != null) {
                executeSubcommand(commandContext, DEFAULT, issuer, args);
            }

        } finally {
            postCommandOperation();
        }
    }

    private void postCommandOperation() {
        CommandManager.commandOperationContext.get().pop();
        execSubcommand = null;
        execLabel = null;
        origArgs = new String[]{};
    }

    private CommandOperationContext preCommandOperation(CommandIssuer issuer, String commandLabel, String[] args) {
        Stack<CommandOperationContext> contexts = CommandManager.commandOperationContext.get();
        CommandOperationContext context = this.manager.createCommandOperationContext(this, issuer, commandLabel, args);
        contexts.push(context);
        lastCommandOperationContext = context;
        execSubcommand = null;
        execLabel = commandLabel;
        origArgs = args;
        return context;
    }

    public CommandIssuer getCurrentCommandIssuer() {
        return CommandManager.getCurrentCommandIssuer();
    }
    public CommandManager getCurrentCommandManager() {
        return CommandManager.getCurrentCommandManager();
    }

    private CommandSearch findSubCommand(String[] args) {
        return findSubCommand(args, false);
    }
    private CommandSearch findSubCommand(String[] args, boolean completion) {
        for (int i = args.length; i >= 0; i--) {
            String checkSub = ApacheCommonsLangUtil.join(args, " ", 0, i).toLowerCase();
            Set<RegisteredCommand> cmds = subCommands.get(checkSub);

            final int extraArgs = args.length - i;
            if (!cmds.isEmpty()) {
                RegisteredCommand cmd = null;
                if (cmds.size() == 1) {
                    cmd = Iterables.getOnlyElement(cmds);
                } else {
                    Optional<RegisteredCommand> optCmd = cmds.stream().filter(c -> {
                        int required = c.requiredResolvers;
                        int optional = c.optionalResolvers;
                        return extraArgs <= required + optional && (completion || extraArgs >= required);
                    }).sorted((c1, c2) -> {
                        int a = c1.requiredResolvers + c1.optionalResolvers;
                        int b = c2.requiredResolvers + c2.optionalResolvers;

                        if (a == b) {
                            return 0;
                        }
                        return a < b ? 1 : -1;
                    }).findFirst();
                    if (optCmd.isPresent()) {
                        cmd = optCmd.get();
                    }
                }
                if (cmd != null) {
                    return new CommandSearch(cmd, i, checkSub);
                }
            }
        }
        return null;
    }

    private void executeCommand(CommandOperationContext commandOperationContext,
                                CommandIssuer issuer, String[] args, RegisteredCommand cmd) {
        if (cmd.hasPermission(issuer)) {
            commandOperationContext.setRegisteredCommand(cmd);
            if (checkPrecommand(commandOperationContext, cmd, issuer, args)) {
                return;
            }
            List<String> sargs = Lists.newArrayList(args);
            cmd.invoke(issuer, sargs);
        } else {
            issuer.sendMessage(MessageType.ERROR, MessageKeys.PERMISSION_DENIED);
        }
    }

    public boolean canExecute(CommandIssuer issuer, RegisteredCommand<?> cmd) {
        return true;
    }

    public List<String> tabComplete(CommandIssuer issuer, String commandLabel, String[] args)
        throws IllegalArgumentException {

        commandLabel = commandLabel.toLowerCase();
        try {
            CommandOperationContext commandOperationContext = preCommandOperation(issuer, commandLabel, args);

            final CommandSearch search = findSubCommand(args, true);

            String argString = ApacheCommonsLangUtil.join(args, " ").toLowerCase();

            final List<String> cmds = new ArrayList<>();

            if (search != null) {
                cmds.addAll(completeCommand(commandOperationContext, issuer, search.cmd, Arrays.copyOfRange(args, search.argIndex, args.length), commandLabel));
            } else if (subCommands.get(UNKNOWN).size() == 1) {
                cmds.addAll(completeCommand(commandOperationContext, issuer, Iterables.getOnlyElement(subCommands.get(UNKNOWN)), args, commandLabel));
            }

            for (Map.Entry<String, RegisteredCommand> entry : subCommands.entries()) {
                final String key = entry.getKey();
                if (key.startsWith(argString) && !UNKNOWN.equals(key) && !DEFAULT.equals(key)) {
                    final RegisteredCommand value = entry.getValue();
                    if (!value.hasPermission(issuer)) {
                        continue;
                    }
                    String prefCommand = value.prefSubCommand;

                    final String[] psplit = ACFPatterns.SPACE.split(prefCommand);
                    cmds.add(psplit[args.length - 1]);
                }
            }

            return filterTabComplete(args[args.length - 1], cmds);
        } finally {
            postCommandOperation();
        }
    }

    private List<String> completeCommand(CommandOperationContext commandOperationContext, CommandIssuer issuer, RegisteredCommand cmd, String[] args, String commandLabel) {
        if (!cmd.hasPermission(issuer) || args.length > cmd.requiredResolvers + cmd.optionalResolvers || args.length == 0
                || cmd.complete == null) {
            return ImmutableList.of();
        }

        String[] completions = ACFPatterns.SPACE.split(cmd.complete);

        List<String> cmds = manager.getCommandCompletions().of(commandOperationContext, cmd, issuer, completions, args);
        return filterTabComplete(args[args.length-1], cmds);
    }

    private static List<String> filterTabComplete(String arg, List<String> cmds) {
        return cmds.stream()
                   .distinct()
                   .filter(cmd -> cmd != null && (arg.isEmpty() || ApacheCommonsLangUtil.startsWithIgnoreCase(cmd, arg)))
                   .collect(Collectors.toList());
    }

    RegisteredCommand getSubcommand(String subcommand) {
        return getSubcommand(subcommand, false);
    }

    RegisteredCommand getSubcommand(String subcommand, boolean requireOne) {
        final Set<RegisteredCommand> commands = subCommands.get(subcommand);
        if (!commands.isEmpty() && (!requireOne || commands.size() == 1)) {
            return commands.iterator().next();
        }
        return null;
    }

    private boolean executeSubcommand(CommandOperationContext commandContext, String subcommand, CommandIssuer issuer, String... args) {
        final RegisteredCommand cmd = this.getSubcommand(subcommand);
        if (cmd != null) {
            executeCommand(commandContext, issuer, args, cmd);
            return true;
        }

        return false;
    }

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

    /** @deprecated Unstable API */ @Deprecated @UnstableAPI
    public CommandHelp getCommandHelp() {
       return manager.generateCommandHelp();
    }

    /** @deprecated Unstable API */ @Deprecated @UnstableAPI
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
                "{command}", "/" + cmd.command,
                "{syntax}", cmd.syntaxText
        );
    }

    public boolean hasPermission(Object issuer) {
        return hasPermission(manager.getCommandIssuer(issuer));
    }

    public boolean hasPermission(CommandIssuer issuer) {
        return permission == null || permission.isEmpty() || (manager.hasPermission(issuer, permission) && (parentCommand == null || parentCommand.hasPermission(issuer)));
    }


    public Set<String> getRequiredPermissions() {
        if (this.permission == null || this.permission.isEmpty()) {
            return ImmutableSet.of();
        }
        return Sets.newHashSet(ACFPatterns.COMMA.split(this.permission));
    }

    public boolean requiresPermission(String permission) {
        return getRequiredPermissions().contains(permission) || this.parentCommand != null && parentCommand.requiresPermission(permission);
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
        return this.getSubcommand(DEFAULT);
    }

    private static class CommandSearch { RegisteredCommand cmd; int argIndex; String checkSub;

        CommandSearch(RegisteredCommand cmd, int argIndex, String checkSub) {
            this.cmd = cmd;
            this.argIndex = argIndex;
            this.checkSub = checkSub;
        }

        String getCheckSub() {
            return this.checkSub;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CommandSearch that = (CommandSearch) o;
            return argIndex == that.argIndex &&
                    Objects.equals(cmd, that.cmd) &&
                    Objects.equals(checkSub, that.checkSub);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cmd, argIndex, checkSub);
        }
    }
}
