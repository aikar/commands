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

    public BukkitBrigadierManager(CommandManager<?, ?, ?, ?, ?, ?> manager, ACFBrigadierProvider provider) {
        super(manager, provider);

        //TODO custom argument types?
//        registerArgument(Player.class, new ArgumentType<Player>() {
//            @Override
//            public Player parse(StringReader reader) throws CommandSyntaxException {
//                return Bukkit.getPlayer(reader.readString());
//            }
//
//            @Override
//            public Collection<String> getExamples() {
//                List<String> list = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
//                list.add("TEST");
//                return list;
//            }
//
//            @Override
//            public String toString() {
//                return super.toString();
//            }
//        });
    }

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
}
