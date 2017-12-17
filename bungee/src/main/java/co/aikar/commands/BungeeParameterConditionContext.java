package co.aikar.commands;

import co.aikar.commands.annotation.Conditions;

public class BungeeParameterConditionContext <P> extends ParameterConditionContext<P, BungeeCommandExecutionContext, BungeeCommandIssuer> {
    BungeeParameterConditionContext(RegisteredCommand cmd, BungeeCommandIssuer issuer, BungeeCommandExecutionContext execContext, Conditions conditions) {
        super(cmd, issuer, execContext, conditions);
    }
}
