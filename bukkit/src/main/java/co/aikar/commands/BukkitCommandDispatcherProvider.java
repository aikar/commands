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

import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BukkitCommandDispatcherProvider implements ACFBrigadierProvider {

    private Object commandDispatcher;

    @Override
    public Object getCommandDispatcher() {
        if (commandDispatcher == null) {
            try {
                Class<?> craftServer = Class.forName("CraftServer");
                Field console = craftServer.getDeclaredField("console");
                console.setAccessible(true);

                Class<?> minecraftServer = Class.forName("MinecraftServer");
                Method getCommandDispatcher = minecraftServer.getDeclaredMethod("getCommandDispatcher");
                getCommandDispatcher.setAccessible(true);

                Class<?> commandDispatcher = Class.forName("CommandDispatcher");
                Method getBrigadierDispatcher = commandDispatcher.getDeclaredMethod("a");
                getBrigadierDispatcher.setAccessible(true);

                Object mcServer = console.get(Bukkit.getServer());
                Object commandDispatcherObject = getCommandDispatcher.invoke(mcServer);

                this.commandDispatcher = getBrigadierDispatcher.invoke(commandDispatcherObject);
            } catch (NoSuchMethodException | NoSuchFieldException | ClassNotFoundException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Could not figure get command dispatcher", e);
            }
        }

        return commandDispatcher;
    }
}
