package co.aikar.commands;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class ACFSpongeUtil {
    public static ServerPlayer findPlayerSmart(CommandIssuer issuer, String search) {
        CommandCause requester = issuer.getIssuer();
        if (search == null) {
            return null;
        }
        String name = ACFUtil.replace(search, ":confirm", "");
        if (!isValidName(name)) {
            issuer.sendError(MinecraftMessageKeys.IS_NOT_A_VALID_NAME, "{name}", name);
            return null;
        }

        List<ServerPlayer> matches = matchPlayer(name);
        List<ServerPlayer> confirmList = new ArrayList<>();
        findMatches(search, requester, matches, confirmList);


        if (matches.size() > 1 || confirmList.size() > 1) {
            String allMatches = matches.stream().map(ServerPlayer::name).collect(Collectors.joining(", "));
            issuer.sendError(MinecraftMessageKeys.MULTIPLE_PLAYERS_MATCH,
                    "{search}", name, "{all}", allMatches);
            return null;
        }

        if (matches.isEmpty()) {
            ServerPlayer player = ACFUtil.getFirstElement(confirmList);
            if (player == null) {
                issuer.sendError(MinecraftMessageKeys.NO_PLAYER_FOUND_SERVER, "{search}", name);
                return null;
            } else {

                issuer.sendInfo(MinecraftMessageKeys.PLAYER_IS_VANISHED_CONFIRM, "{vanished}", player.name());
                return null;
            }
        }

        return matches.get(0);
    }

    private static void findMatches(String search, CommandCause requester, List<ServerPlayer> matches, List<ServerPlayer> confirmList) {
        // Remove vanished players from smart matching.
        Iterator<ServerPlayer> iter = matches.iterator();
        //noinspection Duplicates
        while (iter.hasNext()) {
            ServerPlayer player = iter.next();
            if (requester.subject() instanceof ServerPlayer && !((ServerPlayer) requester.subject()).canSee(player)) {
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

    public static List<ServerPlayer> matchPlayer(String partialName) {
        List<ServerPlayer> matchedPlayers = new ArrayList<>();

        for (ServerPlayer iterPlayer : Sponge.server().onlinePlayers()) {
            String iterPlayerName = iterPlayer.name();

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
