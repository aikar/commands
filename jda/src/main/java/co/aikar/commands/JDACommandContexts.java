package co.aikar.commands;

import co.aikar.commands.annotation.Optional;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class JDACommandContexts extends CommandContexts<JDACommandExecutionContext> {
    public JDACommandContexts(JDACommandManager manager) {
        super(manager);
        this.registerIssuerOnlyContext(JDACommandEvent.class, CommandExecutionContext::getIssuer);
        this.registerIssuerOnlyContext(MessageReceivedEvent.class, c -> c.getIssuer().getIssuer());
        this.registerIssuerOnlyContext(Message.class, c -> {
            MessageReceivedEvent event = c.getIssuer().getIssuer();
            return event.getMessage();
        });
        this.registerIssuerOnlyContext(Guild.class, c -> {
            MessageReceivedEvent event = c.getIssuer().getIssuer();
            if (event.isFromType(ChannelType.PRIVATE) && c.getAnnotation(Optional.class) == null) {
                throw new InvalidCommandArgument("This command can only be executed in a Guild.", false); // TODO: Message Keys
            } else {
                return event.getGuild();
            }
        });
        this.registerIssuerOnlyContext(MessageChannel.class, c -> {
            MessageReceivedEvent event = c.getIssuer().getIssuer();
            return event.getChannel();
        });
        this.registerIssuerOnlyContext(ChannelType.class, c -> {
            MessageReceivedEvent event = c.getIssuer().getIssuer();
            return event.getChannelType();
        });
        this.registerIssuerOnlyContext(User.class, c -> {
            MessageReceivedEvent event = c.getIssuer().getIssuer();
            return event.getAuthor();
        });
        this.registerIssuerOnlyContext(JDA.class, c -> ((JDACommandManager) this.manager).getJDA());
    }

}
