/*
 * Copyright (c) 2016. Starlis LLC / dba Empire Minecraft
 *
 * This source code is proprietary software and must not be redistributed without Starlis LLC's approval
 *
 */

package co.aikar.commands.contexts;

import org.bukkit.entity.Player;

/*@Data*/ public class OnlinePlayer {
    public final Player player;

    public OnlinePlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return this.player;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof OnlinePlayer)) {
            return false;
        }
        final OnlinePlayer other = (OnlinePlayer) o;
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$player = this.getPlayer();
        final Object other$player = other.getPlayer();
        if (this$player == null ? other$player != null : !this$player.equals(other$player)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $player = this.getPlayer();
        result = result * PRIME + ($player == null ? 43 : $player.hashCode());
        return result;
    }

    protected boolean canEqual(Object other) {
        return other instanceof OnlinePlayer;
    }

    public String toString() {
        return "com.empireminecraft.commands.contexts.OnlinePlayer(player=" + this.getPlayer() + ")";
    }
}
