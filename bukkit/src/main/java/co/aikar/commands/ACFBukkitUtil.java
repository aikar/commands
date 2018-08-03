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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ACFBukkitUtil {

    public static String formatLocation(Location loc) {
        if (loc == null) {
            return null;
        }
        return loc.getWorld().getName() +
                ":" +
                loc.getBlockX() +
                "," +
                loc.getBlockY() +
                "," +
                loc.getBlockZ();
    }

    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Move to Message Keys on the CommandIssuer
     * @deprecated
     */
    @Deprecated
    public static void sendMsg(CommandSender player, String message) {
        message = color(message);
        for (String msg : ACFPatterns.NEWLINE.split(message)) {
            player.sendMessage(msg);
        }
    }

    public static Location stringToLocation(String storedLoc) {
        return stringToLocation(storedLoc, null);
    }
    public static Location stringToLocation(String storedLoc, World forcedWorld) {
        if (storedLoc == null) {
            return null;
        }
        String[] args = ACFPatterns.COLON.split(storedLoc);
        if (args.length >= 4 || (args.length == 3 && forcedWorld != null)) {
            String world = forcedWorld != null ? forcedWorld.getName() : args[0];
            int i = args.length == 3 ? 0 : 1;
            double x = Double.parseDouble(args[i]);
            double y = Double.parseDouble(args[i + 1]);
            double z = Double.parseDouble(args[i + 2]);
            Location loc = new Location(Bukkit.getWorld(world), x, y, z);
            if (args.length >= 6) {
                loc.setPitch(Float.parseFloat(args[4]));
                loc.setYaw(Float.parseFloat(args[5]));
            }
            return loc;
        } else if (args.length == 2) {
            String[] args2 = ACFPatterns.COMMA.split(args[1]);
            if (args2.length == 3) {
                String world = forcedWorld != null ? forcedWorld.getName() : args[0];
                double x = Double.parseDouble(args2[0]);
                double y = Double.parseDouble(args2[1]);
                double z = Double.parseDouble(args2[2]);
                return new Location(Bukkit.getWorld(world), x, y, z);
            }
        }
        return null;
    }

    public static String fullLocationToString(Location loc) {
        if (loc == null) {
            return null;
        }
        return (new StringBuilder(64))
                .append(loc.getWorld().getName())
                .append(':')
                .append(ACFUtil.precision(loc.getX(), 4))
                .append(':')
                .append(ACFUtil.precision(loc.getY(), 4))
                .append(':')
                .append(ACFUtil.precision(loc.getZ(), 4))
                .append(':')
                .append(ACFUtil.precision(loc.getPitch(), 4))
                .append(':')
                .append(ACFUtil.precision(loc.getYaw(), 4))
                .toString();
    }

    public static String fullBlockLocationToString(Location loc) {
        if (loc == null) {
            return null;
        }
        return (new StringBuilder(64))
                .append(loc.getWorld().getName())
                .append(':')
                .append(loc.getBlockX())
                .append(':')
                .append(loc.getBlockY())
                .append(':')
                .append(loc.getBlockZ())
                .append(':')
                .append(ACFUtil.precision(loc.getPitch(), 4))
                .append(':')
                .append(ACFUtil.precision(loc.getYaw(), 4))
                .toString();
    }

    public static String blockLocationToString(Location loc) {
        if (loc == null) {
            return null;
        }

        return (new StringBuilder(32))
                .append(loc.getWorld().getName())
                .append(':')
                .append(loc.getBlockX())
                .append(':')
                .append(loc.getBlockY())
                .append(':')
                .append(loc.getBlockZ())
                .toString();
    }

    public static double distance(@NotNull Entity e1, @NotNull Entity e2) {
        return distance(e1.getLocation(), e2.getLocation());
    }
    public static double distance2d(@NotNull Entity e1, @NotNull Entity e2) {
        return distance2d(e1.getLocation(), e2.getLocation());
    }
    public static double distance2d(@NotNull  Location loc1, @NotNull Location loc2) {
        loc1 = loc1.clone();
        loc1.setY(loc2.getY());
        return distance(loc1, loc2);
    }
    public static double distance(@NotNull  Location loc1, @NotNull Location loc2) {
        if (loc1.getWorld() != loc2.getWorld()) {
            return 0;
        }
        return loc1.distance(loc2);
    }

    public static Location getTargetLoc(Player player) {
        return getTargetLoc(player, 128);
    }
    public static Location getTargetLoc(Player player, int maxDist) {
        return getTargetLoc(player, maxDist, 1.5);
    }
    public static Location getTargetLoc(Player player, int maxDist, double addY) {
        try {
            Location target = player.getTargetBlock((Set<Material>) null, maxDist).getLocation();
            target.setY(target.getY() + addY);
            return target;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static Location getRandLoc(Location loc, int radius) {
        return getRandLoc(loc, radius, radius, radius);
    }
    public static Location getRandLoc(Location loc, int xzRadius, int yRadius) {
        return getRandLoc(loc, xzRadius, yRadius, xzRadius);
    }
    @NotNull public static Location getRandLoc(Location loc, int xRadius, int yRadius, int zRadius) {
        Location newLoc = loc.clone();
        newLoc.setX(ACFUtil.rand(loc.getX()-xRadius, loc.getX()+xRadius));
        newLoc.setY(ACFUtil.rand(loc.getY()-yRadius, loc.getY()+yRadius));
        newLoc.setZ(ACFUtil.rand(loc.getZ()-zRadius, loc.getZ()+zRadius));
        return newLoc;
    }


    public static String removeColors(String msg) {
        return ChatColor.stripColor(color(msg));
    }

    public static String replaceChatString(String message, String replace, String with) {
        return replaceChatString(message, Pattern.compile(Pattern.quote(replace), Pattern.CASE_INSENSITIVE), with);
    }
    public static String replaceChatString(String message, Pattern replace, String with) {
        final String[] split = replace.split(message + "1");

        if (split.length < 2) {
            return replace.matcher(message).replaceAll(with);
        }
        message = split[0];

        for (int i = 1; i < split.length; i++) {
            final String prev = ChatColor.getLastColors(message);
            message += with + prev + split[i];
        }
        return message.substring(0, message.length() - 1);
    }

    public static boolean isWithinDistance(@NotNull Player p1, @NotNull Player p2, int dist) {
        return isWithinDistance(p1.getLocation(), p2.getLocation(), dist);
    }
    public static boolean isWithinDistance(@NotNull Location loc1, @NotNull Location loc2, int dist) {
        return loc1.getWorld() == loc2.getWorld() && loc1.distance(loc2) <= dist;
    }

    /**
     * Please move to the CommandIssuer version
     * @deprecated
     */
    public static Player findPlayerSmart(CommandSender requester, String search) {
        CommandManager manager = CommandManager.getCurrentCommandManager();
        if (manager != null) {
            return findPlayerSmart(manager.getCommandIssuer(requester), search);
        }
        throw new IllegalStateException("You may not use the ACFBukkitUtil#findPlayerSmart(CommandSender) async to the command execution.");
    }

    public static Player findPlayerSmart(CommandIssuer issuer, String search) {
        CommandSender requester = issuer.getIssuer();
        if (search == null) {
            return null;
        }
        String name = ACFUtil.replace(search, ":confirm", "");

        if (!isValidName(name)) {
            issuer.sendError(MinecraftMessageKeys.IS_NOT_A_VALID_NAME, "{name}", name);
            return null;
        }

        List<Player> matches = Bukkit.getServer().matchPlayer(name);
        List<Player> confirmList = new ArrayList<>();
        findMatches(search, requester, matches, confirmList);


        if (matches.size() > 1 || confirmList.size() > 1) {
            String allMatches = matches.stream().map(Player::getName).collect(Collectors.joining(", "));
            issuer.sendError(MinecraftMessageKeys.MULTIPLE_PLAYERS_MATCH,
                    "{search}", name, "{all}", allMatches);
            return null;
        }

        //noinspection Duplicates
        if (matches.isEmpty()) {
            if (confirmList.isEmpty()) {
                issuer.sendError(MinecraftMessageKeys.NO_PLAYER_FOUND_SERVER,
                        "{search}", name);
                return null;
            } else {
                Player player = ACFUtil.getFirstElement(confirmList);
                issuer.sendInfo(MinecraftMessageKeys.PLAYER_IS_VANISHED_CONFIRM, "{vanished}", player.getName());
                return null;
            }
        }

        return matches.get(0);
    }

    private static void findMatches(String search, CommandSender requester, List<Player> matches, List<Player> confirmList) {
        // Remove vanished players from smart matching.
        Iterator<Player> iter = matches.iterator();
        //noinspection Duplicates
        while (iter.hasNext()) {
            Player player = iter.next();
            if (requester instanceof Player && !((Player) requester).canSee(player)) {
                if (requester.hasPermission("acf.seevanish")) {
                    if (!search.endsWith(":confirm")) {
                        confirmList.add(player);
                        iter.remove();
                    }
                } else {
                    iter.remove();
                }
            }
        }
    }


    public static boolean isValidName(String name) {
        return name != null && !name.isEmpty() && ACFPatterns.VALID_NAME_PATTERN.matcher(name).matches();
    }

    static boolean isValidItem(ItemStack item) {
        return item != null && item.getType() != Material.AIR && item.getAmount() > 0;
    }
}
