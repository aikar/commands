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

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.PreCommand;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.UnknownHandler;
import co.aikar.commands.apachecommonslang.ApacheCommonsLangUtil;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
    CommandManager manager = null;
    Map<String, RootCommand> registeredCommands = new HashMap<>();
    String description;
    String commandName;
    String usageMessage;
    String permission;

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
        for (Method method : self.getDeclaredMethods()) {
            method.setAccessible(true);
            String sublist = null;
            String sub = getSubcommandValue(method);
            final Default def = method.getAnnotation(Default.class);

            final CommandAlias commandAliases = method.getAnnotation(CommandAlias.class);

            if (def != null) {
                if (!foundDefault) {
                    registerSubcommand(method, DEFAULT);
                    foundDefault = true;
                } else {
                    ACFUtil.sneaky(new IllegalStateException("Multiple @Default commands, duplicate on " + method.getDeclaringClass().getName() + "#" + method.getName()));
                }
            }

            if (sub != null) {
                sublist = sub;
            } else if (commandAliases != null) {
                sublist = commandAliases.value();
            }


            UnknownHandler unknown    = method.getAnnotation(UnknownHandler.class);
            PreCommand     preCommand = method.getAnnotation(PreCommand.class);
            if (unknown != null) {
                if (!foundUnknown) {
                    registerSubcommand(method, UNKNOWN);
                    foundUnknown = true;
                } else {
                    ACFUtil.sneaky(new IllegalStateException("Multiple @UnknownHandler commands, duplicate on " + method.getDeclaringClass().getName() + "#" + method.getName()));
                }
            } else if (preCommand != null) {
                if (this.preCommandHandler == null) {
                    this.preCommandHandler = method;
                } else {
                    ACFUtil.sneaky(new IllegalStateException("Multiple @PreCommand commands, duplicate on " + method.getDeclaringClass().getName() + "#" + method.getName()));
                }
            } else if (sublist != null) {
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
            if (BaseSubCommand.class.isAssignableFrom(clazz)) {
                try {
                    BaseSubCommand subCommand = null;
                    Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();
                    for (Constructor<?> declaredConstructor : declaredConstructors) {

                        declaredConstructor.setAccessible(true);
                        Parameter[] parameters = declaredConstructor.getParameters();
                        if (parameters.length == 1) {
                            subCommand = (BaseSubCommand) declaredConstructor.newInstance(this);
                        } else {
                            manager.log(LogLevel.INFO, "Found unusable constructor: " + declaredConstructor.getName() + "(" + Stream.of(parameters).map(p -> p.getType().getSimpleName() + " " + p.getName()).collect(Collectors.joining(", ")) + ")");
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

    public boolean execute(CommandIssuer sender, String commandLabel, String[] args) {
        commandLabel = commandLabel.toLowerCase();

        execSubcommand = null;
        execLabel = commandLabel;
        origArgs = args;

        if (args.length == 0) {
            if (checkPrecommand(sender, args)) {
                return true;
            }
            executeSubcommand(DEFAULT, sender);
            return true;
        }

        CommandSearch cmd = findSubCommand(args);
        if (cmd != null) {
            execSubcommand = cmd.getCheckSub();
            final String[] execargs = Arrays.copyOfRange(args, cmd.argIndex, args.length);
            if (checkPrecommand(sender, execargs)) {
                return true;
            }
            executeCommand(sender, execargs, cmd.cmd);
            return true;
        }

        if (!executeSubcommand(UNKNOWN, sender, args)) {
            help(sender, args);
        }
        return true;
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
                        int nonSender = c.requiredResolvers;
                        int partialSender = c.optionalResolvers;
                        return extraArgs <= nonSender + partialSender && (completion || extraArgs >= nonSender);
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

    private static void executeCommand(CommandIssuer sender, String[] args, RegisteredCommand cmd) {
        if (cmd.hasPermission(sender)) {
            List<String> sargs = Lists.newArrayList(args);
            cmd.invoke(sender, sargs);
        } else {
            sender.sendMessage("&cI'm sorry, but you do not have permission to perform this command.");
        }
    }

    public boolean canExecute(CommandIssuer sender, RegisteredCommand<?> cmd) {
        return true;
    }

    public List<String> tabComplete(CommandIssuer sender, String commandLabel, String[] args)
        throws IllegalArgumentException {

        commandLabel = commandLabel.toLowerCase();

        final CommandSearch search = findSubCommand(args, true);

        String argString = ApacheCommonsLangUtil.join(args, " ").toLowerCase();

        final List<String> cmds = new ArrayList<>();

        if (search != null) {
            cmds.addAll(completeCommand(sender, search.cmd, Arrays.copyOfRange(args, search.argIndex, args.length), commandLabel));
        }

        for (Map.Entry<String, RegisteredCommand> entry : subCommands.entries()) {
            final String key = entry.getKey();
            if (key.startsWith(argString) && !UNKNOWN.equals(key) && !DEFAULT.equals(key)) {
                final RegisteredCommand value = entry.getValue();
                if (!value.hasPermission(sender)) {
                    continue;
                }
                String prefCommand = value.prefSubCommand;

                final String[] psplit = ACFPatterns.SPACE.split(prefCommand);
                cmds.add(psplit[args.length - 1]);
            }
        }

        return filterTabComplete(args[args.length-1], cmds);
    }

    private List<String> completeCommand(CommandIssuer sender, RegisteredCommand cmd, String[] args, String commandLabel) {
        if (this.testPermission(sender) ||args.length > cmd.requiredResolvers + cmd.optionalResolvers || args.length == 0
                || cmd.complete == null) {
            return ImmutableList.of();
        }

        String[] completions = ACFPatterns.SPACE.split(cmd.complete);

        List<String> cmds = manager.getCommandCompletions().of(cmd, sender, completions, args);
        return filterTabComplete(args[args.length-1], cmds);
    }

    private static List<String> filterTabComplete(String arg, List<String> cmds) {
        return cmds.stream()
                   .distinct()
                   .filter(cmd -> cmd != null && (arg.isEmpty() || ApacheCommonsLangUtil.startsWithIgnoreCase(cmd, arg)))
                   .collect(Collectors.toList());
    }


    private boolean executeSubcommand(String subcommand, CommandIssuer sender, String... args) {
        final Set<RegisteredCommand> defs = subCommands.get(subcommand);
        RegisteredCommand def = null;
        if (!defs.isEmpty()) {
            if (defs.size() == 1) {
                def = defs.iterator().next();
            }
            if (def != null) {
                executeCommand(sender, args, def);
                return true;
            }
        }
        return false;
    }

    private boolean checkPrecommand(CommandIssuer sender, String[] args) {
        if (this.preCommandHandler != null) {
            try {
                if (this.preCommandHandler.getParameterCount() == 1) {
                    return (boolean) preCommandHandler.invoke(this, new Object[]{ sender.getIssuer() });
                } else {
                    return (boolean) preCommandHandler.invoke(this, sender.getIssuer(), args);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                this.manager.log(LogLevel.ERROR, "Exception encountered while command pre-processing", e);
            }
        }
        return false;
    }

    public void help(Object sender, String[] args) {
        help(manager.getCommandIssuer(sender), args);
    }
    public void help(CommandIssuer sender, String[] args) {
        sender.sendMessage("&cUnknown Command, please type &f/help");
    }
    public void doHelp(Object sender, String... args) {
        doHelp(manager.getCommandIssuer(sender), args);
    }
    public void doHelp(CommandIssuer sender, String... args) {
        help(sender, args);
    }

    public void showSyntax(CommandIssuer sender, RegisteredCommand<?> cmd) {
        sender.sendMessage("&cUsage: /" + cmd.command + " " + cmd.syntaxText);
    }

    public boolean testPermission(Object sender) {
        return testPermission(manager.getCommandIssuer(sender));
    }

    public boolean testPermission(CommandIssuer sender) {
        return permission == null || permission.isEmpty() || sender.hasPermission(permission);
    }

    public String getName() {
        return commandName;
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
