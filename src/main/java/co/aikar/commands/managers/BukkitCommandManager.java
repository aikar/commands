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

package co.aikar.commands.managers;

import co.aikar.commands.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class BukkitCommandManager implements CommandManager {

    @SuppressWarnings("WeakerAccess")
    protected final Plugin plugin;

    public BukkitCommandManager(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean register(BaseCommand command) {
        return Bukkit.getServer().getCommandMap().register(this.plugin.getDescription().getName(), command);
    }

    @Override
    public boolean register(BaseCommand command, String commandName) {
        return Bukkit.getServer().getCommandMap().register(commandName, this.plugin.getDescription().getName(), command);
    }
}
