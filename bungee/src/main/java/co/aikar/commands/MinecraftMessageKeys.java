package co.aikar.commands;

import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;

public enum MinecraftMessageKeys implements MessageKeyProvider {
    USERNAME_TOO_SHORT,
    IS_NOT_A_VALID_NAME,
    MULTIPLE_PLAYERS_MATCH,
    NO_PLAYER_FOUND_SERVER,
    NO_PLAYER_FOUND
    ;

    private final MessageKey key = MessageKey.of("acf-minecraft." + this.name().toLowerCase());
    public MessageKey getMessageKey() {
        return key;
    }
}
