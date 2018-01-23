package co.aikar.commands;

import org.jetbrains.annotations.NotNull;

public class JDACommandConfig implements CommandConfig {
    protected @NotNull String startsWith = "!";

    public JDACommandConfig() {

    }

    @NotNull
    public String getStartsWith() {
        return startsWith;
    }

    public void setStartsWith(@NotNull String startsWith) {
        this.startsWith = startsWith;
    }
}
