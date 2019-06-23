package co.aikar.commands;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

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
    public @NotNull UUID getUniqueId() {
        // Discord id only have 64 bit width (long) while UUIDs have twice the size.
        // In order to keep it unique we use 0L for the first 64 bit.
        long authorId = event.getAuthor().getIdLong();
        return new UUID(0, authorId);
    }

    @Override
    public boolean hasPermission(String permission) {
        CommandPermissionResolver permissionResolver = this.manager.getPermissionResolver();
        return permissionResolver == null || permissionResolver.hasPermission(manager, this, permission);
    }

    @Override
    public void sendMessageInternal(String message) {
        this.event.getChannel().sendMessage(message).queue();
    }

    public void sendMessage(Message message) {
        this.event.getChannel().sendMessage(message).queue();
    }

    public void sendMessage(MessageEmbed message) {
        this.event.getChannel().sendMessage(message).queue();
    }
}
