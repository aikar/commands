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

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"WeakerAccess", "unchecked"})
public class CommandExecutionContext<CEC extends CommandExecutionContext, I extends CommandIssuer> {
    private final RegisteredCommand cmd;
    private final CommandParameter param;
    protected final I issuer;
    private final List<String> args;
    private final int index;
    private final Map<String, Object> passedArgs;
    private final Map<String, String> flags;
    private final CommandManager manager;

    CommandExecutionContext(RegisteredCommand cmd, CommandParameter param, I sender, List<String> args,
                            int index, Map<String, Object> passedArgs) {
        this.cmd = cmd;
        this.manager = cmd.scope.manager;
        this.param = param;
        this.issuer = sender;
        this.args = args;
        this.index = index;
        this.passedArgs = passedArgs;
        this.flags = param.getFlags();

    }

    public String popFirstArg() {
        return !args.isEmpty() ? args.remove(0) : null;
    }

    public String popLastArg() {
        return !args.isEmpty() ? args.remove(args.size() - 1) : null;
    }

    public String getFirstArg() {
        return !args.isEmpty() ? args.get(0) : null;
    }

    public String getLastArg() {
        return !args.isEmpty() ? args.get(args.size() - 1) : null;
    }

    public boolean isLastArg() {
        return cmd.parameters.length - 1 == index;
    }

    public int getNumParams() {
        return cmd.parameters.length;
    }

    public boolean canOverridePlayerContext() {
        return cmd.requiredResolvers >= args.size();
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
                return (T) o;
            }
        }

        return null;
    }

    public Set<String> getParameterPermissions() {
        return param.getRequiredPermissions();
    }

    public boolean isOptional() {
        return param.isOptional();
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

    public Long getFlagValue(String flag, Long def) {
        return ACFUtil.parseLong(this.flags.get(flag), def);
    }

    public Float getFlagValue(String flag, Float def) {
        return ACFUtil.parseFloat(this.flags.get(flag), def);
    }

    public Double getFlagValue(String flag, Double def) {
        return ACFUtil.parseDouble(this.flags.get(flag), def);
    }

    public Integer getIntFlagValue(String flag, Number def) {
        return ACFUtil.parseInt(this.flags.get(flag), def != null ? def.intValue() : null);
    }

    public Long getLongFlagValue(String flag, Number def) {
        return ACFUtil.parseLong(this.flags.get(flag), def != null ? def.longValue() : null);
    }

    public Float getFloatFlagValue(String flag, Number def) {
        return ACFUtil.parseFloat(this.flags.get(flag), def != null ? def.floatValue() : null);
    }

    public Double getDoubleFlagValue(String flag, Number def) {
        return ACFUtil.parseDouble(this.flags.get(flag), def != null ? def.doubleValue() : null);
    }

    public Boolean getBooleanFlagValue(String flag) {
        return getBooleanFlagValue(flag, false);
    }

    public Boolean getBooleanFlagValue(String flag, Boolean def) {
        String val = this.flags.get(flag);
        if (val == null) {
            return def;
        }
        return ACFUtil.isTruthy(val);
    }

    public Double getFlagValue(String flag, Number def) {
        return ACFUtil.parseDouble(this.flags.get(flag), def != null ? def.doubleValue() : null);
    }

    /**
     * This method will not support annotation processors!! use getAnnotationValue or hasAnnotation
     *
     * @deprecated Use {@link #getAnnotationValue(Class)}
     */
    @Deprecated
    public <T extends Annotation> T getAnnotation(Class<T> cls) {
        return param.getParameter().getAnnotation(cls);
    }

    public <T extends Annotation> String getAnnotationValue(Class<T> cls) {
        return manager.getAnnotations().getAnnotationValue(param.getParameter(), cls);
    }

    public <T extends Annotation> String getAnnotationValue(Class<T> cls, int options) {
        return manager.getAnnotations().getAnnotationValue(param.getParameter(), cls, options);
    }

    public <T extends Annotation> boolean hasAnnotation(Class<T> cls) {
        return manager.getAnnotations().hasAnnotation(param.getParameter(), cls);
    }

    public RegisteredCommand getCmd() {
        return this.cmd;
    }

    @UnstableAPI
    CommandParameter getCommandParameter() {
        return this.param;
    }

    @Deprecated
    public Parameter getParam() {
        return this.param.getParameter();
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
