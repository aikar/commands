package co.aikar.commands;

import co.aikar.commands.annotation.Conditions;

public class SpongeParameterConditionContext <P> extends ParameterConditionContext<P, SpongeCommandExecutionContext, SpongeCommandIssuer> {
    SpongeParameterConditionContext(RegisteredCommand cmd, SpongeCommandIssuer issuer, SpongeCommandExecutionContext execContext, Conditions conditions) {
        super(cmd, issuer, execContext, conditions);
    }
}
