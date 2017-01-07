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

package co.aikar.commands.contexts;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Split;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Values;
import co.aikar.commands.CommandLog;
import co.aikar.commands.CommandPatterns;
import co.aikar.commands.SneakyThrow;
import co.aikar.commands.CommandUtil;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public final class CommandContexts {
    private static final Map<Class<?>, ContextResolver<?>> contextMap = Maps.newHashMap();

    private CommandContexts() {}

    public static void initialize() {
        registerContext(Integer.class, (c) -> {
            try {
                return CommandUtil.parseNumber(c.popFirstArg(), c.hasFlag("suffixes")).intValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument("Must be a number");
            }
        });
        registerContext(Long.class, (c) -> {
            try {
                return CommandUtil.parseNumber(c.popFirstArg(), c.hasFlag("suffixes")).longValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument("Must be a number");
            }

        });
        registerContext(Float.class, (c) -> {
            try {
                return CommandUtil.parseNumber(c.popFirstArg(), c.hasFlag("suffixes")).floatValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument("Must be a number");
            }
        });
        registerContext(Double.class, (c) -> {
            try {
                return CommandUtil.parseNumber(c.popFirstArg(), c.hasFlag("suffixes")).doubleValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument("Must be a number");
            }
        });
        registerContext(Number.class, (c) -> {
            try {
                return CommandUtil.parseNumber(c.popFirstArg(), c.hasFlag("suffixes"));
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument("Must be a number");
            }
        });
        registerContext(Boolean.class, (c) -> CommandUtil.isTruthy(c.popFirstArg()));
        registerContext(String.class, (c) -> {
            final Values values = c.getParam().getAnnotation(Values.class);
            if (values != null) {
                return c.popFirstArg();
            }
            if (c.isLastArg() && c.getParam().getAnnotation(Single.class) == null) {
                return CommandUtil.join(c.getArgs());
            }
            return c.popFirstArg();
        });
        registerContext(String[].class, (c) -> {
            String val;
            if (c.isLastArg() && c.getParam().getAnnotation(Single.class) == null) {
                val = CommandUtil.join(c.getArgs());
            } else {
                val = c.popFirstArg();
            }
            Split split = c.getParam().getAnnotation(Split.class);
            if (split != null) {
                if (val.isEmpty()) {
                    throw new InvalidCommandArgument();
                }
                return CommandPatterns.getPattern(split.value()).split(val);
            } else if (!c.isLastArg()) {
                SneakyThrow.sneaky(new InvalidConfigurationException("Weird Command signature... String[] should be last or @Split"));
            }

            String[] result = c.getArgs().toArray(new String[c.getArgs().size()]);
            c.getArgs().clear();
            return result;
        });

        registerContext(OnlinePlayer.class, (c) -> {
            final String playercheck = c.popFirstArg();
            Player player = CommandUtil.findPlayerSmart(c.getSender(), playercheck);
            if (player == null) {
                CommandUtil.sendMsg(c.getSender(), "&cCould not find a player by the name " + playercheck);
                throw new InvalidCommandArgument(false);
            }
            return new OnlinePlayer(player);
        });
        registerSenderAwareContext(World.class, (c) -> {
            String firstArg = c.getFirstArg();
            World world = firstArg != null ? Bukkit.getWorld(firstArg) : null;
            if (world != null) {
                c.popFirstArg();
            }
            if (world == null && c.getSender() instanceof Player) {
                world = ((Entity) c.getSender()).getWorld();
            }
            if (world == null) {
                throw new InvalidCommandArgument("Invalid World");
            }
            return world;
        });
        registerSenderAwareContext(CommandSender.class, CommandExecutionContext::getSender);
        registerSenderAwareContext(Player.class, (c) -> {
            Player player = c.getSender() instanceof Player ? (Player) c.getSender() : null;
            if (player == null && !c.hasAnnotation(Optional.class)) {
                throw new InvalidCommandArgument("Requires a player to run this command", false);
            }
            if (player != null && c.hasFlag("itemheld") && !isValidItem(player.getInventory().getItemInMainHand())) {
                throw new InvalidCommandArgument("You must be holding an item in your main hand.", false);
            }
            return player;
        });
        registerContext(Enum.class, (c) -> {
            final String first = c.popFirstArg();
            Class<? extends Enum<?>> enumCls = (Class<? extends Enum<?>>) c.getParam().getType();
            Enum<?> match = CommandUtil.simpleMatch(enumCls, first);
            if (match == null) {
                List<String> names = CommandUtil.enumNames(enumCls);
                throw new InvalidCommandArgument("Please specify one of: " + CommandUtil.join(names));
            }
            return match;
        });
    }

    public static <T> void registerSenderAwareContext(Class<T> context, SenderAwareContextResolver<T> supplier) {
        contextMap.put(context, supplier);
    }
    public static <T> void registerContext(Class<T> context, ContextResolver<T> supplier) {
        contextMap.put(context, supplier);
    }

    public static ContextResolver<?> getResolver(Class<?> type) {
        Class<?> rootType = type;
        do {
            if (type == Object.class) {
                break;
            }

            final ContextResolver<?> resolver = contextMap.get(type);
            if (resolver != null) {
                return resolver;
            }
        } while ((type = type.getSuperclass()) != null);

        CommandLog.exception(new InvalidConfigurationException("No context resolver defined for " + rootType.getName()));
        return null;
    }
    private static boolean isValidItem(ItemStack item) {
        return item != null && item.getType() != Material.AIR && item.getAmount() > 0;
    }
}
