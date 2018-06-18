package co.aikar.commands;


public interface CommandPermissionResolver {
    boolean hasPermission(JDACommandManager manager, JDACommandEvent event, String permission);
}
