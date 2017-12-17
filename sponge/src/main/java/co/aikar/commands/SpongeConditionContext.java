package co.aikar.commands;

import co.aikar.commands.annotation.Conditions;

public class SpongeConditionContext extends ConditionContext <SpongeCommandIssuer> {
    SpongeConditionContext(RegisteredCommand cmd, SpongeCommandIssuer issuer, Conditions condAnno) {
        super(cmd, issuer, condAnno);
    }
}
