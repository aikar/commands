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

import co.aikar.commands.annotation.Conditions;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

public class ParameterConditionContext <P, CEC extends CommandExecutionContext, I extends CommandIssuer> extends ConditionContext<I> {

    private final CEC execContext;

    ParameterConditionContext(RegisteredCommand cmd, I issuer, CEC execContext, Conditions conditions) {
        super(cmd, issuer, conditions);

        this.execContext = execContext;
    }

    public boolean isLastArg() {
        return execContext.isLastArg();
    }

    public int getNumParams() {
        return execContext.getNumParams();
    }

    public boolean canOverridePlayerContext() {
        return execContext.canOverridePlayerContext();
    }

    public Object getResolvedArg(String arg) {
        return execContext.getResolvedArg(arg);
    }

    public Object getResolvedArg(Class[] classes) {
        return execContext.getResolvedArg(classes);
    }

    public Object getResolvedArg(String key, Class[] classes) {
        return execContext.getResolvedArg(key, classes);
    }

    public boolean isOptional() {
        return execContext.isOptional();
    }

    public Annotation getAnnotation(Class cls) {
        return execContext.getAnnotation(cls);
    }

    public boolean hasAnnotation(Class cls) {
        return execContext.hasAnnotation(cls);
    }

    public List<String> getArgs() {
        return execContext.getArgs();
    }

    public int getIndex() {
        return execContext.getIndex();
    }

    public Map<String, Object> getPassedArgs() {
        return execContext.getPassedArgs();
    }

    public String joinArgs() {
        return execContext.joinArgs();
    }

    public String joinArgs(String sep) {
        return execContext.joinArgs(sep);
    }
}
