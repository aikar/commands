package co.aikar.commands;

import java.util.concurrent.TimeUnit;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.PlayerSettingsChangedEvent;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

public class ACFVelocityListener {

    private final VelocityCommandManager manager;
    private final PluginContainer plugin;
    private final ProxyServer proxy;

    public ACFVelocityListener(VelocityCommandManager manager, PluginContainer plugin, ProxyServer proxy) {
        this.manager = manager;
        this.plugin = plugin;
        this.proxy = proxy;
    }

    @Subscribe
    public void onPlayerJoin(PostLoginEvent loginEvent) {
        Player player = loginEvent.getPlayer();

        // the client settings are sent after a successful login
        Runnable task = () -> manager.readLocale(player);
        proxy.getScheduler().buildTask(plugin, task).delay(1, TimeUnit.SECONDS).schedule();
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent disconnectEvent) {
        // cleanup
        Player player = disconnectEvent.getPlayer();
        manager.issuersLocale.remove(player.getUniqueId());
    }

    @Subscribe
    public void onSettingsChange(PlayerSettingsChangedEvent settingsEvent) {
        manager.setIssuerLocale(settingsEvent.getPlayer(), settingsEvent.getPlayer().getPlayerSettings().getLocale());
    }
}
