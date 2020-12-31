package co.aikar.commands;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.plugin.Plugin;

public class ACFBungeeAdventureManager extends ACFAdventureManager {
    private BungeeAudiences audiences;

    public ACFBungeeAdventureManager(Plugin plugin, CommandManager<?, ?, ?, ?, ?, ?> manager) {
        super(manager);

        this.audiences = BungeeAudiences.create(plugin);
    }

    public Audience wrapIssuer(CommandIssuer issuer) {
        return this.audiences.sender(issuer.getIssuer());
    }

    @Override
    public void sendMessage(CommandIssuer issuer, String message) {
        wrapIssuer(issuer).sendMessage(MiniMessage.get().parse(message));
    }
}

