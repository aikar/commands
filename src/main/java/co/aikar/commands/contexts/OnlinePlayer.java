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
