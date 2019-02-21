package co.aikar.acfexample;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import org.bukkit.entity.Player;

@CommandAlias("soctest")
public class SomeOtherCommand extends BaseCommand {
    @Default
    public void test(Player player, String string, @Default("1") int integer) {
        player.sendMessage("Hi " + string + " - " + integer);
    }
}
