package co.aikar.commands;

public class SpongeLocales extends Locales{
    private final SpongeCommandManager manager;

    public SpongeLocales(SpongeCommandManager manager) {
        super(manager);
        this.manager = manager;
    }

    @Override
    public void loadLanguages() {
        String pluginName = "acf-" + manager.plugin.getName();
        addMessageBundles("acf-minecraft", pluginName, pluginName.toLowerCase());
    }
}
