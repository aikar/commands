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


import co.aikar.commands.annotation.Optional;
import co.aikar.commands.contexts.OnlineProxiedPlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Williambraecky on 13-06-17.
 */
public class BungeeCommandContexts extends CommandContexts<BungeeCommandExecutionContext> {

    BungeeCommandContexts(CommandManager manager) {
        super(manager);
        registerContext(OnlineProxiedPlayer.class, (c) -> {
            final String playercheck = c.popFirstArg();
            ProxiedPlayer proxiedPlayer = ACFBungeeUtil.findPlayerSmart(c.getSender(), playercheck);
            if (proxiedPlayer == null) {
                if (c.hasAnnotation(Optional.class)) {
                    return null;
                }
                ACFBungeeUtil.sendMsg(c.getSender(), "§cCould not find a player by the name " + playercheck);
                throw new InvalidCommandArgument(false);
            }
            return new OnlineProxiedPlayer(proxiedPlayer);
        });
        registerSenderAwareContext(CommandSender.class, BungeeCommandExecutionContext::getSender);
        registerSenderAwareContext(ProxiedPlayer.class, (c) -> {
            ProxiedPlayer proxiedPlayer = c.getSender() instanceof ProxiedPlayer ? (ProxiedPlayer) c.getSender() : null;
            if (proxiedPlayer == null && !c.hasAnnotation(Optional.class)) {
                throw new InvalidCommandArgument("Requires a player to run this command", false);
            }
            return proxiedPlayer;
        });
        registerContext(ChatColor.class, c -> {
            String first = c.popFirstArg();
            Stream<ChatColor> colors = Stream.of(ChatColor.values());
            if (c.hasFlag("colorsonly")) {
                colors = colors.filter(color -> color.ordinal() <= 0xF);
            }
            String filter = c.getFlagValue("filter", (String) null);
            if (filter != null) {
                filter = ACFUtil.simplifyString(filter);
                String finalFilter = filter;
                colors = colors.filter(color -> finalFilter.equals(ACFUtil.simplifyString(color.name())));
            }

            ChatColor match = ACFUtil.simpleMatch(ChatColor.class, first);
            if (match == null) {
                String valid = colors
                        .map(color -> ChatColor.YELLOW + ACFUtil.simplifyString(color.name()))
                        .collect(Collectors.joining("&c, "));

                throw new InvalidCommandArgument("Please specify one of: " + valid);
            }
            return match;
        });
    }
}
