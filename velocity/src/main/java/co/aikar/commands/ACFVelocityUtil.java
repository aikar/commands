package co.aikar.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import net.kyori.text.TextComponent;
import net.kyori.text.serializer.ComponentSerializers;

public class ACFVelocityUtil {

    @SuppressWarnings("deprecation")
    public static TextComponent color(String message) {
        return ComponentSerializers.LEGACY.deserialize(message);
    }

    public static Player findPlayerSmart(ProxyServer server, CommandIssuer issuer, String search) {
        CommandSource requester = issuer.getIssuer();
        String name = ACFUtil.replace(search, ":confirm", "");
        if (name.length() < 3) {
            issuer.sendError(MinecraftMessageKeys.USERNAME_TOO_SHORT);
            return null;
        }
        if (!isValidName(name)) {
            issuer.sendError(MinecraftMessageKeys.IS_NOT_A_VALID_NAME, "{name}", name);
            return null;
        }

        List<Player> matches = new ArrayList<>(matchPlayer(server, name));

        if (matches.size() > 1) {
            String allMatches = matches.stream().map(Player::getUsername).collect(Collectors.joining(", "));
            issuer.sendError(MinecraftMessageKeys.MULTIPLE_PLAYERS_MATCH, "{search}", name, "{all}", allMatches);
            return null;
        }

        if (matches.isEmpty()) {
            issuer.sendError(MinecraftMessageKeys.NO_PLAYER_FOUND_SERVER, "{search}", name);
            return null;
        }

        return matches.get(0);
    }

    /*
     * Original code written by md_5
     * 
     * Modified to work with Velocity by Crypnotic
     */
    private static Collection<Player> matchPlayer(ProxyServer server, final String partialName) {
        // A better error message might be nice. This just mimics the previous output
        if (partialName == null) {
            throw new NullPointerException("partialName");
        }

        Optional<Player> exactMatch = server.getPlayer(partialName);
        //noinspection OptionalIsPresent
        if (exactMatch.isPresent()) {
            return Collections.singleton(exactMatch.get());
        }

        return server.getAllPlayers().stream()
                .filter(player -> player.getUsername().regionMatches(true, 0, partialName, 0, partialName.length()))
                .collect(Collectors.toList());
    }

    public static boolean isValidName(String name) {
        return name != null && !name.isEmpty() && ACFPatterns.VALID_NAME_PATTERN.matcher(name).matches();
    }

    public static <T> T validate(T object, String message, Object... values) {
        if (object == null) {
            throw new NullPointerException(String.format(message, values));
        }
        return object;
    }
}
