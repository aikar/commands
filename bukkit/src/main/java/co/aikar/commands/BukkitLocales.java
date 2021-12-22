/*
 * Copyright (c) 2016-2017 Daniel Ennis (Aikar) - MIT License
 *
 *  Permission is hereby granted, free of charge, to any person obtaining
 *  a copy of this software and associated documentation files (the
 *  "Software"), to deal in the Software without restriction, including
 *  without limitation the rights to use, copy, modify, merge, publish,
 *  distribute, sublicense, and/or sell copies of the Software, and to
 *  permit persons to whom the Software is furnished to do so, subject to
 *  the following conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 *  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 *  OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package co.aikar.commands;

import co.aikar.locales.MessageKey;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class BukkitLocales extends Locales {
    private final BukkitCommandManager manager;

    public BukkitLocales(BukkitCommandManager manager) {
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
     * @throws InvalidConfigurationException
     */
    public boolean loadYamlLanguageFile(File file, Locale locale) throws IOException, InvalidConfigurationException {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        yamlConfiguration.load(file);
        return loadLanguage(yamlConfiguration, locale);
    }

    /**
     * Loads a file out of the plugin's data folder by the given name
     *
     * @param file
     * @param locale
     * @return If any language keys were added
     * @throws IOException
     * @throws InvalidConfigurationException
     */
    public boolean loadYamlLanguageFile(String file, Locale locale) throws IOException, InvalidConfigurationException {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        yamlConfiguration.load(new File(this.manager.plugin.getDataFolder(), file));
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
    public boolean loadLanguage(FileConfiguration config, Locale locale) {
        boolean loaded = false;
        for (String key : config.getKeys(true)) {
            if (config.isString(key) || config.isDouble(key) || config.isLong(key) || config.isInt(key)
                    || config.isBoolean(key)) {
                String value = config.getString(key);
                if (value != null && !value.isEmpty()) {
                    addMessage(locale, MessageKey.of(key), value);
                    loaded = true;
                }
            }
        }

        return loaded;
    }
}
