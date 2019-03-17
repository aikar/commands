package co.aikar.acfexample;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("soctest")
@CommandPermission("soc.parent")
public class SomeOtherCommand extends BaseCommand {

    @Subcommand("foo")
    @CommandPermission("soc.foo")
    public void onFoo1(Player player) {
        player.sendMessage("you foo'd");
    }

    @Subcommand("foo")
    public void onFoo2(CommandSender sender, @Single String foo) {
        sender.sendMessage("You foo'd with " + foo);
    }

    @CatchUnknown
    public void onUnknown(CommandSender sender) {
        sender.sendMessage("UNKNOWN!");
    }

    @Default
    public void test(Player player, String string, @Default("1") int integer) {
        player.sendMessage("Hi " + string + " - " + integer);
    }

    @HelpCommand
    public void onHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }
}
