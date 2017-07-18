package co.aikar.commands;

public class BungeeLocales extends Locales {
    BungeeLocales(BungeeCommandManager manager) {
        super(manager);
        String pluginName = "acf-" + manager.plugin.getDescription().getName();
        addMessageBundles("acf-minecraft", pluginName, pluginName.toLowerCase());
    }
}
