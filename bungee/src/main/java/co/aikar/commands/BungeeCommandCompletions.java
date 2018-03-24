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

import co.aikar.commands.apachecommonslang.ApacheCommonsLangUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BungeeCommandCompletions extends CommandCompletions<BungeeCommandCompletionContext> {

    public BungeeCommandCompletions(CommandManager manager) {
        super(manager);
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
        registerCompletion("players", c -> {
            CommandSender sender = c.getSender();
            ACFBungeeUtil.validate(sender, "Sender cannot be null");
            String input = c.getInput();

            ArrayList<String> matchedPlayers = new ArrayList<>();
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                String name = player.getName();
                if (ApacheCommonsLangUtil.startsWithIgnoreCase(name, input)) {
                    matchedPlayers.add(name);
                }
            }

            matchedPlayers.sort(String.CASE_INSENSITIVE_ORDER);
            return matchedPlayers;
        });
    }
}
