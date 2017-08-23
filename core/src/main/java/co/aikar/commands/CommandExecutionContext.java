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

import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.contexts.ContextResolver;
import co.aikar.commands.contexts.IssuerAwareContextResolver;
import co.aikar.commands.contexts.IssuerOnlyContextResolver;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"WeakerAccess", "unused"})
public class CommandExecutionContext <T extends CommandExecutionContext, I extends CommandIssuer> {
    private final RegisteredCommand cmd;
    private final Parameter param;
    protected final I issuer;
    private final List<String> args;
    private final int index;
    private final Map<String, Object> passedArgs;
    private final Map<String, String> flags;

    CommandExecutionContext(RegisteredCommand cmd, Parameter param, I sender, List<String> args,
                                   int index, Map<String, Object> passedArgs) {
        this.cmd = cmd;
        this.param = param;
        this.issuer = sender;
        this.args = args;
        this.index = index;
        this.passedArgs = passedArgs;
        Flags flags = param.getAnnotation(Flags.class);
        if (flags != null) {
            this.flags = Maps.newHashMap();
            for (String s : ACFPatterns.COMMA.split(cmd.scope.manager.getCommandReplacements().replace(flags.value()))) {
                String[] v = ACFPatterns.EQUALS.split(s, 2);
                this.flags.put(v[0], v.length > 1 ? v[1] : null);
            }
        } else {
            this.flags = ImmutableMap.of();
        }
    }

    public String popFirstArg() {
        return !args.isEmpty() ? args.remove(0) : null;
    }

    public String getFirstArg() {
        return !args.isEmpty() ? args.get(0) : null;
    }

    public boolean isLastArg() {
        return cmd.parameters.length -1 == index;
    }

    public int getNumParams() {
        return cmd.parameters.length;
    }

    public boolean canOverridePlayerContext() {
        int numRequired = getNumParams();
        for (int i = 0; i < cmd.resolvers.length; i++) {
            Parameter parameter = cmd.parameters[i];
            //noinspection unchecked
            ContextResolver<?, ?> resolver = cmd.resolvers[i];
            if (parameter.getAnnotation(Optional.class) != null || parameter.getAnnotation(Default.class) != null) {
                numRequired--;
            } else if (resolver instanceof IssuerAwareContextResolver || resolver instanceof IssuerOnlyContextResolver) {
                numRequired--;
            }
        }

        return numRequired >= args.size();
    }

    public Object getResolvedArg(String arg) {
        return passedArgs.get(arg);
    }

    public Object getResolvedArg(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            for (Object passedArg : passedArgs.values()) {
                if (clazz.isInstance(passedArg)) {
                    return passedArg;
                }
            }
        }

        return null;
    }

    public <T> T getResolvedArg(String key, Class<?>... classes) {
        final Object o = passedArgs.get(key);
        for (Class<?> clazz : classes) {
            if (clazz.isInstance(o)) {
                //noinspection unchecked
                return (T) o;
            }
        }

        return null;
    }

    public boolean isOptional() {
        return param.getAnnotation(Optional.class) != null;
    }
    public boolean hasFlag(String flag) {
        return flags.containsKey(flag);
    }

    public String getFlagValue(String flag, String def) {
        return flags.getOrDefault(flag, def);
    }

    public Integer getFlagValue(String flag, Integer def) {
        return ACFUtil.parseInt(this.flags.get(flag), def);
    }

    public <T extends Annotation> T getAnnotation(Class<T> cls) {
        return param.getAnnotation(cls);
    }

    public <T extends Annotation> boolean hasAnnotation(Class<T> cls) {
        return param.getAnnotation(cls) != null;
    }

    public RegisteredCommand getCmd() {
        return this.cmd;
    }

    public Parameter getParam() {
        return this.param;
    }

    public I getIssuer() {
        return this.issuer;
    }

    public List<String> getArgs() {
        return this.args;
    }

    public int getIndex() {
        return this.index;
    }

    public Map<String, Object> getPassedArgs() {
        return this.passedArgs;
    }

    public Map<String, String> getFlags() {
        return this.flags;
    }

    public String joinArgs() {
        return ACFUtil.join(args, " ");
    }
    public String joinArgs(String sep) {
        return ACFUtil.join(args, sep);
    }
}
