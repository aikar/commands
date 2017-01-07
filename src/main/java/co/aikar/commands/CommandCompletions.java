/*
 * Copyright (c) 2016. Starlis LLC / dba Empire Minecraft
 *
 * This source code is proprietary software and must not be redistributed without Starlis LLC's approval
 *
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
