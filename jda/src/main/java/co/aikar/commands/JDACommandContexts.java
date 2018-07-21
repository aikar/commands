package co.aikar.commands;

import co.aikar.commands.annotation.Author;
import co.aikar.commands.annotation.CrossGuild;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.SelfUser;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;

// TODO: Message Keys !!!
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
            if (event.isFromType(ChannelType.PRIVATE) && !c.isOptional()) {
                throw new InvalidCommandArgument("This command can only be executed in a Guild.", false);
            } else {
                return event.getGuild();
            }
        });
        this.registerIssuerAwareContext(MessageChannel.class, c -> {
            if (c.hasAnnotation(Author.class)) {
                return c.issuer.getIssuer().getChannel();
            }
            boolean isCrossGuild = c.hasAnnotation(CrossGuild.class);
            String argument = c.popFirstArg(); // we pop because we are only issuer aware if we are annotated
            MessageChannel channel = null;
            if (argument.startsWith("<#")) {
                String id = argument.substring(2, argument.length() - 1);
                channel = isCrossGuild ? jda.getTextChannelById(id) : c.issuer.getIssuer().getGuild().getTextChannelById(id);
            } else {
                List<TextChannel> channelList = isCrossGuild ? jda.getTextChannelsByName(argument, true) :
                        c.issuer.getEvent().getGuild().getTextChannelsByName(argument, true);
                if (channelList.size() > 1) {
                    throw new InvalidCommandArgument("Too many channels were found with the given name. Try with the `#channelname` syntax.", false);
                } else if (channelList.size() == 1) {
                    channel = channelList.get(0);
                }
            }
            if (channel == null) {
                throw new InvalidCommandArgument("Couldn't find a channel with that name or ID.");
            }
            return channel;
        });
        this.registerIssuerAwareContext(User.class, c -> {
            if (c.hasAnnotation(SelfUser.class)) {
                return jda.getSelfUser();
            }
            String arg = c.getFirstArg();
            if (c.isOptional() && (arg == null || arg.isEmpty())) {
                return null;
            }
            arg = c.popFirstArg(); // we pop because we are only issuer aware if we are annotated
            User user = null;
            if (arg.startsWith("<@!")) { // for some reason a ! is added when @'ing and clicking their name.
                user = jda.getUserById(arg.substring(3, arg.length() - 1));
            } else if (arg.startsWith("<@")) { // users can /also/ be mentioned like this...
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
                throw new InvalidCommandArgument("Could not find a user with that name or ID.");
            }
            return user;
        });
        this.registerContext(Role.class, c -> {
            boolean isCrossGuild = c.hasAnnotation(CrossGuild.class);
            String arg = c.popFirstArg();
            Role role = null;
            if (arg.startsWith("<@&")) {
                String id = arg.substring(3, arg.length() - 1);
                role = isCrossGuild ? jda.getRoleById(id) : c.issuer.getIssuer().getGuild().getRoleById(id);
            } else {
                List<Role> roles = isCrossGuild ? jda.getRolesByName(arg, true)
                        : c.issuer.getIssuer().getGuild().getRolesByName(arg, true);
                if (roles.size() > 1) {
                    throw new InvalidCommandArgument("Too many roles were found with the given name. Try with the `@role` syntax.", false);
                }
                if (!roles.isEmpty()) {
                    role = roles.get(0);
                }
            }
            if (role == null) {
                throw new InvalidCommandArgument("Could not find a role with that name or ID.");
            }
            return role;
        });
    }
}
