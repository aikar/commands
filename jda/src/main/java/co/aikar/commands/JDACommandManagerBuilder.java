package co.aikar.commands;

import net.dv8tion.jda.core.JDA;
import org.jetbrains.annotations.NotNull;

public class JDACommandManagerBuilder {
    private JDA jda;
    private CommandConfig defaultConfig = null;
    private CommandConfigProvider configProvider = null;
    private CommandPermissionResolver permissionResolver = null;

    public JDACommandManagerBuilder(JDA jda) {
        this.jda = jda;
    }

    public JDACommandManagerBuilder defaultConfig(@NotNull CommandConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
        return this;
    }

    public JDACommandManagerBuilder configProvider(@NotNull CommandConfigProvider configProvider) {
        this.configProvider = configProvider;
        return this;
    }

    public JDACommandManagerBuilder permissionResolver(@NotNull CommandPermissionResolver permissionResolver) {
        this.permissionResolver = permissionResolver;
        return this;
    }

    public JDACommandManager create() {
        return new JDACommandManager(jda, defaultConfig, configProvider, permissionResolver);
    }
}
