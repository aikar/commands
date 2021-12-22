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
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class CommandContexts<R extends CommandExecutionContext<?, ? extends CommandIssuer>> {
    protected final Map<Class<?>, ContextResolver<?, R>> contextMap = new HashMap<>();
    protected final CommandManager manager;

    CommandContexts(CommandManager manager) {
        this.manager = manager;
        registerIssuerOnlyContext(CommandIssuer.class, c -> c.getIssuer());
        registerContext(Short.class, (c) -> {
            String number = c.popFirstArg();
            try {
                return parseAndValidateNumber(number, c, Short.MIN_VALUE, Short.MAX_VALUE).shortValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", number);
            }
        });
        registerContext(short.class, (c) -> {
            String number = c.popFirstArg();
            try {
                return parseAndValidateNumber(number, c, Short.MIN_VALUE, Short.MAX_VALUE).shortValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", number);
            }
        });
        registerContext(Integer.class, (c) -> {
            String number = c.popFirstArg();
            try {
                return parseAndValidateNumber(number, c, Integer.MIN_VALUE, Integer.MAX_VALUE).intValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", number);
            }
        });
        registerContext(int.class, (c) -> {
            String number = c.popFirstArg();
            try {
                return parseAndValidateNumber(number, c, Integer.MIN_VALUE, Integer.MAX_VALUE).intValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", number);
            }
        });
        registerContext(Long.class, (c) -> {
            String number = c.popFirstArg();
            try {
                return parseAndValidateNumber(number, c, Long.MIN_VALUE, Long.MAX_VALUE).longValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", number);
            }
        });
        registerContext(long.class, (c) -> {
            String number = c.popFirstArg();
            try {
                return parseAndValidateNumber(number, c, Long.MIN_VALUE, Long.MAX_VALUE).longValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", number);
            }
        });
        registerContext(Float.class, (c) -> {
            String number = c.popFirstArg();
            try {
                return parseAndValidateNumber(number, c, -Float.MAX_VALUE, Float.MAX_VALUE).floatValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", number);
            }
        });
        registerContext(float.class, (c) -> {
            String number = c.popFirstArg();
            try {
                return parseAndValidateNumber(number, c, -Float.MAX_VALUE, Float.MAX_VALUE).floatValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", number);
            }
        });
        registerContext(Double.class, (c) -> {
            String number = c.popFirstArg();
            try {
                return parseAndValidateNumber(number, c, -Double.MAX_VALUE, Double.MAX_VALUE).doubleValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", number);
            }
        });
        registerContext(double.class, (c) -> {
            String number = c.popFirstArg();
            try {
                return parseAndValidateNumber(number, c, -Double.MAX_VALUE, Double.MAX_VALUE).doubleValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", number);
            }
        });
        registerContext(Number.class, (c) -> {
            String number = c.popFirstArg();
            try {
                return parseAndValidateNumber(number, c, -Double.MAX_VALUE, Double.MAX_VALUE);
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", number);
            }
        });
        registerContext(BigDecimal.class, (c) -> {
            String numberStr = c.popFirstArg();
            try {
                BigDecimal number = ACFUtil.parseBigNumber(numberStr, c.hasFlag("suffixes"));
                validateMinMax(c, number);
                return number;
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", numberStr);
            }
        });
        registerContext(BigInteger.class, (c) -> {
            String numberStr = c.popFirstArg();
            try {
                BigDecimal number = ACFUtil.parseBigNumber(numberStr, c.hasFlag("suffixes"));
                validateMinMax(c, number);
                return number.toBigIntegerExact();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", numberStr);
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
            // This will fail fast, it's either in the values or it's not
            if (c.hasAnnotation(Values.class)) {
                return c.popFirstArg();
            }
            String ret = (c.isLastArg() && !c.hasAnnotation(Single.class)) ?
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
            List<String> args = c.getArgs();
            if (c.isLastArg() && !c.hasAnnotation(Single.class)) {
                val = ACFUtil.join(args);
            } else {
                val = c.popFirstArg();
            }
            String split = c.getAnnotationValue(Split.class, Annotations.NOTHING | Annotations.NO_EMPTY);
            if (split != null) {
                if (val.isEmpty()) {
                    throw new InvalidCommandArgument();
                }
                return ACFPatterns.getPattern(split).split(val);
            } else if (!c.isLastArg()) {
                ACFUtil.sneaky(new IllegalStateException("Weird Command signature... String[] should be last or @Split"));
            }

            String[] result = args.toArray(new String[0]);
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
                throw new InvalidCommandArgument(MessageKeys.PLEASE_SPECIFY_ONE_OF, "{valid}", ACFUtil.join(names, ", "));
            }
            return match;
        });
        registerOptionalContext(CommandHelp.class, (c) -> {
            String first = c.getFirstArg();
            String last = c.getLastArg();
            Integer page = 1;
            List<String> search = null;
            if (last != null && ACFUtil.isInteger(last)) {
                c.popLastArg();
                page = ACFUtil.parseInt(last);
                if (page == null) {
                    throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", last);
                }
                if (!c.getArgs().isEmpty()) {
                    search = c.getArgs();
                }
            } else if (first != null && ACFUtil.isInteger(first)) {
                c.popFirstArg();
                page = ACFUtil.parseInt(first);
                if (page == null) {
                    throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", first);
                }
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

            // check if we have an exact match and should display the help page for that sub command instead
            if (search != null) {
                String cmd = String.join(" ", search);
                if (commandHelp.testExactMatch(cmd)) {
                    return commandHelp;
                }
            }

            commandHelp.setSearch(search);
            return commandHelp;
        });
    }

    @NotNull
    private Number parseAndValidateNumber(String number, R c, Number minValue, Number maxValue) throws InvalidCommandArgument {
        final Number val = ACFUtil.parseNumber(number, c.hasFlag("suffixes"));
        validateMinMax(c, val, minValue, maxValue);
        return val;
    }

    private void validateMinMax(R c, Number val) throws InvalidCommandArgument {
        validateMinMax(c, val, null, null);
    }

    private void validateMinMax(R c, Number val, Number minValue, Number maxValue) throws InvalidCommandArgument {
        minValue = c.getFlagValue("min", minValue);
        maxValue = c.getFlagValue("max", maxValue);
        if (maxValue != null && val.doubleValue() > maxValue.doubleValue()) {
            throw new InvalidCommandArgument(MessageKeys.PLEASE_SPECIFY_AT_MOST, "{max}", String.valueOf(maxValue));
        }
        if (minValue != null && val.doubleValue() < minValue.doubleValue()) {
            throw new InvalidCommandArgument(MessageKeys.PLEASE_SPECIFY_AT_LEAST, "{min}", String.valueOf(minValue));
        }
    }


    /**
     * @see #registerIssuerAwareContext(Class, IssuerAwareContextResolver)
     * @deprecated Please switch to {@link #registerIssuerAwareContext(Class, IssuerAwareContextResolver)}
     * as the core wants to use the platform-agnostic term of "Issuer" instead of Sender
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
