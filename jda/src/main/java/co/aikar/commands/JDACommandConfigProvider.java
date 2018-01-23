package co.aikar.commands;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public interface JDACommandConfigProvider {
    CommandConfig provide(MessageReceivedEvent event);
}
