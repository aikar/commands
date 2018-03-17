package co.aikar.commands;

import co.aikar.commands.annotation.Optional;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;

public class JDACommandContexts extends CommandContexts<JDACommandExecutionContext> {
    private final JDACommandManager manager;
    private final JDA jda;

    public JDACommandContexts(JDACommandManager manager) {
        super(manager);
        this.manager = manager;
        this.jda = this.manager.getJDA();
        this.registerIssuerOnlyContext(JDACommandEvent.class, CommandExecutionContext::getIssuer);
        this.registerIssuerOnlyContext(MessageReceivedEvent.class, c -> c.getIssuer().getIssuer());
        this.registerIssuerOnlyContext(Message.class, c -> {
            MessageReceivedEvent event = c.getIssuer().getIssuer();
            return event.getMessage();
        });
        this.registerIssuerOnlyContext(Guild.class, c -> {
            MessageReceivedEvent event = c.getIssuer().getIssuer();
            if (event.isFromType(ChannelType.PRIVATE) && !c.hasAnnotation(Optional.class)) {
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


        this.registerIssuerOnlyContext(JDA.class, c -> jda);
        this.registerContext(User.class, c -> {
            String arg = c.popFirstArg();
            User user = null;
            if (arg.startsWith("@")) {
                user = jda.getUserById(arg.substring(1));
            } else {
                List<User> users = jda.getUsersByName(arg, true);
                if (!users.isEmpty()) {
                    user = users.get(0);
                }
            }
            if (user == null) {
                throw new InvalidCommandArgument("Could not find a user with that name or ID"); // TODO: Message keys
            }
            return user;
        });
    }

}
