/*
 * Copyright (c) 2016. Starlis LLC / dba Empire Minecraft
 *
 * This source code is proprietary software and must not be redistributed without Starlis LLC's approval
 *
 */

package co.aikar.commands;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ForwardingCommand extends Command {
    private final Command command;
    private final String[] baseArgs;
    private static final String[] NO_ARGS =  new String[0];

    public ForwardingCommand(Command command) {
        this(command, NO_ARGS);
    }

    public ForwardingCommand(Command command, String[] baseArgs) {
        super(command.getName());
        this.command = command;
        this.baseArgs = baseArgs;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        return command.tabComplete(sender, alias, (String[]) ArrayUtils.addAll(baseArgs, args));
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        return command.execute(sender, commandLabel, (String[]) ArrayUtils.addAll(baseArgs, args));
    }
}
