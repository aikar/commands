package co.aikar.commands;

public interface ACFBrigadierProvider {

    default boolean isSupported(){
        try {
            Class.forName("com.mojang.brigadier.CommandDispatcher");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    Object getCommandDispatcher();
}
