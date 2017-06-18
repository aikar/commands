# Annotation Command Framework (ACF)
## Purpose
This is the Framework created for [Empire Minecraft](https://ref.emc.gs/Aikar?gac=commands.github).

Many people have wanted to use this framework for themselves, So here are, a public ready version!

ACF Core is now game agnostic, and can be used in any Java Server Software that implements a command system.

## Beta Testing
While the 0.x.x series of ACF is "Beta", note that it is very stable.
It has been used for years on EMC. 


It is labeled Beta as the framework is growing fast and gaining new features, and API's are subject to breakage. The new features also may contain bugs, but we will work to fix them fast.

Please be prepared to keep up with changes, but I try to keep them as least disruptful as possible.

See [Using ACF](https://github.com/aikar/commands/wiki/Using-ACF) on how to add ACF to your plugin and using it.

## Targeted Platforms / Current Version

We are on version:
 - GROUP: co.aikar
 - VERSION `0.5.0-SNAPSHOT`

ARTIFACTID varies by platform target:
 * [Bukkit](https://spigotmc.org): ***acf-bukkit*** (For targetting Spigot)
 * [Paper](https://paper.emc.gs): ***acf-paper*** (Recommended for plugins that require Paper, incase Paper specific improvements are added)
 * [Sponge](https://www.spongepowered.org/): ***acf-sponge***
 * [BungeeCord](https://www.spigotmc.org/wiki/bungeecord/): ***acf-bungee*** 
 
You may include more than 1 platform in your jar if your plugin supports multiple platforms.
    
Any bump in version implies an API break. See [CHANGELOG](CHANGELOG.md) for information on migration guides for API breaks.
 
Every change that should not cause any API break will be deployed over the current version.

## Example
```java
@CommandAlias("res|residence|resadmin")
public class ResidenceCommand extends BaseCommand {
    
    @Subcommand("pset")
    @CommandCompletion("@allplayers:30 @flags @flagstates")
    public void onResFlagPSet(Player player, @Flags("admin") Residence res, EmpireUser[] users, String flag, @Values("@flagstates") String state) {
        res.getPermissions().setPlayerFlag(player, Stream.of(users).map(EmpireUser::getName).collect(Collectors.joining(",")), flag, state, resadmin, true);
    }

    
    @Subcommand("area replace")
    @CommandPermission("residence.admin")
    public void onResAreaReplace(Player player, CuboidSelection selection, @Flags("verbose") Residence res, @Default("main") @Single String area) {
        res.replaceArea(player,
            new CuboidArea(selection),
            area,
            resadmin);
    }
    
}
@CommandAlias("group|gr")
public class GroupCommand extends BaseCommand {

    @Subcommand("invitenear|invnear")
    @CommandAlias("invitenear|invnear|ginvnear")
    @Syntax("[radius=32] &e- Invite Nearby Players to the group.")
    public void onInviteNear(Player player, @Default("32") Integer radius) {
        int maxRadius = UserUtil.isModerator(player) ? 256 : 64;
        radius = !UserUtil.isSrStaff(player) ? Math.min(maxRadius, radius) : radius;
        List<String> names = player.getNearbyEntities(radius, Math.min(128, radius), radius)
            .stream().filter((e) -> e instanceof Player && !UserUtil.isVanished((Player) e))
            .map(CommandSender::getName)
            .collect(Collectors.toList());
        Groups.invitePlayers(player, names);
    }

    @Subcommand("invite|inv")
    @CommandAlias("invite|inv|ginv")
    @Syntax("<name> [name2] [name3] &e- Invite Players to the group.")
    public void onInvite(Player player, String[] names) {
        Groups.invitePlayers(player, names);
    }

    @Subcommand("kick|gkick")
    @CommandAlias("gkick")
    @Syntax("<player> &e- Kick Player from the group.")
    public void onKick(Player player, @Flags("leader") Group group, OnlinePlayer toKick) {
        group.kickPlayer(player, toKick.getPlayer());
    }

}
```
## Why does it require Java 8+?
Get off your dinosaur and get on this rocket ship!

Dinosaurs have been dead for a long time, so get off it before you start to smell.

[Download Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

## Contributing
See Issues section. 

Join [#aikar on Spigot IRC - irc.spi.gt](https://aikarchat.emc.gs) to discuss. 

Or [Code With Aikar](https://aikardiscord.emc.gs) Discord.

## Other projects by Aikar / Empire Minecraft
 - [TaskChain](https://taskchain.emc.gs) - Powerful context control to dispatch tasks Async, then access the result sync for API usage. Concurrency controls too.
 - [Minecraft Timings](https://github.com/aikar/minecraft-timings/) - Add Timings to your plugin in a safe way that works on all Bukkit platforms (CraftBukkit - no timings, Spigot - Timings v1, Paper and Paper forks - Timings v2)

## License
As with all my other public projects

Commands (c) Daniel Ennis (Aikar) 2016-2017.

Commands is licensed [MIT](https://tldrlegal.com/license/mit-license). See [LICENSE](LICENSE)


