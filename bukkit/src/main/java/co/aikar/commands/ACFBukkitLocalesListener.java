/*
 * Copyright (c) 2016-2025 Daniel Ennis (Aikar) - MIT License
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

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLocaleChangeEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

class ACFBukkitLocalesListener implements Listener {

    private final BukkitCommandManager manager;

    ACFBukkitLocalesListener(BukkitCommandManager manager) {
        this.manager = manager;
    }

    @EventHandler
    void onLocaleChange(PlayerLocaleChangeEvent event) {
        if (!manager.autoDetectFromClient) {
            return;
        }
        Player player = event.getPlayer();
        Locale locale = null;
        try {
            locale = event.locale();
        } catch (NoSuchMethodError ignored) {
            try {
                if (!event.getLocale().equals(manager.issuersLocaleString.get(player.getUniqueId()))) {
                    locale = ACFBukkitUtil.stringToLocale(event.getLocale());
                }
            } catch (NoSuchMethodError ignored2) {
                try {
                    Method getNewLocale = event.getClass().getMethod("getNewLocale");
                    getNewLocale.setAccessible(true);
                    String value = (String) getNewLocale.invoke(event);
                    if (!value.equals(manager.issuersLocaleString.get(player.getUniqueId()))) {
                        locale = ACFBukkitUtil.stringToLocale(value);
                    }
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored3) {
                }
            }
        }
        if (locale == null) {
            return;
        }
        manager.setPlayerLocale(player, locale);
    }
}
