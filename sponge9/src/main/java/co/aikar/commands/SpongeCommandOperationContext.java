package co.aikar.commands;

import org.spongepowered.api.command.CommandResult;

public class SpongeCommandOperationContext extends CommandOperationContext {
    private CommandResult result = CommandResult.success();
    SpongeCommandOperationContext(CommandManager manager, CommandIssuer issuer, BaseCommand command, String commandLabel, String[] args, boolean isAsync) {
        super(manager, issuer, command, commandLabel, args, isAsync);
    }

    public CommandResult getResult() {
        return result;
    }

    public void setResult(CommandResult result) {
        this.result = result;
    }
}
