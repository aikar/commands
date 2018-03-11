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
import co.aikar.commands.contexts.IssuerAwareContextResolver;
import co.aikar.commands.contexts.IssuerOnlyContextResolver;
import co.aikar.commands.contexts.OptionalContextResolver;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class CommandContexts <R extends CommandExecutionContext<?, ? extends CommandIssuer>> {
    protected final Map<Class<?>, ContextResolver<?, R>> contextMap = Maps.newHashMap();
    protected final CommandManager manager;

    CommandContexts(CommandManager manager) {
        this.manager = manager;
        registerContext(Short.class, (c) -> {
            try {
                return parseAndValidateNumber(c, Short.MAX_VALUE).shortValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", c.getFirstArg());
            }
        });
        registerContext(short.class, (c) -> {
            try {
                return parseAndValidateNumber(c, Short.MAX_VALUE).shortValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", c.getFirstArg());
            }
        });
        registerContext(Integer.class, (c) -> {
            try {
                return parseAndValidateNumber(c, Integer.MAX_VALUE).intValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", c.getFirstArg());
            }
        });
        registerContext(int.class, (c) -> {
            try {
                return parseAndValidateNumber(c, Integer.MAX_VALUE).intValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", c.getFirstArg());
            }
        });
        registerContext(Long.class, (c) -> {
            try {
                return parseAndValidateNumber(c, Long.MAX_VALUE).longValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", c.getFirstArg());
            }
        });
        registerContext(long.class, (c) -> {
            try {
                return parseAndValidateNumber(c, Long.MAX_VALUE).longValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", c.getFirstArg());
            }
        });
        registerContext(Float.class, (c) -> {
            try {
                return parseAndValidateNumber(c, Float.MAX_VALUE).floatValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", c.getFirstArg());
            }
        });
        registerContext(float.class, (c) -> {
            try {
                return parseAndValidateNumber(c, Float.MAX_VALUE).floatValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", c.getFirstArg());
            }
        });
        registerContext(Double.class, (c) -> {
            try {
                return parseAndValidateNumber(c, Double.MAX_VALUE).doubleValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", c.getFirstArg());
            }
        });
        registerContext(double.class, (c) -> {
            try {
                return parseAndValidateNumber(c, Double.MAX_VALUE).doubleValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", c.getFirstArg());
            }
        });
        registerContext(Number.class, (c) -> {
            try {
                return parseAndValidateNumber(c, Double.MAX_VALUE);
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", c.getFirstArg());
            }
        });
        registerContext(BigDecimal.class, (c) -> {
            try {
                BigDecimal number = ACFUtil.parseBigNumber(c.popFirstArg(), c.hasFlag("suffixes"));
                validateMinMax(c, number, null);
                return number;
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", c.getFirstArg());
            }
        });
        registerContext(BigInteger.class, (c) -> {
            try {
                BigDecimal number = ACFUtil.parseBigNumber(c.popFirstArg(), c.hasFlag("suffixes"));
                validateMinMax(c, number, null);
                return number.toBigIntegerExact();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", c.getFirstArg());
            }
        });
        registerContext(Boolean.class, (c) -> ACFUtil.isTruthy(c.popFirstArg()));
        registerContext(boolean.class, (c) -> ACFUtil.isTruthy(c.popFirstArg()));
        registerContext(char.class, c -> {
            String s = c.popFirstArg();
            if (s.length() > 1) {
                throw new InvalidCommandArgument(MessageKeys.MUST_BE_MAX_LENGTH, "{max}", String.valueOf(1));
            }
            return s.charAt(0);
        });
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
                    throw new InvalidCommandArgument(MessageKeys.MUST_BE_MIN_LENGTH, "{min}", String.valueOf(minLen));
                }
            }
            if (maxLen != null) {
                if (ret.length() > maxLen) {
                    throw new InvalidCommandArgument(MessageKeys.MUST_BE_MAX_LENGTH, "{max}", String.valueOf(maxLen));
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
                throw new InvalidCommandArgument(MessageKeys.PLEASE_SPECIFY_ONE_OF, "{valid}", ACFUtil.join(names));
            }
            return match;
        });
        registerOptionalContext(CommandHelp.class, (c) -> {
            String first = c.getFirstArg();
            String last = c.getLastArg();
            int page = 1;
            List<String> search = null;
            if (last != null && ACFUtil.isInteger(last)) {
                c.popLastArg();
                page = ACFUtil.parseInt(last);
                if (!c.getArgs().isEmpty()) {
                    search = c.getArgs();
                }
            } else if (first != null && ACFUtil.isInteger(first)) {
                c.popFirstArg();
                page = ACFUtil.parseInt(first);
                if (!c.getArgs().isEmpty()) {
                    search = c.getArgs();
                }
            } else if (!c.getArgs().isEmpty()) {
                search = c.getArgs();
            }
            CommandHelp commandHelp = manager.generateCommandHelp();
            commandHelp.setPage(page);
            Integer perPage = c.getFlagValue("perpage", (Integer) null);
            if (perPage != null) {
                commandHelp.setPerPage(perPage);
            }
            commandHelp.setSearch(search);
            return commandHelp;
        });
    }

    @NotNull
    private Number parseAndValidateNumber(R c, Number maxValue) throws InvalidCommandArgument {
        Number val = ACFUtil.parseNumber(c.getFirstArg(), c.hasFlag("suffixes"));
        validateMinMax(c, val, maxValue);
        c.popFirstArg(); // pop later so we can have a chance to display a nicer error message
        return val;
    }

    private void validateMinMax(R c, Number val, Number maxValue) throws InvalidCommandArgument {
        Number minValue = c.getFlagValue("min", (Integer) null);
        maxValue = c.getFlagValue("max", maxValue != null ? maxValue.intValue() : null);
        if (maxValue != null && val.doubleValue() > maxValue.doubleValue()) {
            throw new InvalidCommandArgument(MessageKeys.PLEASE_SPECIFY_AT_MOST, "{max}", String.valueOf(maxValue));
        }
        if (minValue != null && val.doubleValue() < minValue.doubleValue()) {
            throw new InvalidCommandArgument(MessageKeys.PLEASE_SPECIFY_AT_LEAST, "{min}", String.valueOf(minValue));
        }
    }


    /**
     * @deprecated Please switch to {@link #registerIssuerAwareContext(Class, IssuerAwareContextResolver)}
     * as the core wants to use the platform agnostic term of "Issuer" instead of Sender
     * @see #registerIssuerAwareContext(Class, IssuerAwareContextResolver)
     */
    @Deprecated
    public <T> void registerSenderAwareContext(Class<T> context, IssuerAwareContextResolver<T, R> supplier) {
        contextMap.put(context, supplier);
    }

    /**
     * Registers a context resolver that may conditionally consume input, falling back to using the context of the
     * issuer to potentially fulfill this context.
     * You may call {@link CommandExecutionContext#getFirstArg()} and then conditionally call {@link CommandExecutionContext#popFirstArg()}
     * if you want to consume that input.
     */
    public <T> void registerIssuerAwareContext(Class<T> context, IssuerAwareContextResolver<T, R> supplier) {
        contextMap.put(context, supplier);
    }

    /**
     * Registers a context resolver that will never consume input. It will always satisfy its context based on the
     * issuer of the command, so it will not appear in syntax strings.
     */
    public <T> void registerIssuerOnlyContext(Class<T> context, IssuerOnlyContextResolver<T, R> supplier) {
        contextMap.put(context, supplier);
    }

    /**
     * Registers a context that can safely accept a null input from the command issuer to resolve. This resolver should always
     * call {@link CommandExecutionContext#popFirstArg()}
     */
    public <T> void registerOptionalContext(Class<T> context, OptionalContextResolver<T, R> supplier) {
        contextMap.put(context, supplier);
    }

    /**
     * Registers a context that requires input from the command issuer to resolve. This resolver should always
     * call {@link CommandExecutionContext#popFirstArg()}
     */
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
