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

import co.aikar.commands.apachecommonslang.ApacheCommonsLangUtil;
import com.google.common.collect.SetMultimap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static co.aikar.commands.BaseCommand.CATCHUNKNOWN;
import static co.aikar.commands.BaseCommand.DEFAULT;

public interface RootCommand {
    void addChild(BaseCommand command);

    CommandManager getManager();

    SetMultimap<String, RegisteredCommand> getSubCommands();

    List<BaseCommand> getChildren();

    String getCommandName();

    default void addChildShared(List<BaseCommand> children, SetMultimap<String, RegisteredCommand> subCommands, BaseCommand command) {
        command.subCommands.entries().forEach(e -> {
            String key = e.getKey();
            RegisteredCommand registeredCommand = e.getValue();
            if (key.equals(DEFAULT) || key.equals(BaseCommand.CATCHUNKNOWN)) {
                return;
            }
            Set<RegisteredCommand> registered = subCommands.get(key);
            if (!registered.isEmpty()) {
                BaseCommand prevBase = registered.iterator().next().scope;
                if (prevBase != registeredCommand.scope) {
                    this.getManager().log(LogLevel.ERROR, "ACF Error: " + command.getName() + " registered subcommand " + key + " for root command " + getCommandName() + " - but it is already defined in " + prevBase.getName());
                    this.getManager().log(LogLevel.ERROR, "2 subcommands of the same prefix may not be spread over 2 different classes. Ignoring this.");
                    return;
                }
            }
            subCommands.put(key, registeredCommand);
        });

        children.add(command);
    }

    /**
     * @return If this root command can be summarized to a single required permission node to use it, returns that value. If any RegisteredCommand is permission-less, or has multiple required permission nodes, null is returned.
     */
    default String getUniquePermission() {
        Set<String> permissions = new HashSet<>();
        for (BaseCommand child : getChildren()) {
            for (RegisteredCommand<?> value : child.subCommands.values()) {
                Set<String> requiredPermissions = value.getRequiredPermissions();
                if (requiredPermissions.isEmpty()) {
                    return null;
                } else {
                    permissions.addAll(requiredPermissions);
                }
            }
        }
        return permissions.size() == 1 ? permissions.iterator().next() : null;
    }

    default boolean hasAnyPermission(CommandIssuer issuer) {
        List<BaseCommand> children = getChildren();
        if (children.isEmpty()) {
            return true;
        }

        for (BaseCommand child : children) {
            if (!child.hasPermission(issuer)) {
                continue;
            }
            for (RegisteredCommand value : child.getRegisteredCommands()) {
                if (value.hasPermission(issuer)) {
                    return true;
                }
            }
        }
        return false;
    }

    default BaseCommand execute(CommandIssuer sender, String commandLabel, String[] args) {
        BaseCommand command = getBaseCommand(args);

        command.execute(sender, commandLabel, args);
        return command;
    }

    default BaseCommand getBaseCommand(String[] args) {
        SetMultimap<String, RegisteredCommand> subCommands = getSubCommands();
        RegisteredCommand command;
        for (int i = args.length; i >= 0; i--) {
            String checkSub = ApacheCommonsLangUtil.join(args, " ", 0, i).toLowerCase();
            command = ACFUtil.getFirstElement(subCommands.get(checkSub));
            if (command != null) {
                return command.scope;
            }
        }

        command = ACFUtil.getFirstElement(subCommands.get(DEFAULT));
        if (command != null) {
            if (args.length == 0 || command.consumeInputResolvers > 0) {
                return command.scope;
            }
        }

        command = ACFUtil.getFirstElement(subCommands.get(CATCHUNKNOWN));
        if (command != null) {
            return command.scope;
        }
        return getDefCommand();
    }

    default List<String> getTabCompletions(CommandIssuer sender, String alias, String[] args) {
        return getTabCompletions(sender, alias, args, false);
    }

    default List<String> getTabCompletions(CommandIssuer sender, String alias, String[] args, boolean commandsOnly) {
        return getTabCompletions(sender, alias, args, commandsOnly, false);
    }

    default List<String> getTabCompletions(CommandIssuer sender, String alias, String[] args, boolean commandsOnly, boolean isAsync) {
        Set<String> completions = new HashSet<>();
        getChildren().forEach(child -> {
            if (!commandsOnly) {
                completions.addAll(child.tabComplete(sender, alias, args, isAsync));
            }
            completions.addAll(child.getCommandsForCompletion(sender, args));
        });
        return new ArrayList<>(completions);
    }


    default RegisteredCommand getDefaultRegisteredCommand() {
        BaseCommand defCommand = this.getDefCommand();
        if (defCommand != null) {
            return defCommand.getDefaultRegisteredCommand();
        }
        return null;
    }

    default BaseCommand getDefCommand() {
        return null;
    }


    default String getDescription() {
        final RegisteredCommand cmd = this.getDefaultRegisteredCommand();
        if (cmd != null) {
            return cmd.getHelpText();
        }
        BaseCommand defCommand = getDefCommand();
        if (defCommand != null && defCommand.description != null) {
            return defCommand.description;
        }
        return "";
    }


    default String getUsage() {
        final RegisteredCommand cmd = this.getDefaultRegisteredCommand();
        if (cmd != null) {
            return cmd.syntaxText != null ? cmd.syntaxText : "";
        }
        return "";
    }
}
