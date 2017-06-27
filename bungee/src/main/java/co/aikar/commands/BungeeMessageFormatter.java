package co.aikar.commands;

import net.md_5.bungee.api.ChatColor;

public class BungeeMessageFormatter extends MessageFormatter<ChatColor> {

    public BungeeMessageFormatter(ChatColor... colors) {
        super(colors);
    }

    @Override
    String format(ChatColor color, String message) {
        return color + message;
    }
}
