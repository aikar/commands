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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public class CommandCompletions {
    private Map<String, CommandCompletionHandler> completionMap = new HashMap<>();

    public CommandCompletions() {
        registerCompletion("range", (sender, config, input, c) -> {
            if (config == null) {
                return ImmutableList.of();
            }
            final String[] ranges = ACFPatterns.DASH.split(config);
            int start;
            int end;
            if (ranges.length != 2) {
                start = 0;
                end = ACFUtil.parseInt(ranges[0], 0);
            } else {
                start = ACFUtil.parseInt(ranges[0], 0);
                end = ACFUtil.parseInt(ranges[1], 0);
            }
            return IntStream.rangeClosed(start, end).mapToObj(Integer::toString).collect(Collectors.toList());
        });
        registerCompletion("timeunits", (sender, config, input, c) -> ImmutableList.of("minutes", "hours", "days", "weeks", "months", "years"));
    }

    public CommandCompletionHandler registerCompletion(String id, CommandCompletionHandler handler) {
        return this.completionMap.put("@" + id.toLowerCase(), handler);
    }

    @NotNull
    List<String> of(RegisteredCommand command, CommandSender sender, String[] completionInfo, String[] args) {
        final int argIndex = args.length - 1;

        String input = args[argIndex];
        final String completion = argIndex < completionInfo.length ? completionInfo[argIndex] : null;
        if (completion == null) {
            return ImmutableList.of(input);
        }

        return getCompletionValues(command, sender, completion, args);
    }

    @NotNull
    List<String> getCompletionValues(RegisteredCommand command, CommandSender sender, String completion, String[] args) {
        final int argIndex = args.length - 1;

        String input = args[argIndex];
        String[] complete = ACFPatterns.COLON.split(completion, 2);

        CommandCompletionHandler handler = this.completionMap.get(complete[0].toLowerCase());
        if (handler != null) {
            String config = complete.length == 1 ? null : complete[1];
            CommandCompletionContext context = new CommandCompletionContext(command, sender, input, config, args);

            try {
                Collection<String> completions = handler.getCompletions(sender, config, input, context);
                if (completions != null) {
                    return Lists.newArrayList(completions);
                }
                //noinspection ConstantIfStatement,ConstantConditions
                if (false) { // Hack to fool compiler. since its sneakily thrown.
                    throw new CommandCompletionTextLookupException();
                }
            } catch (CommandCompletionTextLookupException ignored) {
                // This should only happen if some other feedback error occured.
            } catch (Exception e) {
                ACFLog.exception(e);
            }
            // Something went wrong in lookup, fall back to input
            return ImmutableList.of(input);
        }
        // Plaintext values.
        return Lists.newArrayList(ACFPatterns.PIPE.split(completion));
    }

    public interface CommandCompletionHandler {
        Collection<String> getCompletions(CommandSender sender, String config, String input, CommandCompletionContext context) throws InvalidCommandArgument;
    }

    public class CommandCompletionContext {
        private final RegisteredCommand command;
        private final CommandSender sender;
        private final String input;
        private final String config;
        private final List<String> args;

        CommandCompletionContext(RegisteredCommand command, CommandSender sender, String input, String config, String[] args) {
            this.command = command;
            this.sender = sender;
            this.input = input;
            this.config = config;
            this.args = Lists.newArrayList(args);
        }

        public <T> T getContextValue(Class<? extends T> clazz) throws InvalidCommandArgument {
            return getContextValue(clazz, null);
        }

        public <T> T getContextValue(Class<? extends T> clazz, Integer paramIdx) throws InvalidCommandArgument {
            String name = null;
            if (paramIdx != null) {
                if (paramIdx >= command.parameters.length) {
                    throw new IllegalArgumentException("Param index is higher than number of parameters");
                }
                Parameter param = command.parameters[paramIdx];
                Class<?> paramType = param.getType();
                if (!clazz.isAssignableFrom(paramType)) {
                    throw new IllegalArgumentException(param.getName() +":" + paramType.getName() + " can not satisfy " + clazz.getName());
                }
                name = param.getName();
            } else {
                Parameter[] parameters = command.parameters;
                for (int i = 0; i < parameters.length; i++) {
                    Parameter param = parameters[i];
                    if (clazz.isAssignableFrom(param.getType())) {
                        paramIdx = i;
                        name = param.getName();
                        break;
                    }
                }
                if (paramIdx == null) {
                    throw new IllegalStateException("Can not find any parameter that can satisfy " + clazz.getName());
                }
            }
            Map<String, Object> resolved = command.resolveContexts(sender, args, args.size());
            if (resolved == null || paramIdx > resolved.size()) {
                ACFLog.error("resolved: " + resolved + " paramIdx: " + paramIdx + " - size: " + (resolved != null ? resolved.size() : null ));
                ACFUtil.sneaky(new CommandCompletionTextLookupException());
            }

            //noinspection unchecked
            return (T) resolved.get(name);
        }

        public CommandSender getSender() {
            return sender;
        }

        public String getInput() {
            return input;
        }

        public String getConfig() {
            return config;
        }
    }

    private class CommandCompletionTextLookupException extends Throwable {
    }
}
