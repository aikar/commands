/*
 * Copyright (c) 2016-2018 Daniel Ennis (Aikar) - MIT License
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

package co.aikar.commands.flags;

import co.aikar.commands.CommandExecutionContext;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.CommandManager;
import co.aikar.commands.LogLevel;
import com.google.common.collect.Maps;

import java.util.Map;

public class CommandFlags<R extends CommandExecutionContext<?, ? extends CommandIssuer>> {

    protected final CommandManager manager;

    protected Map<String, CommandFlagType<?, ?>> flags = Maps.newHashMap();

    protected Map<String, CommandFlagResolver<CommandFlag<?, ?>, R>> resolvers = Maps.newHashMap();

    public CommandFlags(CommandManager manager) {
        this.manager = manager;
    }

    public <V> void registerFlag(String flag,
                                 CommandFlagType<?, V> flagType,
                                 CommandFlagResolver<V, R> resolver) {
        this.flags.put(flag.toLowerCase(), flagType);
        this.resolvers.put(flag.toLowerCase(), c -> flagType.create(resolver.get(c)));
    }

    public <V> CommandFlagType<?, V> getType(String flag) {
        flag = flag.toLowerCase();

        if (this.flags.containsKey(flag))
            return (CommandFlagType<?, V>) this.flags.get(flag);

        this.manager.log(LogLevel.ERROR, "Could not find flag type",
                new IllegalStateException("No flag type defined for " + flag));
        return null;
    }

    public CommandFlagResolver<?, R> getResolver(String flag) {
        flag = flag.toLowerCase();

        if (this.resolvers.containsKey(flag))
            return this.resolvers.get(flag);

        this.manager.log(LogLevel.ERROR, "Could not find flag resolver",
                new IllegalStateException("No flag resolver defined for " + flag));
        return null;
    }

}
