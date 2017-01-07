/*
 * Copyright (c) 2016 Daniel Ennis (Aikar) - MIT License
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
import org.bukkit.entity.EntityType;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public final class CommandCompletions {
    private CommandCompletions() {}

    private static final List<String> EMPTY = ImmutableList.of();
    public static List<String> of(CommandSender sender,  String completion, String input) {
        if (completion == null) {
            return ImmutableList.of();
        }
        String[] complete = CommandPatterns.COLON.split(completion, 2);

        switch (complete[0]) {
            case "@range":
                if (complete.length == 1) {
                    return ImmutableList.of();
                }
                final String[] ranges = CommandPatterns.DASH.split(complete[1]);
                int start;
                int end;
                if (ranges.length != 2) {
                    start = 0;
                    end = CommandUtil.parseInt(ranges[0], 0);
                } else {
                    start = CommandUtil.parseInt(ranges[0], 0);
                    end = CommandUtil.parseInt(ranges[1], 0);
                }
                return IntStream.rangeClosed(start, end).mapToObj(Integer::toString).collect(Collectors.toList());
            case "@timeunits":
                return ImmutableList.of("hours", "days", "weeks", "months", "minutes");

            case "@mobs":
                final Stream<String> normal = Stream.of(EntityType.values())
                                              .map(entityType -> CommandUtil.simplifyString(entityType.getName()));
                return normal.collect(Collectors.toList());
            default:
                return Lists.newArrayList(CommandPatterns.PIPE.split(completion));
        }
    }
}
