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

import java.util.*;
import java.util.regex.Matcher;

/**
 * Handles formatting Messages and managing colors
 * @param <C> The platform specific color object
 */
@Deprecated
public abstract class MessageFormatter <C> {

    private final List<C> colors = new ArrayList<>();

    public MessageFormatter(C... colors) {
        for (int i = 0; i < colors.length; i++) {
            this.colors.set(i, colors[i]);
        }

    }
    public C setColor(int index, C color) {
        if (this.colors.size() < index) {
            this.colors.addAll(Collections.nCopies(index - this.colors.size(), null));
        }
        return colors.set(index, color);
    }

    public C getColor(int index) {
        C color = colors.get(index);
        if (color == null) {
            color = getDefaultColor();
        }
        return color;
    }

    public C getDefaultColor() {
        return getColor(1);
    }

    abstract String format(C color, String message);

    public String format(int index, String message) {
        return format(getColor(index), message);
    }

    public String format(String message) {
        String def = format(1, "");
        Matcher matcher = ACFPatterns.FORMATTER.matcher(message);
        StringBuffer sb = new StringBuffer(message.length());
        while (matcher.find()) {
            Integer color = ACFUtil.parseInt(matcher.group("color"), 1);
            String msg = format(color, matcher.group("msg")) + def;
            matcher.appendReplacement(sb, Matcher.quoteReplacement(msg));
        }
        matcher.appendTail(sb);
        return def + sb.toString();
    }
}
