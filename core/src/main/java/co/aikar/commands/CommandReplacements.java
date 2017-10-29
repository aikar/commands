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

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Manages replacement template strings
 */
public class CommandReplacements {

    private final CommandManager manager;
    private final Map<String, Map.Entry<Pattern, String>> replacements = new LinkedHashMap<>();

    CommandReplacements(CommandManager manager) {
        this.manager = manager;
        addReplacement("truthy", "true|false|yes|no|1|0|on|off");
    }

    public void addReplacements(String... replacements) {
        if (replacements.length == 0 || replacements.length % 2 != 0) {
            throw new IllegalArgumentException("Must pass a number of arguments divisible by 2.");
        }
        for (int i = 0; i < replacements.length; i += 2) {
            addReplacement(replacements[i], replacements[i+1]);
        }
    }

    public String addReplacement(String key, String val) {
        if (this.manager.hasRegisteredCommands()) {
            this.manager.log(LogLevel.ERROR, "You are registering replacements after you have registered your commands!");
            this.manager.log(LogLevel.ERROR, "This is not allowed, and this replacement (" + key + ") will not work for any previously registered command.");
        }

        key = ACFPatterns.PERCENTAGE.matcher(key.toLowerCase()).replaceAll("");
        Pattern pattern = Pattern.compile("%" + Pattern.quote(key) + "\\b", Pattern.CASE_INSENSITIVE);

        Map.Entry<Pattern, String> entry = new AbstractMap.SimpleImmutableEntry<>(pattern, val);
        Map.Entry<Pattern, String> replaced = replacements.put(key, entry);

        if (replaced != null) {
            return replaced.getValue();
        }

        return null;
    }
    public String replace(String text) {
        if (text == null) {
            return null;
        }

        for (Map.Entry<Pattern, String> entry : replacements.values()) {
            text = entry.getKey().matcher(text).replaceAll(entry.getValue());
        }

        return manager.getLocales().replaceI18NStrings(text);
    }
}
