package co.aikar.commands;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.Plugin;

public class ACFBukkitAdventureManager extends ACFAdventureManager {
    private BukkitAudiences audiences;

    public ACFBukkitAdventureManager(Plugin plugin, CommandManager<?, ?, ?, ?, ?, ?> manager) {
        super(manager);

        this.audiences = BukkitAudiences.create(plugin);
    }

    public Audience wrapIssuer(CommandIssuer issuer) {
        return this.audiences.sender(issuer.getIssuer());
    }

    @Override
    public void sendMessage(CommandIssuer issuer, String message) {
        wrapIssuer(issuer).sendMessage(MiniMessage.get().parse(message));
    }
}

