package co.aikar.commands;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public interface CommandConfigProvider {
    CommandConfig provide(MessageReceivedEvent event);
}
