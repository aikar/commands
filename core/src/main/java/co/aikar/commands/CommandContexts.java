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

import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Split;
import co.aikar.commands.annotation.Values;
import co.aikar.commands.contexts.ContextResolver;
import co.aikar.commands.contexts.SenderAwareContextResolver;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class CommandContexts <R extends CommandExecutionContext<?>> {
    protected final Map<Class<?>, ContextResolver<?, R>> contextMap = Maps.newHashMap();
    private final CommandManager manager;

    CommandContexts(CommandManager manager) {
        this.manager = manager;
        registerContext(Integer.class, (c) -> {
            try {
                return ACFUtil.parseNumber(c.popFirstArg(), c.hasFlag("suffixes")).intValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument("Must be a number");
            }
        });
        registerContext(Long.class, (c) -> {
            try {
                return ACFUtil.parseNumber(c.popFirstArg(), c.hasFlag("suffixes")).longValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument("Must be a number");
            }

        });
        registerContext(Float.class, (c) -> {
            try {
                return ACFUtil.parseNumber(c.popFirstArg(), c.hasFlag("suffixes")).floatValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument("Must be a number");
            }
        });
        registerContext(Double.class, (c) -> {
            try {
                return ACFUtil.parseNumber(c.popFirstArg(), c.hasFlag("suffixes")).doubleValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument("Must be a number");
            }
        });
        registerContext(Number.class, (c) -> {
            try {
                return ACFUtil.parseNumber(c.popFirstArg(), c.hasFlag("suffixes"));
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument("Must be a number");
            }
        });
        registerContext(Boolean.class, (c) -> ACFUtil.isTruthy(c.popFirstArg()));
        registerContext(String.class, (c) -> {
            final Values values = c.getParam().getAnnotation(Values.class);
            if (values != null) {
                return c.popFirstArg();
            }
            String ret = (c.isLastArg() && c.getParam().getAnnotation(Single.class) == null) ?
                ACFUtil.join(c.getArgs())
                :
                c.popFirstArg();

            Integer minLen = c.getFlagValue("minlen", (Integer) null);
            Integer maxLen = c.getFlagValue("maxlen", (Integer) null);
            if (minLen != null) {
                if (ret.length() < minLen) {
                    throw new InvalidCommandArgument("Must be at least " + minLen + " characters long");
                }
            }
            if (maxLen != null) {
                if (ret.length() > maxLen) {
                    throw new InvalidCommandArgument("Must be less " + maxLen + " characters long");
                }
            }

            return ret;
        });
        registerContext(String[].class, (c) -> {
            String val;
            // Go home IDEA, you're drunk
            //noinspection unchecked
            List<String> args = c.getArgs();
            if (c.isLastArg() && c.getParam().getAnnotation(Single.class) == null) {
                val = ACFUtil.join(args);
            } else {
                val = c.popFirstArg();
            }
            Split split = c.getParam().getAnnotation(Split.class);
            if (split != null) {
                if (val.isEmpty()) {
                    throw new InvalidCommandArgument();
                }
                return ACFPatterns.getPattern(split.value()).split(val);
            } else if (!c.isLastArg()) {
                ACFUtil.sneaky(new IllegalStateException("Weird Command signature... String[] should be last or @Split"));
            }

            String[] result = args.toArray(new String[args.size()]);
            args.clear();
            return result;
        });

        registerContext(Enum.class, (c) -> {
            final String first = c.popFirstArg();
            //noinspection unchecked
            Class<? extends Enum<?>> enumCls = (Class<? extends Enum<?>>) c.getParam().getType();
            Enum<?> match = ACFUtil.simpleMatch(enumCls, first);
            if (match == null) {
                List<String> names = ACFUtil.enumNames(enumCls);
                throw new InvalidCommandArgument("Please specify one of: " + ACFUtil.join(names));
            }
            return match;
        });
    }

    public <T> void registerSenderAwareContext(Class<T> context, SenderAwareContextResolver<T, R> supplier) {
        contextMap.put(context, supplier);
    }
    public <T> void registerContext(Class<T> context, ContextResolver<T, R> supplier) {
        contextMap.put(context, supplier);
    }

    public ContextResolver<?, R> getResolver(Class<?> type) {
        Class<?> rootType = type;
        do {
            if (type == Object.class) {
                break;
            }

            final ContextResolver<?, R> resolver = contextMap.get(type);
            if (resolver != null) {
                return resolver;
            }
        } while ((type = type.getSuperclass()) != null);

        this.manager.log(LogLevel.ERROR, "Could not find context resolver", new IllegalStateException("No context resolver defined for " + rootType.getName()));
        return null;
    }
}
