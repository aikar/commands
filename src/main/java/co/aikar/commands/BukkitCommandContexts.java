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

import co.aikar.commands.annotation.Optional;
import co.aikar.commands.contexts.OnlinePlayer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class BukkitCommandContexts extends CommandContexts {

    BukkitCommandContexts() {
        super();

        registerContext(OnlinePlayer.class, (c) -> {
            final String playercheck = c.popFirstArg();
            Player player = CommandUtil.findPlayerSmart(c.getSender(), playercheck);
            if (player == null) {
                CommandUtil.sendMsg(c.getSender(), "&cCould not find a player by the name " + playercheck);
                throw new InvalidCommandArgument(false);
            }
            return new OnlinePlayer(player);
        });
        registerSenderAwareContext(World.class, (c) -> {
            String firstArg = c.getFirstArg();
            World world = firstArg != null ? Bukkit.getWorld(firstArg) : null;
            if (world != null) {
                c.popFirstArg();
            }
            if (world == null && c.getSender() instanceof Player) {
                world = ((Entity) c.getSender()).getWorld();
            }
            if (world == null) {
                throw new InvalidCommandArgument("Invalid World");
            }
            return world;
        });
        registerSenderAwareContext(CommandSender.class, CommandExecutionContext::getSender);
        registerSenderAwareContext(Player.class, (c) -> {
            Player player = c.getSender() instanceof Player ? (Player) c.getSender() : null;
            if (player == null && !c.hasAnnotation(Optional.class)) {
                throw new InvalidCommandArgument("Requires a player to run this command", false);
            }
            if (player != null && c.hasFlag("itemheld") && !CommandUtil.isValidItem(player.getInventory().getItemInMainHand())) {
                throw new InvalidCommandArgument("You must be holding an item in your main hand.", false);
            }
            return player;
        });
    }
}
