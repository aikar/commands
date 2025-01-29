package co.aikar.commands;

import org.spongepowered.api.entity.living.player.Player;

public class SpongeConditionContext extends ConditionContext <SpongeCommandIssuer> {
    protected SpongeConditionContext(SpongeCommandIssuer issuer, String config) {
        super(issuer, config);
    }


    public SpongeCommandSource getSource() {
        return getIssuer().getIssuer();
    }

    public Player getPlayer() {
        return getIssuer().getPlayer();
    }
}
