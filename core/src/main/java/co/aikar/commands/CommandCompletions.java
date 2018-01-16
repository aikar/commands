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
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public class CommandCompletions <C extends CommandCompletionContext> {
    private final CommandManager manager;
    private Map<String, CommandCompletionHandler> completionMap = new HashMap<>();

    public CommandCompletions(CommandManager manager) {
        this.manager = manager;
        registerAsyncCompletion("nothing", c -> ImmutableList.of());
        registerAsyncCompletion("range", (c) -> {
            String config = c.getConfig();
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
        registerAsyncCompletion("timeunits", (c) -> ImmutableList.of("minutes", "hours", "days", "weeks", "months", "years"));
    }

    public CommandCompletionHandler registerCompletion(String id, CommandCompletionHandler<C> handler) {
        return this.completionMap.put("@" + id.toLowerCase(), handler);
    }

    public CommandCompletionHandler registerAsyncCompletion(String id, AsyncCommandCompletionHandler<C> handler) {
        return this.completionMap.put("@" + id.toLowerCase(), handler);
    }

    @NotNull
    List<String> of(RegisteredCommand command, CommandIssuer sender, String[] completionInfo, String[] args, boolean isAsync) {
        final int argIndex = args.length - 1;

        String input = args[argIndex];
        String completion = argIndex < completionInfo.length ? completionInfo[argIndex] : null;
        if (completion == null && completionInfo.length > 0) {
            completion = completionInfo[completionInfo.length - 1];
        }
        if (completion == null) {
            return ImmutableList.of(input);
        }

        return getCompletionValues(command, sender, completion, args, isAsync);
    }

    List<String> getCompletionValues(RegisteredCommand command, CommandIssuer sender, String completion, String[] args, boolean isAsync) {
        completion = manager.getCommandReplacements().replace(completion);

        List<String> allCompletions = Lists.newArrayList();
        String input = args.length > 0 ? args[args.length - 1] : "";

        for (String value : ACFPatterns.PIPE.split(completion)) {
            String[] complete = ACFPatterns.COLONEQUALS.split(value, 2);
            CommandCompletionHandler handler = this.completionMap.get(complete[0].toLowerCase());
            if (handler != null) {
                if (isAsync && !(handler instanceof AsyncCommandCompletionHandler)) {
                    ACFUtil.sneaky(new SyncCompletionRequired());
                    return null;
                }
                String config = complete.length == 1 ? null : complete[1];
                CommandCompletionContext context = manager.createCompletionContext(command, sender, input, config, args);

                try {
                    //noinspection unchecked
                    Collection<String> completions = handler.getCompletions(context);
                    if (completions != null) {
                        allCompletions.addAll(completions);
                        continue;
                    }
                    //noinspection ConstantIfStatement,ConstantConditions
                    if (false) { // Hack to fool compiler. since its sneakily thrown.
                        throw new CommandCompletionTextLookupException();
                    }
                } catch (CommandCompletionTextLookupException ignored) {
                    // This should only happen if some other feedback error occured.
                } catch (Exception e) {
                    command.handleException(sender, Lists.newArrayList(args), e);
                }
                // Something went wrong in lookup, fall back to input
                return ImmutableList.of(input);
            } else {
                // Plaintext value
                allCompletions.add(value);
            }
        }
        return allCompletions;
    }

    public interface CommandCompletionHandler <C extends CommandCompletionContext> {
        Collection<String> getCompletions(C context) throws InvalidCommandArgument;
    }
    public interface AsyncCommandCompletionHandler <C extends CommandCompletionContext> extends  CommandCompletionHandler <C> {}
    public static class SyncCompletionRequired extends Exception {}

}
