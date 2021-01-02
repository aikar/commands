package co.aikar.commands;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

public class ACFBukkitAdventureManager extends ACFAdventureManager<ChatColor> {
    private BukkitAudiences audiences;
    private MiniMessage miniMessage;

    public ACFBukkitAdventureManager(Plugin plugin, CommandManager<?, ?, ? extends ChatColor, ?, ?, ?> manager) {
        super(manager);

        this.audiences = BukkitAudiences.create(plugin);

        CommandContexts<? extends CommandExecutionContext<?, ? extends CommandIssuer>> contexts = manager.getCommandContexts();
        contexts.registerIssuerOnlyContext(Audience.class, c -> wrapIssuer(c.getIssuer()));
    }

    public MiniMessage getMiniMessage() {
        return miniMessage;
    }

    public void setMiniMessage(MiniMessage miniMessage) {
        this.miniMessage = miniMessage;
    }

    private Audience wrapIssuer(CommandIssuer issuer) {
        return this.audiences.sender(issuer.getIssuer());
    }

    @Override
    public void sendMessage(CommandIssuer issuer, MessageFormatter<ChatColor> formatter, String message) {
        if (formatter != null) {
            message = "<color:" + formatter.getDefaultColor().name().toLowerCase() + ">" + message;
            for (int i = 1; i <= formatter.getColors().size(); ++i) {
                String colorname = formatter.getColor(i).name().toLowerCase();
                message = message.replace("<c" + i + ">", "<color:" + colorname + ">");
                message = message.replace("</c" + i + ">", "</color:" + colorname + ">");
            }
        }
        wrapIssuer(issuer).sendMessage(miniMessage.parse(message));
    }
}

