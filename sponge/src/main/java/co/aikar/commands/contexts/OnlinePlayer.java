package co.aikar.commands.contexts;

import org.spongepowered.api.entity.living.player.Player;

/**
 * @deprecated Use {@link co.aikar.commands.sponge.OnlinePlayer instead}
 */
@Deprecated
public class OnlinePlayer extends co.aikar.commands.sponge.OnlinePlayer {
    public OnlinePlayer(Player player) {
        super(player);
    }
}
