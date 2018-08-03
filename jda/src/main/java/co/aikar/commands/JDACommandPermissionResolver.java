package co.aikar.commands;

import net.dv8tion.jda.core.Permission;

import java.util.HashMap;
import java.util.Map;

public class JDACommandPermissionResolver implements CommandPermissionResolver {
    private Map<String, Integer> discordPermissionOffsets;

    public JDACommandPermissionResolver() {
        discordPermissionOffsets = new HashMap<>();
        for (Permission permission : Permission.values()) {
            discordPermissionOffsets.put(permission.name().toLowerCase().replaceAll("_", "-"), permission.getOffset());
        }
    }

    @Override
    public boolean hasPermission(JDACommandManager manager, JDACommandEvent event, String permission) {
        // Explicitly return true if the issuer is the bot's owner. They are always allowed.
        if (manager.getBotOwnerId() == event.getIssuer().getAuthor().getIdLong()) {
            return true;
        }

        // Return false on webhook messages, as they cannot have permissions defined.
        if (event.getIssuer().isWebhookMessage()) {
            return false;
        }

        Integer permissionOffset = discordPermissionOffsets.get(permission);
        if (permissionOffset == null) {
            return false;
        }

        return event.getIssuer().getMember().hasPermission(
                Permission.getFromOffset(permissionOffset)
        );
    }
}
