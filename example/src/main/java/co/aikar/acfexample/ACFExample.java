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
import co.aikar.commands.MessageKeys;
import co.aikar.commands.MessageType;
import com.google.common.collect.Lists;
import org.bukkit.plugin.java.JavaPlugin;

public final class ACFExample extends JavaPlugin {

    private static ACFExample plugin;
    private static BukkitCommandManager commandManager;
    @Override
    public void onEnable() {
        plugin = this;
        registerCommands();
    }
    //is called by the Spigot / Bukkit server
    private void registerCommands() {
        //calls the new CommandManger through ACF
        commandManager = new BukkitCommandManager(this);
        //adds a replacementmethod for calling the main command with an alias, note these must be csv format
        //and if you are using a % it is considered a wildcard????
        commandManager.getCommandReplacements().addReplacements("test", "foobar", "%foo", "barbaz");
        //this line adds a replacement style with or statements?? 
        commandManager.getCommandReplacements().addReplacement("testcmd", "test4|foobar|barbaz");
        //register the contexts for the command?   SomeObject.class is this a literal such as YourCommand.class? 
        //                                         YourCommand.getContextResolver(), that is a method however must be for some reason static
        commandManager.getCommandContexts().registerContext(SomeObject.class, SomeObject.getContextResolver());
        //registers the completion for the command test! with the results listed in the array
        commandManager.getCommandCompletions().registerCompletion("test", c -> (
            Lists.newArrayList("foo", "bar", "baz")
        ));
        //registers the command with bukkit / spigot SomeCommand() is a class and must be replaced by YourCommandClass
        //setExceptionHandler 
        commandManager.registerCommand(new SomeCommand().setExceptionHandler((command, registeredCommand, sender, args, t) -> {
                sender.sendMessage(MessageType.ERROR, MessageKeys.ERROR_GENERIC_LOGGED);
                return true; // mark as handeled, default message will not be send to sender
        }));
        //registers the command fromt he method found in ??
        commandManager.registerCommand(new SomeCommand_ExtraSubs());

        commandManager.setDefaultExceptionHandler((command, registeredCommand, sender, args, t) -> {
            getLogger().warning("Error occured while executing command " + command.getName());
            return false; // mark as unhandeled, sender will see default message
        });
    }
    //this should already be in your plugin as a initializer for the static method private static YourPluginMainClass handle;
    public static ACFExample getPlugin() {
        return plugin;
    }
    //this returns the value from line 35
    public static BukkitCommandManager getCommandManager() {
        return commandManager;
    }
}
