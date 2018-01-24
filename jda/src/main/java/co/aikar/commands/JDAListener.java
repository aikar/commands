package co.aikar.commands;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class JDAListener extends ListenerAdapter {

    private final JDACommandManager manager;

    JDAListener(JDACommandManager manager) {

        this.manager = manager;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.TEXT) || event.isFromType(ChannelType.PRIVATE)) {
            this.manager.dispatchEvent(event);
        }
    }
}
