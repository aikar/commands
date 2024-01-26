package co.aikar.commands;

import java.util.Locale;
import java.util.Map.Entry;

import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.living.player.PlayerChangeClientSettingsEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;

public class ACFSpongeListener {

    private final SpongeCommandManager manager;

    public ACFSpongeListener(SpongeCommandManager manager) {
        this.manager = manager;
    }

    @Listener(order = Order.POST)
    public void onSettingsChange(PlayerChangeClientSettingsEvent changeSettingsEvent, @First ServerPlayer targetPlayer) {
        //this event will be fired on join as well as every time the player changes it
        manager.setIssuerLocale(CommandCause.create(), targetPlayer.locale());
    }

    @Listener
    public void onDisconnectCleanup(ServerSideConnectionEvent.Disconnect disconnectEvent, @First ServerPlayer player) {
        manager.issuersLocale.remove(player.uniqueId());
    }

    @Listener(order = Order.LAST)
    public void registerCommands(RegisterCommandEvent<Command> event) {
        for (Entry<String, SpongeRootCommand> entry : this.manager.registeredCommands.entrySet()) {
            String commandName = entry.getKey().toLowerCase(Locale.ENGLISH);
            SpongeRootCommand spongeCommand = (SpongeRootCommand) entry.getValue();
            event.register(manager.plugin, spongeCommand, commandName);
            spongeCommand.isRegistered = true;
            this.manager.registeredCommands.put(commandName, spongeCommand);
        }
    }
}
