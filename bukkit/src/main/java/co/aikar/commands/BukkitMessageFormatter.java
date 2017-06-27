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

import org.bukkit.ChatColor;

public class BukkitMessageFormatter implements MessageFormatter {
    private final ChatColor color1;
    private final ChatColor color2;
    private final ChatColor color3;

    public BukkitMessageFormatter(ChatColor color1) {
        this(color1, color1);
    }
    public BukkitMessageFormatter(ChatColor color1, ChatColor color2) {
        this(color1, color2, color2);
    }
    public BukkitMessageFormatter(ChatColor color1, ChatColor color2, ChatColor color3) {
        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;
    }
    @Override
    public String c1(String message) {
        return color1 + message;
    }

    @Override
    public String c2(String message) {
        return color2 + message;
    }

    @Override
    public String c3(String message) {
        return color3 + message;
    }
}
