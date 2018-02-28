package co.aikar.commands;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.living.humanoid.player.PlayerChangeClientSettingsEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class ACFSpongeListener {

    private final SpongeCommandManager manager;

    public ACFSpongeListener(SpongeCommandManager manager) {
        this.manager = manager;
    }

    @Listener(order = Order.POST)
    public void onSettingsChange(PlayerChangeClientSettingsEvent changeSettingsEvent, @First Player targetPlayer) {
        //this event will be fired on join as well as every time the player changes it
        manager.setPlayerLocale(targetPlayer, targetPlayer.getLocale());
    }

    @Listener
    public void onDisconnectCleanup(ClientConnectionEvent.Disconnect disconnectEvent, @First Player player) {
        manager.issuersLocale.remove(player.getUniqueId());
    }
}
