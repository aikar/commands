package co.aikar.commands;

import co.aikar.locales.MessageKey;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class BungeeLocales extends Locales {
    private final BungeeCommandManager manager;

    public BungeeLocales(BungeeCommandManager manager) {
        super(manager);

        this.manager = manager;
        this.addBundleClassLoader(this.manager.getPlugin().getClass().getClassLoader());
    }

    @Override
    public void loadLanguages() {
        super.loadLanguages();
        String pluginName = "acf-" + manager.plugin.getDescription().getName();
        addMessageBundles("acf-minecraft", pluginName, pluginName.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Loads the given file
     *
     * @param file
     * @param locale
     * @return If any language keys were added
     * @throws IOException
     */
    public boolean loadYamlLanguageFile(File file, Locale locale) throws IOException {
        Configuration yamlConfiguration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        return loadLanguage(yamlConfiguration, locale);
    }

    /**
     * Loads a file out of the plugin's data folder by the given name
     *
     * @param file
     * @param locale
     * @return If any language keys were added
     * @throws IOException
     */
    public boolean loadYamlLanguageFile(String file, Locale locale) throws IOException {
        Configuration yamlConfiguration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(this.manager.plugin.getDataFolder(), file));
        return loadLanguage(yamlConfiguration, locale);
    }


    /**
     * Loads every message from the Configuration object. Any nested values will be treated as namespace
     * so acf-core:\n\tfoo: bar will be acf-core.foo = bar
     *
     * @param config
     * @param locale
     * @return If any language keys were added
     */
    public boolean loadLanguage(Configuration config, Locale locale) {
        boolean loaded = false;
        for (String parentKey : config.getKeys()) {
            Configuration inner = config.getSection(parentKey);
            if (inner == null) {
                continue;
            }
            for (String key : inner.getKeys()) {
                String value = inner.getString(key);
                if (value != null && !value.isEmpty()) {
                    addMessage(locale, MessageKey.of(parentKey + "." + key), value);
                    loaded = true;
                }
            }
        }
        return loaded;
    }
}
