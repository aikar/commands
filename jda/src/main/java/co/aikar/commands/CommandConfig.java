package co.aikar.commands;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public interface CommandConfig extends CommandConfigProvider {
    @NotNull String getStartsWith();

    @Override
    default CommandConfig provide(MessageReceivedEvent event) {
        return this;
    }
}
