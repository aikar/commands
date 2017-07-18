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

import co.aikar.locales.LanguageTable;
import co.aikar.locales.LocaleManager;
import co.aikar.locales.MessageKey;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class Locales {

    private static final Locale[] CORE_LANGUAGES = new Locale[]{
            Locale.ENGLISH
    };
    private final CommandManager manager;
    private final LocaleManager<CommandIssuer> localeManager;

    Locales(CommandManager manager) {
        this.manager = manager;
        this.localeManager = LocaleManager.create(manager::getIssuerLocale);
    }

    public Locale getDefaultLocale() {
        return this.localeManager.getDefaultLocale();
    }


    public void addMessageBundles(String... bundleNames) {
        for (String bundleName : bundleNames) {
            this.localeManager.addMessageBundle(bundleName, CORE_LANGUAGES);
        }
    }

    public void addMessageBundle(String bundleName, Locale locale) {
        this.localeManager.addMessageBundle(bundleName, locale);
    }

    public void addMessages(Locale locale, @NotNull Map<MessageKey, String> messages) {
        this.localeManager.addMessages(locale, messages);
    }

    public String addMessage(Locale locale, MessageKey key, String message) {
        return this.localeManager.addMessage(locale, key, message);
    }

    public String getMessage(CommandIssuer issuer, MessageKey key) {
        String message = this.localeManager.getMessage(issuer, key);
        if (message == null) {
            manager.log(LogLevel.ERROR, "Missing Language Key: " + key.getKey());
            message = "<MISSING_LANGUAGE_KEY:" + key.getKey() + ">";
        }
        return message;
    }

}
