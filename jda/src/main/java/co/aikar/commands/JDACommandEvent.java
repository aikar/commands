package co.aikar.commands;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class JDACommandEvent implements CommandIssuer {

    private MessageReceivedEvent event;
    private JDACommandManager manager;

    public JDACommandEvent(JDACommandManager manager, MessageReceivedEvent event) {

        this.manager = manager;
        this.event = event;
    }

    public MessageReceivedEvent getEvent() {
        return event;
    }

    @Override
    public MessageReceivedEvent getIssuer() {
        return event;
    }

    @Override
    public CommandManager getManager() {
        return this.manager;
    }

    @Override
    public boolean isPlayer() {
        return false;
    }

    @Override
    public boolean hasPermission(String permission) {
        // TODO: Permission Resolving
        return false;
    }

    @Override
    public void sendMessageInternal(String message) {
        this.event.getChannel().sendMessage(message).queue();
    }

    @Override
    public void sendMessage(String message) {

    }

    public void sendMessage(Message message) {
        this.event.getChannel().sendMessage(message).queue();
    }
    public void sendMessage(MessageEmbed message) {
        this.event.getChannel().sendMessage(message).queue();
    }
}
