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

import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("WeakerAccess")
public class BukkitCommandCompletions extends CommandCompletions<BukkitCommandCompletionContext> {
    public BukkitCommandCompletions(BukkitCommandManager manager) {
        super(manager);
        registerAsyncCompletion("mobs", c -> {
            final Stream<String> normal = Stream.of(EntityType.values())
                    .map(entityType -> ACFUtil.simplifyString(entityType.getName()));
            return normal.collect(Collectors.toList());
        });
        registerAsyncCompletion("chatcolors", c -> {
            Stream<ChatColor> colors = Stream.of(ChatColor.values());
            if (c.hasConfig("colorsonly")) {
                colors = colors.filter(color -> color.ordinal() <= 0xF);
            }
            String filter = c.getConfig("filter");
            if (filter != null) {
                Set<String> filters = Arrays.stream(ACFPatterns.COLON.split(filter))
                        .map(ACFUtil::simplifyString).collect(Collectors.toSet());

                colors = colors.filter(color -> filters.contains(ACFUtil.simplifyString(color.name())));
            }

            return colors.map(color -> ACFUtil.simplifyString(color.name())).collect(Collectors.toList());
        });
        registerAsyncCompletion("dyecolors", c -> ACFUtil.enumNames(DyeColor.values()));
        registerCompletion("worlds", c -> (
                Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList())
        ));

        registerCompletion("players", c -> {
            CommandSender sender = c.getSender();
            Validate.notNull(sender, "Sender cannot be null");

            Player senderPlayer = sender instanceof Player ? (Player) sender : null;

            ArrayList<String> matchedPlayers = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                String name = player.getName();
                if ((senderPlayer == null || senderPlayer.canSee(player)) && StringUtil.startsWithIgnoreCase(name, c.getInput())) {
                    matchedPlayers.add(name);
                }
            }


            matchedPlayers.sort(String.CASE_INSENSITIVE_ORDER);
            return matchedPlayers;
        });

        setDefaultCompletion("players", OnlinePlayer.class, co.aikar.commands.contexts.OnlinePlayer.class, Player.class);
        setDefaultCompletion("worlds", World.class);
    }

}
