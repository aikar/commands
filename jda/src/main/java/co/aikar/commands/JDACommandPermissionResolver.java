package co.aikar.commands;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;

import java.util.HashMap;
import java.util.Map;

public class JDACommandPermissionResolver implements CommandPermissionResolver {
    private JDACommandManager jdaCommandManager;
    private Map<String, Integer> discordPermissionOffsets;

    public JDACommandPermissionResolver(JDACommandManager jdaCommandManager) {
        this.jdaCommandManager = jdaCommandManager;
        discordPermissionOffsets = new HashMap<>();
        for (Permission permission : Permission.values()) {
            discordPermissionOffsets.put(permission.name().toLowerCase().replaceAll("_", "-"), permission.getOffset());
        }
    }

    @Override
    public boolean hasPermission(JDACommandEvent event, String permission) {
        // Explicitly return true if the issuer is the bot's owner. They are always allowed.
        if (jdaCommandManager.getBotOwnerId() == event.getIssuer().getAuthor().getIdLong()) {
            return true;
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
