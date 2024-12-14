package co.aikar.commands;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.GenericSessionEvent;
import net.dv8tion.jda.api.events.session.SessionState;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

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

    @Override
    public void onGenericSession(@NotNull GenericSessionEvent event) {
        if (event.getState() == SessionState.READY) {
            manager.initializeBotOwner();
        }
    }

}
