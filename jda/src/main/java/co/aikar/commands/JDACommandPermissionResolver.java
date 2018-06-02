package co.aikar.commands;


import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;

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
    public boolean hasPermission(JDACommandEvent event, String permission) {
        Member guildMember = event.getIssuer().getMember();
        if (guildMember == null) {
            return false;
        }

        if (guildMember.isOwner()) {
            return true;
        }

        Integer permissionOffset = discordPermissionOffsets.get(permission);
        if (permissionOffset == null) {
            return false;
        }

        return guildMember.hasPermission(
                Permission.getFromOffset(permissionOffset)
        );
    }
}
