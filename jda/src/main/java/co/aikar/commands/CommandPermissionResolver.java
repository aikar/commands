package co.aikar.commands;


public interface CommandPermissionResolver {
    boolean hasPermission(JDACommandEvent event, String permission);
}
