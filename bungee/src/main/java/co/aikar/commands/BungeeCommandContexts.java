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
import co.aikar.commands.bungee.contexts.OnlinePlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BungeeCommandContexts extends CommandContexts<BungeeCommandExecutionContext> {

    BungeeCommandContexts(CommandManager manager) {
        super(manager);
        registerContext(OnlinePlayer.class, this::getOnlinePlayer);
        registerContext(co.aikar.commands.contexts.OnlineProxiedPlayer.class, c -> {
            OnlinePlayer onlinePlayer = getOnlinePlayer(c);
            return onlinePlayer != null ? new co.aikar.commands.contexts.OnlineProxiedPlayer(onlinePlayer.getPlayer()) : null;
        });
        registerIssuerAwareContext(CommandSender.class, BungeeCommandExecutionContext::getSender);
        registerIssuerAwareContext(ProxiedPlayer.class, (c) -> {
            ProxiedPlayer proxiedPlayer = c.getSender() instanceof ProxiedPlayer ? (ProxiedPlayer) c.getSender() : null;
            if (proxiedPlayer == null && !c.hasAnnotation(Optional.class)) {
                throw new InvalidCommandArgument(MessageKeys.NOT_ALLOWED_ON_CONSOLE, false);
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

            ChatColor match = null;
            String simplified = ACFUtil.simplifyString(first);
            for (ChatColor chatColor : ChatColor.values()) {
                String simple = ACFUtil.simplifyString(chatColor.name());
                if (simplified.equals(simple)) {
                    match = chatColor;
                    break;
                }
            }
            if (match == null) {
                String valid = colors
                        .map(color -> "<c2>" + ACFUtil.simplifyString(color.name()) + "</c2>")
                        .collect(Collectors.joining("<c1>,</c1> "));

                throw new InvalidCommandArgument(MessageKeys.PLEASE_SPECIFY_ONE_OF, "{valid}", valid);
            }
            return match;
        });
    }

    @Nullable
    private co.aikar.commands.contexts.OnlineProxiedPlayer getOnlinePlayer(BungeeCommandExecutionContext c) throws InvalidCommandArgument {
        ProxiedPlayer proxiedPlayer = ACFBungeeUtil.findPlayerSmart(c.getIssuer(), c.popFirstArg());
        if (proxiedPlayer == null) {
            if (c.hasAnnotation(Optional.class)) {
                return null;
            }
            throw new InvalidCommandArgument(false);
        }
        return new co.aikar.commands.contexts.OnlineProxiedPlayer(proxiedPlayer);
    }
}
