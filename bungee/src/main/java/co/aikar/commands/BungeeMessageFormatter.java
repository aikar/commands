package co.aikar.commands;

import net.md_5.bungee.api.ChatColor;

public class BungeeMessageFormatter implements MessageFormatter {
    private final ChatColor color1;
    private final ChatColor color2;
    private final ChatColor color3;

    public BungeeMessageFormatter(ChatColor color1) {
        this(color1, color1);
    }
    public BungeeMessageFormatter(ChatColor color1, ChatColor color2) {
        this(color1, color2, color2);
    }
    public BungeeMessageFormatter(ChatColor color1, ChatColor color2, ChatColor color3) {
        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;
    }
    @Override
    public String c1(String message) {
        return color1 + message;
    }

    @Override
    public String c2(String message) {
        return color2 + message;
    }

    @Override
    public String c3(String message) {
        return color3 + message;
    }
}
