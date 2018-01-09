package co.aikar.commands;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

public class SpongeConditionContext extends ConditionContext <SpongeCommandIssuer> {
    SpongeConditionContext(SpongeCommandIssuer issuer, String config) {
        super(issuer, config);
    }


    public CommandSource getSource() {
        return getIssuer().getIssuer();
    }

    public Player getPlayer() {
        return getIssuer().getPlayer();
    }
}
