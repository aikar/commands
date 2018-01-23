package co.aikar.commands;

public class JDACommandConfig implements CommandConfig {
    protected String startsWith = "!";

    public JDACommandConfig() {

    }

    public String getStartsWith() {
        return startsWith;
    }

    public void setStartsWith(String startsWith) {
        this.startsWith = startsWith;
    }
}
