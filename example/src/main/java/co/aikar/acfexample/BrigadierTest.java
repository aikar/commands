package co.aikar.acfexample;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
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
    public static void onTest(CommandSender sender, boolean booleanParam, float floatParam, double doubleParam, int integerParam, String stringParam, @Optional Boolean test2) {
        sender.sendMessage("You said: " + booleanParam + " - " + floatParam + " - " + doubleParam + " - " + integerParam + " - " + stringParam + " - " + test2 + "!");
    }

    @Subcommand("custom")
    @Description("Try custom completions")
    @Syntax("<syntaxTest>")
    @CommandCompletion("@someobject")
    public static void onCustom(Player player, SomeObject object) {
        player.sendMessage("You said: " + object);
    }

    @Subcommand("dummy admin")
    @CommandPermission("dummy")
    public static void onPerm(Player player) {
        player.sendMessage("You shall pass");
    }

    @Subcommand("sub3")
    public static void sub3(Player player, String wooo) {
        player.sendMessage("Wooo " + wooo);
    }

    @Subcommand("sub sub")
    public static void onSubSub(Player player, String wooo) {
        player.sendMessage("Wooo " + wooo);
    }

    @Subcommand("sub2")
    public static void onSub2(Player player) {
        player.sendMessage("Sub2");
    }

    @Subcommand("sub2 sub")
    public static void onSub2Sub(Player player, String test) {
        player.sendMessage("Sub2 sub " + test);
    }

    @Subcommand("find|where")
    @CommandPermission("hyperverse.find")
    @CommandAlias("hvf|hvfind")
    @CommandCompletion("@players")
    public void findPlayer(final CommandSender sender, final String player) { /* stub */ }

    @Subcommand("greedy")
    public void onGreedy(Player player, String greedyString) {
        player.sendMessage(greedyString);
    }

    @Subcommand("notgreedy")
    public void notGreedy(Player player, @Single String notGreedy) {
        player.sendMessage(notGreedy);
    }
}
