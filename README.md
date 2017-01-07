# Aikar Commands
## WARNING!!! PRE-ALPHA
This is a rough import of my command framework to be a standalone Library. This project is currently extremely volatile and will receive drastic changes.

We haven't even decided a name yet!

## Targetted Platforms
Requires CraftBukkit, Spigot or Paper.
Sponge maybe in future if we can

## Purpose
This is the Framework used on [Empire Minecraft](https://ref.emc.gs/Aikar?gac=commands.github).

Many people have wanted to use this framework for themselves, so I am trying to make it general purpose (with help from the community) so that others can use it just as simple as they can [TaskChain](https://taskchain.emc.gs) now.

## Example
```java
@CommandAlias("res|residence|resadmin")
public class ResidenceCommand extends co.aikar.commands.BaseCommand {
    
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
@CommandAlias("gr")
public class GroupCommand extends co.aikar.commands.BaseCommand {
    public GroupCommand() {
        super("group");
    }
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

## Contributing
See Issues section. Lots to decide and frame out. 

Join [#aikar on Spigot IRC - irc.spi.gt](https://aikarchat.emc.gs) to discuss. 

Or [Code With Aikar](https://aikardiscord.emc.gs) Discord.


## License
As with all my other public projects

Commands (c) Daniel Ennis (Aikar) 2016.

Commands is licensed [MIT](https://tldrlegal.com/license/mit-license). See [LICENSE](LICENSE)


