package co.aikar.commands;

import co.aikar.commands.annotation.Conditions;

public class BungeeConditionContext extends ConditionContext <BungeeCommandIssuer> {
    BungeeConditionContext(RegisteredCommand cmd, BungeeCommandIssuer issuer, Conditions condAnno) {
        super(cmd, issuer, condAnno);
    }
}
