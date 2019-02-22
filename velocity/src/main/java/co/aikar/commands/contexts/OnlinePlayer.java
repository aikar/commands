package co.aikar.commands.contexts;

import com.velocitypowered.api.proxy.Player;

/**
 * @deprecated Use {@link co.aikar.commands.velocity.OnlinePlayer instead}
 */
@Deprecated
public class OnlinePlayer extends co.aikar.commands.velocity.OnlinePlayer {
    public OnlinePlayer(Player player) {
        super(player);
    }
}
