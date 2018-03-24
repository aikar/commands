package co.aikar.commands;

import co.aikar.commands.annotation.Author;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.SelfUser;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
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
        this.registerIssuerOnlyContext(Message.class, c -> c.issuer.getIssuer().getMessage());
        this.registerIssuerOnlyContext(ChannelType.class, c -> c.issuer.getIssuer().getChannelType());
        this.registerIssuerOnlyContext(JDA.class, c -> jda);
        this.registerIssuerOnlyContext(Guild.class, c -> {
            MessageReceivedEvent event = c.getIssuer().getIssuer();
            if (event.isFromType(ChannelType.PRIVATE) && !c.hasAnnotation(Optional.class)) {
                throw new InvalidCommandArgument("This command can only be executed in a Guild.", false); // TODO: Message Keys
            } else {
                return event.getGuild();
            }
        });
        this.registerIssuerOnlyContext(MessageChannel.class, c -> {
            if (c.hasAnnotation(Author.class)) {
                return c.issuer.getIssuer().getChannel();
            }
            String argument = c.popFirstArg();
            MessageChannel channel = null;
            if (argument.startsWith("<#")) {
                channel = jda.getTextChannelById(argument.substring(2, argument.length() - 1));
            } else {
                List<TextChannel> channelList = c.issuer.getEvent().getGuild().getTextChannelsByName(argument.toLowerCase(), true);
                if (channelList.size() > 1) {
                    throw new InvalidCommandArgument("Too many channels were found with the given name. Try with the `#channelname` syntax.", false);
                } else if (channelList.size() == 1) {
                    channel = channelList.get(0);
                }
            }
            if (channel == null) {
                throw new InvalidCommandArgument("Couldn't find the channel with that name or ID.");
            }
            return channel;
        });
        this.registerContext(User.class, c -> {
            if (c.hasAnnotation(SelfUser.class)) {
                return jda.getSelfUser();
            }
            String arg = c.popFirstArg();
            User user = null;
            if (arg.startsWith("<@")) {
                user = jda.getUserById(arg.substring(2, arg.length() - 1));
            } else {
                List<User> users = jda.getUsersByName(arg, true);
                if (users.size() > 1) {
                    throw new InvalidCommandArgument("Too many users were found with the given name. Try with the `@username#0000` syntax.", false);
                }
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
