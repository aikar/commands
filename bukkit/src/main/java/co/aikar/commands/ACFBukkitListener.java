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

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

class ACFBukkitListener implements Listener {
    private BukkitCommandManager manager;
    private final Plugin plugin;

    public ACFBukkitListener(BukkitCommandManager manager, Plugin plugin) {
        this.manager = manager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (!(plugin.getName().equalsIgnoreCase(event.getPlugin().getName()))) {
            return;
        }
        manager.unregisterCommands();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (this.manager.autoDetectFromClient) {
            this.manager.readPlayerLocale(player);
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> manager.readPlayerLocale(player), 20);
        } else {
            this.manager.setIssuerLocale(player, this.manager.getLocales().getDefaultLocale());
            this.manager.notifyLocaleChange(this.manager.getCommandIssuer(player), null, this.manager.getLocales().getDefaultLocale());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent quitEvent) {
        //cleanup
        UUID playerUniqueId = quitEvent.getPlayer().getUniqueId();
        manager.issuersLocale.remove(playerUniqueId);
        manager.issuersLocaleString.remove(playerUniqueId);
    }
}
