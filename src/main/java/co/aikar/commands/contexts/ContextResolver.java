/*
 * Copyright (c) 2016. Starlis LLC / dba Empire Minecraft
 *
 * This source code is proprietary software and must not be redistributed without Starlis LLC's approval
 *
 */

package co.aikar.commands.contexts;

import co.aikar.commands.InvalidCommandArgument;

@FunctionalInterface
public interface ContextResolver <C> {
    C getContext(CommandExecutionContext c) throws InvalidCommandArgument;
}
