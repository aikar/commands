package co.aikar.commands;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JDACommandCompletions extends CommandCompletions<CommandCompletionContext<?>> {
    private boolean initialized;
    public JDACommandCompletions(CommandManager manager) {
        super(manager);
        this.initialized = true;
    }

    @Override
    public CommandCompletionHandler registerCompletion(String id, CommandCompletionHandler<CommandCompletionContext<?>> handler) {
        if (initialized) {
            throw new UnsupportedOperationException("JDA Doesn't support Command Completions");
        }
        return null;
    }

    @Override
    public CommandCompletionHandler registerAsyncCompletion(String id, AsyncCommandCompletionHandler<CommandCompletionContext<?>> handler) {
        if (initialized) {
            throw new UnsupportedOperationException("JDA Doesn't support Command Completions");
        }
        return null;
    }

    @NotNull
    @Override
    List<String> of(RegisteredCommand command, CommandIssuer sender, String[] args, boolean isAsync) {
        return ImmutableList.of();
    }

    @Override
    List<String> getCompletionValues(RegisteredCommand command, CommandIssuer sender, String completion, String[] args, boolean isAsync) {
        return ImmutableList.of();
    }
}
