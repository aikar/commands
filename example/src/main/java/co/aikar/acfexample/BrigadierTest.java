package co.aikar.acfexample;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("brigadiertest")
public class BrigadierTest extends BaseCommand {

    @Subcommand("hello")
    @Syntax("<player>")
    @CommandCompletion("@players")
    @Description("Says hello to a player")
    public static void onHello(Player player, @Flags("other") Player arg) {
        player.sendMessage("You said hello to " + arg.getDisplayName());
    }

    @Default
    @HelpCommand
    public static void onHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("test|test2")
    @Description("Says hello to a player")
    @CommandCompletion("true|false @range:20 @range:1-5 @range:20-30 test|test2|foo|bar true|false")
    public static void onTest(Player player, boolean booleanParam, float floatParam, double doubleParam, int integerParam, String stringParam, boolean test2) {
        player.sendMessage("You said: " + booleanParam + " - " + floatParam + " - " + doubleParam + " - " + integerParam + " - " + stringParam + " - " + test2 + "!");
    }

    @Subcommand("custom")
    @Description("Try custom completions")
    @CommandCompletion("@someobject")
    public static void onCustom(Player player, SomeObject object) {
        player.sendMessage("You said: " + object);
    }
}
