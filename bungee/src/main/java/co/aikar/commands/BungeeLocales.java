package co.aikar.commands;

import java.util.Locale;

public class BungeeLocales extends Locales {
    private final BungeeCommandManager manager;

    public BungeeLocales(BungeeCommandManager manager) {
        super(manager);

        this.manager = manager;
    }

    @Override
    public void loadLanguages() {
        super.loadLanguages();
        String pluginName = "acf-" + manager.plugin.getDescription().getName();
        addMessageBundles("acf-minecraft", pluginName, pluginName.toLowerCase());
    }

    @Override
    public void addMessageBundle(String bundleName, Locale locale) {
        // Load our bundles from the ClassLoader which ACF resides in
        super.addMessageBundle(bundleName, locale);
        // Attempt to load our bundles from the ClassLoader which the managers plugin resides in
        this.addMessageBundle(this.manager.getPlugin().getClass().getClassLoader(), bundleName, locale);
    }
}
