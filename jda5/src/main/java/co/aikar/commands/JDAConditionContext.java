package co.aikar.commands;

public class JDAConditionContext extends ConditionContext<JDACommandEvent> {
    protected JDAConditionContext(JDACommandEvent issuer, String config) {
        super(issuer, config);
    }
}
