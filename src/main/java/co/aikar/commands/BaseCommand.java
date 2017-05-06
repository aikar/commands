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
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.UnknownHandler;
import co.aikar.timings.lib.MCTiming;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.util.StringUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class BaseCommand extends Command {

    public static final String UNKNOWN = "__unknown";
    public static final String DEFAULT = "__default";
    final SetMultimap<String, RegisteredCommand> subCommands = HashMultimap.create();

    @SuppressWarnings("WeakerAccess")
    private String execLabel;
    @SuppressWarnings("WeakerAccess")
    private String execSubcommand;
    @SuppressWarnings("WeakerAccess")
    private String[] origArgs;
    CommandManager manager = null;
    Map<String, RootCommand> registeredCommands = new HashMap<>();

    public BaseCommand() {
        this(null);
    }

    public BaseCommand(String cmd) {
        super(cmd);
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
        onRegister(manager, getName());
    }
    void onRegister(CommandManager manager, String cmd) {
        this.manager = manager;
        final Class<? extends BaseCommand> self = this.getClass();
        CommandAlias rootCmdAlias = self.getAnnotation(CommandAlias.class);
        if (cmd == null) {
            if (rootCmdAlias == null) {
                cmd = "__" + self.getSimpleName();
            } else {
                cmd = ACFPatterns.PIPE.split(rootCmdAlias.value())[0];
            }
            cmd = cmd.toLowerCase();
            try {
                setName(cmd);
            } catch (NoSuchMethodError ignored) {
                try {
                    // To support pre 1.8 where setName was not added.
                    Field field = Command.class.getDeclaredField("name");
                    field.setAccessible(true);
                    field.set(this, cmd);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    ACFLog.exception("Error setting name for command", e);
                }
            }
            setLabel(cmd);
        }

        this.description = cmd + " commands";
        this.usageMessage = "/" + cmd;

        final CommandPermission perm = self.getAnnotation(CommandPermission.class);
        if (perm != null) {
            this.setPermission(perm.value());
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
                    ACFUtil.sneaky(new InvalidConfigurationException("Multiple @Default commands, duplicate on " + method.getDeclaringClass().getName() + "#" + method.getName()));
                }
            }
            if (sub != null) {
                sublist = sub;
            } else if (commandAliases != null) {
                sublist = commandAliases.value();
            }
            if (sublist != null) {
                registerSubcommand(method, sublist);
            }


            //CommandSender.class, String.class, String[].class
            UnknownHandler unknown = method.getAnnotation(UnknownHandler.class);
            if (unknown != null) {
                if (!foundUnknown) {
                    registerSubcommand(method, UNKNOWN);
                    foundUnknown = true;
                } else {
                    ACFUtil.sneaky(new InvalidConfigurationException("Multiple @UnknownHandler commands, duplicate on " + method.getDeclaringClass().getName() + "#" + method.getName()));
                }
            }
        }

        if (rootCmdAlias != null) {
            List<String> cmdList = new ArrayList<>();
            Collections.addAll(cmdList, ACFPatterns.PIPE.split(rootCmdAlias.value().toLowerCase()));
            cmdList.remove(cmd);
            for (String cmdAlias : cmdList) {
                register(cmdAlias, new ForwardingCommand(this));
            }
        }

        register(cmd, this);
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
                            ACFLog.info("Found unusable constructor: " + declaredConstructor.getName() + "(" + Stream.of(parameters).map(p -> p.getType().getSimpleName() + " " + p.getName()).collect(Collectors.joining(", ")) + ")");
                        }
                    }
                    if (subCommand != null) {
                        subCommand.setParentCommand(this);
                        subCommand.onRegister(manager, cmd);
                        this.subCommands.putAll(subCommand.subCommands);
                        this.registeredCommands.putAll(subCommand.registeredCommands);
                    } else {
                        ACFLog.severe("Could not find a subcommand ctor for " + clazz.getName());
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
        subCommand = subCommand.toLowerCase();
        final String[] subCommandParts = ACFPatterns.SPACE.split(subCommand);
        // Must run getSubcommandPossibility BEFORE we rewrite it just after this.
        List<String> cmdList = getSubCommandPossibilityList(subCommandParts);

        // Strip pipes off for auto complete addition
        for (int i = 0; i < subCommandParts.length; i++) {
            subCommandParts[i] = ACFPatterns.PIPE.split(subCommandParts[i])[0];
        }
        String prefSubCommand = StringUtils.join(subCommandParts, " ");
        final CommandAlias cmdAlias = method.getAnnotation(CommandAlias.class);

        final String[] aliasNames = cmdAlias != null ? ACFPatterns.PIPE.split(cmdAlias.value().toLowerCase()) : null;
        String cmdName = aliasNames != null ? aliasNames[0] : getLabel() + " ";
        RegisteredCommand cmd = new RegisteredCommand(this, cmdName, method, prefSubCommand);

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
    private static List<String> getSubCommandPossibilityList(String[] subCommandParts) {
        int i = 0;
        ArrayList<String> current = null;
        while (true) {
            ArrayList<String> newList = new ArrayList<>();

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

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        commandLabel = commandLabel.toLowerCase();

        execSubcommand = null;
        execLabel = commandLabel;
        origArgs = args;

        if (args.length == 0) {
            if (preCommand(sender, commandLabel, args)) {
                return true;
            }
            executeSubcommand(DEFAULT, sender);
            return true;
        }

        CommandSearch cmd = findSubCommand(args);
        if (cmd != null) {
            execSubcommand = cmd.getCheckSub();
            final String[] execargs = Arrays.copyOfRange(args, cmd.argIndex, args.length);
            if (preCommand(sender, commandLabel, execargs)) {
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
            String checkSub = StringUtils.join(args, " ", 0, i).toLowerCase();
            Set<RegisteredCommand> cmds = subCommands.get(checkSub);

            final int extraArgs = args.length - i;
            if (!cmds.isEmpty()) {
                RegisteredCommand cmd = null;
                if (cmds.size() == 1) {
                    cmd = Iterables.getOnlyElement(cmds);
                } else {
                    Optional<RegisteredCommand> optCmd = cmds.stream().filter(c -> {
                        int nonSender = c.nonSenderAwareResolvers;
                        int partialSender = c.optionalResolvers;
                        return extraArgs <= nonSender + partialSender && (completion || extraArgs >= nonSender);
                    }).sorted((c1, c2) -> {
                        int a = c1.nonSenderAwareResolvers + c1.optionalResolvers;
                        int b = c2.nonSenderAwareResolvers + c2.optionalResolvers;

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

    private static void executeCommand(CommandSender sender, String[] args, RegisteredCommand cmd) {
        if (cmd.hasPermission(sender)) {
            List<String> sargs = Lists.newArrayList(args);
            try (MCTiming timing = cmd.getTiming().startTiming()) {
                cmd.invoke(sender, sargs);
            }
        } else {
            ACFUtil.sendMsg(sender, "&cI'm sorry, but you do not have permission to perform this command.");
        }
    }

    public boolean canExecute(CommandSender sender, RegisteredCommand cmd) {
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String commandLabel, String[] args)
        throws IllegalArgumentException {

        commandLabel = commandLabel.toLowerCase();

        final CommandSearch search = findSubCommand(args, true);

        String argString = StringUtils.join(args, " ").toLowerCase();

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

    private List<String> completeCommand(CommandSender sender, RegisteredCommand cmd, String[] args, String commandLabel) {
        if (args.length > cmd.nonSenderAwareResolvers + cmd.optionalResolvers) {
            return ImmutableList.of();
        }
        if (args.length == 0 || cmd.complete == null) {
            return args.length < 2 ? super.tabComplete(sender, commandLabel, args) : ImmutableList.of();
        }

        String[] completions = ACFPatterns.SPACE.split(cmd.complete.value());

        List<String> cmds = manager.getCommandCompletions().of(cmd, sender, completions, args);
        return filterTabComplete(args[args.length-1], cmds);
    }

    private static List<String> filterTabComplete(String arg, List<String> cmds) {
        return cmds.stream()
                   .distinct()
                   .filter(cmd -> cmd != null && (arg.isEmpty() || StringUtil.startsWithIgnoreCase(cmd, arg)))
                   .collect(Collectors.toList());
    }

    public void help(CommandSender sender, String[] args) {
        ACFUtil.sendMsg(sender, "&cUnknown Command, please type &f/help");
    }

    private boolean executeSubcommand(String subcommand, CommandSender sender, String... args) {
        final Set<RegisteredCommand> defs = subCommands.get(subcommand);
        RegisteredCommand def = null;
        if (!defs.isEmpty()) {
            if (defs.size() == 1) {
                def = Iterables.getOnlyElement(defs);
            }
            if (def != null) {
                executeCommand(sender, args, def);
                return true;
            }
        }
        return false;
    }

    public boolean preCommand(CommandSender sender, String commandLabel, String[] args) {
        return false;
    }

    public void doHelp(CommandSender sender, String... args) {
        help(sender, args);
    }

    public void showSyntax(CommandSender sender, RegisteredCommand cmd) {
        ACFUtil.sendMsg(sender, "&cUsage: /" + cmd.command + " " + cmd.syntaxText);
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
