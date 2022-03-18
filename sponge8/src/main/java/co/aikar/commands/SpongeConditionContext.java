package co.aikar.commands;

import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class SpongeConditionContext extends ConditionContext <SpongeCommandIssuer> {
    SpongeConditionContext(SpongeCommandIssuer issuer, String config) {
        super(issuer, config);
    }


    public CommandCause getSource() {
        return getIssuer().getIssuer();
    }

    public ServerPlayer getPlayer() {
        return getIssuer().getPlayer();
    }
}
