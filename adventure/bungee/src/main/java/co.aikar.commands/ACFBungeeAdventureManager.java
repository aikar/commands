package co.aikar.commands;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.Plugin;

public class ACFBungeeAdventureManager extends ACFAdventureManager<ChatColor> {
    private BungeeAudiences audiences;

    public ACFBungeeAdventureManager(Plugin plugin, CommandManager<?, ?, ? extends ChatColor, ?, ?, ?> manager) {
        super(manager);

        this.audiences = BungeeAudiences.create(plugin);
    }

    public Audience wrapIssuer(CommandIssuer issuer) {
        return this.audiences.sender(issuer.getIssuer());
    }

    @Override
    public void sendMessage(CommandIssuer issuer, MessageFormatter<ChatColor> formatter, String message) {
        if (formatter != null) {
            message = "<color:" + Integer.toHexString(formatter.getDefaultColor().getColor().getRGB()).substring(2) + ">" + message;
            for (int i = 1; i <= formatter.getColors().size(); ++i) {
                String colorname = "#" + Integer.toHexString(formatter.getColor(i).getColor().getRGB()).substring(2);
                message = message.replace("<c" + i + ">", "<color:" + colorname + ">");
                message = message.replace("</c" + i + ">", "</color:" + colorname + ">");
            }
        }
        wrapIssuer(issuer).sendMessage(MiniMessage.get().parse(message));
    }
}

