package co.aikar.acfexample;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("brigadiertest")
public class BrigadierTest extends BaseCommand {

    @Subcommand("hello")
    @Syntax("<player>")
    @CommandCompletion("@player")
    @Description("Says hello to a player")
    public static void onHello(Player player, Player arg) {
        player.sendMessage("You said hello to " + arg.getDisplayName());
    }

    @Default
    @HelpCommand
    public static void onHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("test")
    @Syntax("<bool> <float> <double> <integer> <string>")
    @Description("Says hello to a player")
    public static void onTest(Player player, boolean booleanParam, float floatParam, double doubleParam, int integerParam, String stringParam) {
        player.sendMessage("You said: " + booleanParam + " - " + floatParam + " - " + doubleParam + " - " + integerParam + " - " + stringParam);
    }
}
