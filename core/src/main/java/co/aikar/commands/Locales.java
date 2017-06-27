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

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * This isn't public yet, still WIP - API will break
 * @deprecated
 */
@SuppressWarnings("WeakerAccess")
@Deprecated
public class Locales {

    private Locale defaultLocale = Locale.ENGLISH;
    private final CommandManager manager;
    private final Map<Locale, LanguageTable> tables = Maps.newHashMap();

    Locales(CommandManager manager) {
        this.manager = manager;
        this.initializeSystemMessages();
    }

    private void initializeSystemMessages() {
        LanguageTable table = getTable(Locale.ENGLISH);
        //table.addMessage(MessageKey.FOO, "bar");
    }

    /**
     * Changes the default locale to use if the specified language key is missing for the desired locale
     * @param locale
     * @return Previous default locale
     */
    public Locale setDefaultLocale(Locale locale) {
        Locale prev = this.defaultLocale;
        this.defaultLocale = locale;
        return prev;
    }

    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    public void addMessages(Locale locale, @NotNull Map<MessageKey, String> messages) {
        getTable(locale).addMessages(messages);
    }

    public String addMessage(Locale locale, MessageKey key, String message) {
        return getTable(locale).addMessage(key, message);
    }

    public String getMessage(Locale locale, MessageKey key) {
        String message = getTable(locale).getMessage(key);
        if (message == null && !Objects.equals(locale, defaultLocale)) {
            message = getTable(defaultLocale).getMessage(key);
        }
        if (message == null && !Objects.equals(Locale.ENGLISH, defaultLocale) && !Objects.equals(Locale.ENGLISH, locale)) {
            message = getTable(Locale.ENGLISH).getMessage(key);
        }
        if (message == null) {
            manager.log(LogLevel.ERROR, "Missing Language Key: " + key);
            message = "<MISSING_LANGUAGE_KEY:" + key + ">";
        }
        return message;
    }

    public LanguageTable getTable(Locale locale) {
        return tables.computeIfAbsent(locale, LanguageTable::new);
    }

}
