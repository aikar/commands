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
import org.bukkit.command.CommandSender;

/**
 * Created by simple on 4/27/2017.
 */
@FunctionalInterface
public interface Paginator {
    /**
     * Gets the requested page number from this {@link Paginator}.
     *
     * @param page the requested page
     * @return the paginated contents
     * @throws IllegalStateException
     */
    void sendPage(CommandSender sender, int page) throws IllegalStateException;

    /**
     * Formats the header for display to the sender.
     *
     * @param header the raw header
     * @param page the requested page
     * @param maximumPages the maximum page count
     * @return the formatted header
     */
    default String formatHeader(String header, int page, int maximumPages) {
        return ChatColor.YELLOW + "=== " + header + ChatColor.YELLOW + " (Page " + page + " of " + maximumPages + ") ===";
    }

    /**
     * Formats a line for display to the sender.
     *
     * @param line the line
     * @return the formatted line
     */
    default String formatLine(String line) {
        return ChatColor.LIGHT_PURPLE + "> " + ChatColor.YELLOW + line;
    }

    default String getInvalidPage() {
        return ChatColor.RED + "No such page exists!";
    }
}
