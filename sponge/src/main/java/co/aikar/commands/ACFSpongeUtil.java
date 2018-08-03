package co.aikar.commands;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class ACFSpongeUtil {
    public static Player findPlayerSmart(CommandIssuer issuer, String search) {
        CommandSource requester = issuer.getIssuer();
        if (search == null) {
            return null;
        }
        String name = ACFUtil.replace(search, ":confirm", "");
        if (name.length() < 3) {
            issuer.sendError(MinecraftMessageKeys.USERNAME_TOO_SHORT);
            return null;
        }
        if (!isValidName(name)) {
            issuer.sendError(MinecraftMessageKeys.IS_NOT_A_VALID_NAME, "{name}", name);
            return null;
        }

        List<Player> matches = matchPlayer(name);
        List<Player> confirmList = new ArrayList<>();
        findMatches(search, requester, matches, confirmList);


        if (matches.size() > 1 || confirmList.size() > 1) {
            String allMatches = matches.stream().map(Player::getName).collect(Collectors.joining(", "));
            issuer.sendError(MinecraftMessageKeys.MULTIPLE_PLAYERS_MATCH,
                    "{search}", name, "{all}", allMatches);
            return null;
        }

        if (matches.isEmpty()) {
            if (confirmList.isEmpty()) {
                issuer.sendError(MinecraftMessageKeys.NO_PLAYER_FOUND_SERVER,
                        "{search}", name);
                return null;
            } else {
                Player player = ACFUtil.getFirstElement(confirmList);
                issuer.sendInfo(MinecraftMessageKeys.PLAYER_IS_VANISHED_CONFIRM, "{vanished}", player.getName());
                return null;
            }
        }

        return matches.get(0);
    }

    private static void findMatches(String search, CommandSource requester, List<Player> matches, List<Player> confirmList) {
        // Remove vanished players from smart matching.
        Iterator<Player> iter = matches.iterator();
        //noinspection Duplicates
        while (iter.hasNext()) {
            Player player = iter.next();
            if (requester instanceof Player && !((Player) requester).canSee(player)) {
                if (requester.hasPermission("acf.seevanish")) {
                    if (!search.endsWith(":confirm")) {
                        confirmList.add(player);
                        iter.remove();
                    }
                } else {
                    iter.remove();
                }
            }
        }
    }

    public static List<Player> matchPlayer(String partialName) {
        List<Player> matchedPlayers = new ArrayList<>();

        for (Player iterPlayer : Sponge.getServer().getOnlinePlayers()) {
            String iterPlayerName = iterPlayer.getName();

            if (partialName.equalsIgnoreCase(iterPlayerName)) {
                // Exact match
                matchedPlayers.clear();
                matchedPlayers.add(iterPlayer);
                break;
            }
            if (iterPlayerName.toLowerCase(java.util.Locale.ENGLISH).contains(partialName.toLowerCase(java.util.Locale.ENGLISH))) {
                // Partial match
                matchedPlayers.add(iterPlayer);
            }
        }

        return matchedPlayers;
    }

    public static boolean isValidName(String name) {
        return name != null && !name.isEmpty() && ACFPatterns.VALID_NAME_PATTERN.matcher(name).matches();
    }

}
