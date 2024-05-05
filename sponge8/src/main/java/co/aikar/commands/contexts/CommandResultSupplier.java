package co.aikar.commands.contexts;

import co.aikar.commands.CommandManager;
import co.aikar.commands.SpongeCommandOperationContext;
import org.spongepowered.api.command.CommandResult;

import java.util.function.Consumer;

public class CommandResultSupplier implements Consumer<CommandResult> {

    public CommandResultSupplier() {
    }

    @Override
    public void accept(CommandResult commandResult) {
        SpongeCommandOperationContext context = (SpongeCommandOperationContext) CommandManager.getCurrentCommandOperationContext();
        context.setResult(commandResult);
    }
}
