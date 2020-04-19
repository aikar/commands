/*
 * Copyright (c) 2016-2018 Daniel Ennis (Aikar) - MIT License
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

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Brigadier Manager that hacks craftbukkit to handle command suggestions
 *
 * @param <S>
 * @author MiniDigger
 * @deprecated Unstable API
 */
@Deprecated
@UnstableAPI
public class BukkitBrigadierManager<S> extends ACFBrigadierManager<S> {

    private static final String SERVER_VERSION = getServerVersion();


    private static Object bukkitCommandWrapper;
    private static Method getSuggestionsMethod;

    static {
        try {
            Class<?> craftServerClass = Class.forName("org.bukkit.craftbukkit" + SERVER_VERSION + "CraftServer");
            Class<?> bukkitCommandWrapperClass = Class.forName("org.bukkit.craftbukkit" + SERVER_VERSION + "command.BukkitCommandWrapper");
            bukkitCommandWrapper = bukkitCommandWrapperClass.getConstructor(craftServerClass, Command.class).newInstance(Bukkit.getServer(), null);

            getSuggestionsMethod = bukkitCommandWrapperClass.getDeclaredMethod("getSuggestions", CommandContext.class, SuggestionsBuilder.class);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructs a new brigadier manager, utilizing the currently active command manager and an brigadier provider.
     *
     * @param manager
     * @param provider
     */
    public BukkitBrigadierManager(CommandManager<?, ?, ?, ?, ?, ?> manager, ACFBrigadierProvider provider) {
        super(manager, provider);
    }

    public BukkitBrigadierManager(CommandManager<?, ?, ?, ?, ?, ?> manager) {
        super(manager, new BukkitCommandDispatcherProvider());
    }

    @Override
    protected void registerACF(BaseCommand command) {
// try to register only to acf, not to bukkit, didnt work as expected, we wil need to register to bukkit
//        BukkitCommandManager bukkitCommandManager = (BukkitCommandManager) manager;
//        command.onRegister(manager);
//        for (Map.Entry<String, RootCommand> entry : command.registeredCommands.entrySet()) {
//            String commandName = entry.getKey().toLowerCase(Locale.ENGLISH);
//            BukkitRootCommand bukkitCommand = (BukkitRootCommand) entry.getValue();
//            bukkitCommand.isRegistered = true;
//            bukkitCommandManager.registeredCommands.put(commandName, bukkitCommand);
//        }

        manager.registerCommand(command);
    }

    private static String getServerVersion() {
        Class<?> server = Bukkit.getServer().getClass();
        if (!server.getSimpleName().equals("CraftServer")) {
            return ".";
        }
        if (server.getName().equals("org.bukkit.craftbukkit.CraftServer")) {
            // Non versioned class
            return ".";
        } else {
            String version = server.getName().substring("org.bukkit.craftbukkit".length());
            return version.substring(0, version.length() - "CraftServer".length());
        }
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        try {
            //noinspection unchecked
            return (CompletableFuture<Suggestions>) getSuggestionsMethod.invoke(bukkitCommandWrapper, context, builder);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return builder.buildFuture();
    }

    @Override
    public int run(CommandContext<S> commandContext) throws CommandSyntaxException {
        return 0;
    }

    @Override
    public boolean test(S s) {
        return false;
    }
}
