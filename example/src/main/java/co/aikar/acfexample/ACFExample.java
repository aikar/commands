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

import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.MessageKeys;
import co.aikar.commands.MessageType;
import co.aikar.commands.PaperBrigadierManager;
import co.aikar.commands.PaperCommandManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public final class ACFExample extends JavaPlugin {

    private static ACFExample plugin;
    private static PaperCommandManager commandManager;

    @Override
    public void onEnable() {
        plugin = this;
        registerCommands();
    }

    private void registerCommands() {
        // 1: Create Command Manager for your respective platform
        commandManager = new PaperCommandManager(this);

        // enable brigadier integration for paper servers
        commandManager.enableUnstableAPI("brigadier");

        // optional: enable unstable api to use help
        commandManager.enableUnstableAPI("help");

        // 2: Setup some replacement values that may be used inside of the annotations dynamically.
        commandManager.getCommandReplacements().addReplacements(
                // key - value
                "test", "foobar",
                // key - demonstrate that % is ignored  - value
                "%foo", "barbaz");
        // Another replacement for piped values
        commandManager.getCommandReplacements().addReplacement("testcmd", "test4|foobar|barbaz");

        // 3: Register Custom Command Contexts
        commandManager.getCommandContexts().registerContext(
                /* The class of the object to resolve*/
                SomeObject.class,
                /* A resolver method - Placed the resolver in its own class for organizational purposes */
                SomeObject.getContextResolver());

        // 4: Register Command Completions - this will be accessible with @CommandCompletion("@test")
        commandManager.getCommandCompletions().registerAsyncCompletion("test", c ->
                Arrays.asList("foo123", "bar123", "baz123")
        );

        // 5: Register Command Conditions
        commandManager.getCommandConditions().addCondition(SomeObject.class, "limits", (c, exec, value) -> {
            if (value == null) {
                return;
            }
            if (c.hasConfig("min") && c.getConfigValue("min", 0) > value.getValue()) {
                throw new ConditionFailedException("Min value must be " + c.getConfigValue("min", 0));
            }
            if (c.hasConfig("max") && c.getConfigValue("max", 3) < value.getValue()) {
                throw new ConditionFailedException("Max value must be " + c.getConfigValue("max", 3));
            }
        });

        // 6: (Optionally) Register dependencies - Dependencies can be injected into fields of command classes by
        // marking them with @Dependency. Some classes, like your Plugin, are already registered by default.
        SomeHandler someHandler = new SomeHandler();
        someHandler.setSomeField("Secret");
        commandManager.registerDependency(SomeHandler.class, someHandler);
        commandManager.registerDependency(String.class, "Test3");
        commandManager.registerDependency(String.class, "test", "Test");
        commandManager.registerDependency(String.class, "test2", "Test2");

        // 7: Register your commands - This first command demonstrates adding an exception handler to that command
        commandManager.registerCommand(new SomeCommand().setExceptionHandler((command, registeredCommand, sender, args, t) -> {
            sender.sendMessage(MessageType.ERROR, MessageKeys.ERROR_GENERIC_LOGGED);
            return true; // mark as handeled, default message will not be send to sender
        }));

        // 8: Register an additional command. This one happens to share the same CommandAlias as the previous command
        // This means it simply registers additional sub commands under the same command, but organized into separate
        // Classes (Maybe different permission sets)
        commandManager.registerCommand(new SomeCommand_ExtraSubs());
        commandManager.registerCommand(new SomeOtherCommand());

        // 9: Register default exception handler for any command that doesn't supply its own
        commandManager.setDefaultExceptionHandler((command, registeredCommand, sender, args, t) -> {
            getLogger().warning("Error occurred while executing command " + command.getName());
            return false; // mark as unhandeled, sender will see default message
        });

        // test command for brigadier
        commandManager.getCommandCompletions().registerAsyncCompletion("someobject", c ->
                Arrays.asList("1", "2", "3", "4", "5")
        );
        commandManager.registerCommand(new BrigadierTest());
    }

    // Typical Bukkit Plugin Scaffolding
    public static ACFExample getPlugin() {
        return plugin;
    }

    // A way to access your command manager from other files if you do not use a Dependency Injection approach
    public static PaperCommandManager getCommandManager() {
        return commandManager;
    }
}
