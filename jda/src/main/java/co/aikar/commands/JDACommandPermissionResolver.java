package co.aikar.commands;


import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;

import java.util.HashMap;
import java.util.List;
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

        // TODO: regex
        if (permission.startsWith("role.")) {
            String perm = permission.split("role.")[1];
            List<Role> roles = event.getIssuer().getJDA().getRolesByName(perm, true);
            if (roles.size() == 1) {
                return guildMember.getRoles().contains(roles.get(0));
            }

            return false;
        }

        // TODO: We need to check if the event is for a specific channel
        int permissionOffset = discordPermissionOffsets.get(permission);
        Permission discordPermission = Permission.getFromOffset(permissionOffset);
        return guildMember.hasPermission(discordPermission);
    }
}
