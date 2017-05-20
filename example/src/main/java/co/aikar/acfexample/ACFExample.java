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

package co.aikar.acfexample;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.CommandManager;
import com.google.common.collect.Lists;
import org.bukkit.plugin.java.JavaPlugin;

public final class ACFExample extends JavaPlugin {

    private static ACFExample plugin;
    private static CommandManager commandManager;
    @Override
    public void onEnable() {
        plugin = this;
        registerCommands();
    }

    private void registerCommands() {
        commandManager = new BukkitCommandManager(this);
        commandManager.getCommandReplacements().addReplacements("test", "foobar", "%foo", "barbaz");
        commandManager.getCommandReplacements().addReplacement("testcmd", "test4|foobar|barbaz");
        commandManager.getCommandContexts().registerContext(SomeObject.class, SomeObject.getContextResolver());
        commandManager.getCommandCompletions().registerCompletion("test", (sender, config, input, c) -> (
            Lists.newArrayList("foo", "bar", "baz")
        ));
        commandManager.registerCommand(new SomeCommand());
        commandManager.registerCommand(new SomeCommand_ExtraSubs());
    }

    public static ACFExample getPlugin() {
        return plugin;
    }

    public static CommandManager getCommandManager() {
        return commandManager;
    }
}
