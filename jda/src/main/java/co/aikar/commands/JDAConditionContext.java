package co.aikar.commands;

public class JDAConditionContext extends ConditionContext<CommandEvent> {
    JDAConditionContext(CommandEvent issuer, String config) {
        super(issuer, config);
    }
}
