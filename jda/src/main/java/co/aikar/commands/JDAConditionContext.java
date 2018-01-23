package co.aikar.commands;

public class JDAConditionContext extends ConditionContext<JDACommandEvent> {
    JDAConditionContext(JDACommandEvent issuer, String config) {
        super(issuer, config);
    }
}
