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

import java.util.List;
import java.util.function.Function;

public class ItemPaginator<T> implements Paginator {
    private static final int PAGE_CAP = 9;
    private final String header;
    private final List<T> items;
    private final Function<? super T, ? extends String> stringMapper;

    public ItemPaginator(List<T> items) {
        this("&eResults", items);
    }

    public ItemPaginator(String header, List<T> items) {
        this(header, items, Object::toString);
    }

    public ItemPaginator(String header, List<T> items, Function<? super T, ? extends String> stringMapper) {
        this.header = ChatColor.translateAlternateColorCodes('&', header);
        this.items = items;
        this.stringMapper = stringMapper;
    }

    /**
     * Gets the requested page of results.
     * @param page the requested page
     * @return the requested page of results
     * @throws IllegalStateException
     */
    @Override
    public final void sendPage(CommandSender sender, int page) throws IllegalStateException {
        if (items.size() == 0) {
            throw new IllegalStateException("No such items exist.");
        }

        page -= 1;

        int maximumPages = items.size() / PAGE_CAP;

        if (items.size() % PAGE_CAP == 0) {
            maximumPages--;
        }

        if (page > maximumPages) {
            sender.sendMessage(getInvalidPage());
            return;
        }

        page = Math.max(0, Math.min(page, maximumPages));

        sender.sendMessage(formatHeader(header, (page + 1), (maximumPages + 1)));
        for (int i = (PAGE_CAP * page); ((i < (PAGE_CAP * page) + PAGE_CAP) && i < items.size()); i++) {
            sender.sendMessage(formatLine(stringMapper.apply(items.get(i))));
        }
    }
}