package co.aikar.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeConditionContext extends ConditionContext<BungeeCommandIssuer> {
    BungeeConditionContext(BungeeCommandIssuer issuer, String config) {
        super(issuer, config);
    }


    public CommandSender getSender() {
        return getIssuer().getIssuer();
    }

    public ProxiedPlayer getPlayer() {
        return getIssuer().getPlayer();
    }
}
