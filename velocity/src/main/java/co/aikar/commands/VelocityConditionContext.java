package co.aikar.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

public class VelocityConditionContext extends ConditionContext <VelocityCommandIssuer> {
    VelocityConditionContext(VelocityCommandIssuer issuer, String config) {
        super(issuer, config);
    }


    public CommandSource getSender() {
        return getIssuer().getIssuer();
    }

    public Player getPlayer() {
        return getIssuer().getPlayer();
    }
}
