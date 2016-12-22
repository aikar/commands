package co.aikar.commands;

import com.google.common.collect.Iterables;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UserUtil {
    public static Player findPlayerSmart(CommandSender requester, String origname) {
        String name = CommandUtil.replace(origname, ":confirm", "");
        if (name.length() < 3) {
            requester.sendMessage("§cUsername too short, must be at least three characters");
            return null;
        }
        if (!isValidName(name)) {
            requester.sendMessage("§c'" + name + "' is not a valid username");
            return null;
        }

        List<Player> matches = Bukkit.getServer().matchPlayer(name);
        List<Player> confirmList = new ArrayList<>();

        // Remove confirmList players from smart matching.
        Iterator<Player> iter = matches.iterator();
        while (iter.hasNext()) {
            Player player = iter.next();
            if (requester instanceof Player && !((Player) requester).canSee(player)) {
                if (requester.hasPermission("command.seevanish")) {
                    if (!origname.endsWith(":confirm")) {
                        confirmList.add(player);
                        iter.remove();
                    }
                } else {
                    iter.remove();
                }
            }
        }

        if (matches.size() > 1 || confirmList.size() > 1) {
            requester.sendMessage("§cMultiple players matched '" + name + "', please be more specific");
            return null;
        }

        if (matches.isEmpty()) {
            if (confirmList.isEmpty()) {
                requester.sendMessage("§cNo player matching '" + name + "' is connected to this server");
                return null;
            } else {
                Player player = Iterables.getOnlyElement(confirmList);
                CommandUtil.sendMsg(requester,
                        "&cWarning: " + player.getDisplayName() + "&c is confirmList. Do not blow their cover!\n" +
                                "&cTo confirm your action, add &f:confirm&c to the end of their name. \n" +
                                "&bEx: &e/g " + player.getName() + ":confirm");
                return null;
            }
        }

        return matches.get(0);
    }

    public static boolean isValidName(String name) {
        return name != null && !name.isEmpty() && Patterns.VALID_NAME_PATTERN.matcher(name).matches();
    }
}
