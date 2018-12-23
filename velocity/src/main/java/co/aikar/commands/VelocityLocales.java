package co.aikar.commands;

public class VelocityLocales extends Locales {
    private final VelocityCommandManager manager;

    public VelocityLocales(VelocityCommandManager manager) {
        super(manager);

        this.manager = manager;
        this.addBundleClassLoader(this.manager.getPlugin().getClass().getClassLoader());
    }

    @Override
    public void loadLanguages() {
        super.loadLanguages();
        String pluginName = "acf-" + manager.plugin.getDescription().getName().get();
        addMessageBundles("acf-minecraft", pluginName, pluginName.toLowerCase());
    }
}
