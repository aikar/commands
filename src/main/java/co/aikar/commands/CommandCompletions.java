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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public class CommandCompletions {
    private Map<String, CommandCompletionHandler> completionMap = new HashMap<>();

    public CommandCompletions() {
        registerCompletion("range", (sender, completionConfig, input) -> {
            if (completionConfig == null) {
                return ImmutableList.of();
            }
            final String[] ranges = ACFPatterns.DASH.split(completionConfig);
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
        registerCompletion("timeunits", (sender, completionConfig, input) -> ImmutableList.of("minutes", "hours", "days", "weeks", "months", "years"));
    }

    public CommandCompletionHandler registerCompletion(String id, CommandCompletionHandler handler) {
        return this.completionMap.put("@" + id.toLowerCase(), handler);
    }

    public List<String> of(CommandSender sender, String completion, String input) {
        if (completion == null) {
            return ImmutableList.of();
        }
        if (input == null) {
            input = "";
        }

        String[] complete = ACFPatterns.COLON.split(completion, 2);

        CommandCompletionHandler handler = this.completionMap.get(complete[0].toLowerCase());
        if (handler != null) {
            List<String> completions = handler.getCompletions(sender, complete.length == 1 ? null : complete[1], input);
            if (completions == null) {
                return ImmutableList.of();
            } else {
                return completions;
            }
        }
        return Lists.newArrayList(ACFPatterns.PIPE.split(completion));
    }

    public interface CommandCompletionHandler {
        List<String> getCompletions(CommandSender sender, String config, String input);
    }
}
