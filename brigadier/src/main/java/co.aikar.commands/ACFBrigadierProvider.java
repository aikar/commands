package co.aikar.commands;

/**
 * Interface for providing access to a {@link com.mojang.brigadier.CommandDispatcher}
 *
 * @author MiniDigger
 * @deprecated Unstable API
 */
@Deprecated
@UnstableAPI
public interface ACFBrigadierProvider {

    /**
     * Checks if this provider is supported or not
     *
     * @return if this provider is supported or not
     */
    default boolean isSupported() {
        try {
            Class.forName("com.mojang.brigadier.CommandDispatcher");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Gets an instance of the currently active {@link com.mojang.brigadier.CommandDispatcher}
     *
     * @return the command dispatcher
     */
    Object getCommandDispatcher();
}
