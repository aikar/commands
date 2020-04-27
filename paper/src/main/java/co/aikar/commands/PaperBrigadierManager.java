/*
 * Copyright (c) 2016-2020 Daniel Ennis (Aikar) - MIT License
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

import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

/**
 * Handles registering of commands into brigadier
 *
 * @author MiniDigger
 * @deprecated Unstable API
 */
@Deprecated
@UnstableAPI
public class PaperBrigadierManager implements Listener {

    private final PaperCommandManager manager;
    private final ACFBrigadierManager<BukkitBrigadierCommandSource> brigadierManager;

    public PaperBrigadierManager(Plugin plugin, PaperCommandManager manager) {
        manager.verifyUnstableAPI("brigadier");
        manager.log(LogLevel.INFO, "Enabled Brigadier Support!");

        this.manager = manager;
        this.brigadierManager = new ACFBrigadierManager<>(manager);

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onCommandRegister(CommandRegisteredEvent<BukkitBrigadierCommandSource> event) {
        RootCommand acfCommand = manager.getRootCommand(event.getCommandLabel());
        if (acfCommand != null) {
            event.setLiteral(brigadierManager.register(
                    acfCommand,
                    event.getLiteral(),
                    event.getBrigadierCommand(),
                    event.getBrigadierCommand(),
                    this::checkPermRoot,
                    this::checkPermSub
            ));
        }
    }

    private boolean checkPermSub(RegisteredCommand registeredCommand, BukkitBrigadierCommandSource sender) {
        return registeredCommand.hasPermission(manager.getCommandIssuer(sender.getBukkitSender()));
    }

    private boolean checkPermRoot(RootCommand rootCommand, BukkitBrigadierCommandSource sender) {
        return rootCommand.hasAnyPermission(manager.getCommandIssuer(sender.getBukkitSender()));
    }
}
