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
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VelocityCommandCompletions extends CommandCompletions<VelocityCommandCompletionContext> {

    public VelocityCommandCompletions(ProxyServer server, CommandManager manager) {
        super(manager);
        registerAsyncCompletion("chatcolors", c -> {
            Stream<TextFormat> colors = NamedTextColor.NAMES.values().stream().map(namedTextColor -> namedTextColor);
            if (!c.hasConfig("colorsonly")) {
                colors = Stream.concat(colors, Stream.of(TextDecoration.values()));
            }
            String filter = c.getConfig("filter");
            if (filter != null) {
                Set<String> filters = Arrays.stream(ACFPatterns.COLON.split(filter)).map(ACFUtil::simplifyString)
                        .collect(Collectors.toSet());

                colors = colors.filter(color -> filters.contains(ACFUtil.simplifyString(color.toString())));
            }

            return colors.map(color -> ACFUtil.simplifyString(color.toString())).collect(Collectors.toList());
        });
        registerCompletion("players", c -> {
            CommandSource sender = c.getSender();
            ACFVelocityUtil.validate(sender, "Sender cannot be null");
            String input = c.getInput();

            ArrayList<String> matchedPlayers = new ArrayList<>();
            for (Player player : server.getAllPlayers()) {
                String name = player.getUsername();
                if (ApacheCommonsLangUtil.startsWithIgnoreCase(name, input)) {
                    matchedPlayers.add(name);
                }
            }

            matchedPlayers.sort(String.CASE_INSENSITIVE_ORDER);
            return matchedPlayers;
        });
    }
}
