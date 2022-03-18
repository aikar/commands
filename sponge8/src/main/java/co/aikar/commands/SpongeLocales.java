package co.aikar.commands;

import java.util.Locale;

public class SpongeLocales extends Locales{
    private final SpongeCommandManager manager;

    public SpongeLocales(SpongeCommandManager manager) {
        super(manager);
        this.manager = manager;
        this.addBundleClassLoader(this.manager.getPlugin().getClass().getClassLoader());
    }

    @Override
    public void loadLanguages() {
        super.loadLanguages();
        String pluginName = "acf-" + manager.plugin.metadata().id();
        addMessageBundles("acf-minecraft", pluginName, pluginName.toLowerCase(Locale.ENGLISH));
    }
}
