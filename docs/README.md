# Annotation Command Framework (ACF)

## Purpose

This is the command Framework originally created for [Empire Minecraft](https://ref.emc.gs/Aikar?gac=commands.github).

ACF Started as a Bukkit Command Framework, but has shifted to be platform-agnostic and can be used on any Java based
application.

ACF is an extremely powerful command framework that takes nearly every concept of boilerplate code commonly found in
command handlers, and abstracts them away behind annotations.

ACF redefines how you build your command handlers, allowing things such as Dependency Injection, Validation, Tab
Completion, Help Documentation, Syntax Advice, and Stateful Conditions to all be behind Annotations that you place on
methods.

Clean up your command handlers and unleash rich command experiences that would be too burdensome to pull off manually.

## Beta Testing

While the 0.x.x series of ACF is "Beta", note that it is very stable. It has been used for years on EMC.

It is labeled Beta as the framework is growing and gaining new features, and API's are subject to breakage. The new
features also may contain bugs, but we will work to fix them.

Branches previxed with dev/XXX indicate that the branch is likely to be very unstable, possibly completely
non-functional.
May also not have the new changes implemented to that branch yet.

v1.0.0 will be the signal that ACF features are more complete and the API will remain stable for a long time.

## Documentation / Using ACF

[Documentation Wiki](https://github.com/aikar/commands/wiki) - All of ACF's documentation is currently on the GitHub
Wiki. Please review every page to learn about each feature.

See [Using ACF](https://github.com/aikar/commands/wiki/Using-ACF) on how to add ACF to your plugin and getting started.

See [Examples](https://github.com/aikar/commands/wiki/Real-World-Examples) for some real world examples

## Targeted Platforms / Current Version

We are on version:

- GROUP: co.aikar
- VERSION `0.5.1-SNAPSHOT`

ARTIFACTID varies by platform target:

* [Bukkit](https://spigotmc.org): ***acf-bukkit*** (For targetting Bukkit/Spigot)
* [Paper](https://paper.emc.gs): ***acf-paper*** (Recommended over Bukkit, will gradually enhance when ran on Paper, but
  still runs on Spigot)
* [Sponge](https://www.spongepowered.org/): ***acf-sponge***
* [BungeeCord](https://www.spigotmc.org/wiki/bungeecord/): ***acf-bungee***
* [JDA](https://github.com/DV8FromTheWorld/JDA): ***acf-jda*** - IN PROGRESS - NOT READY

Setup Guides (Repo and Requirements): [Maven](https://github.com/aikar/commands/wiki/Maven-Setup)
, [Gradle](https://github.com/aikar/commands/wiki/Gradle-Setup)
You may include more than 1 platform in your jar if your plugin supports multiple platforms.

Any bump in version implies an API break. See [CHANGELOG](CHANGELOG.md) for information on migration guides for API
breaks.

Every change that should not cause any API break will be deployed over the current version.

## Say Thanks

If this library has helped you, please consider donating as a way of saying thanks

[![PayPal Donate](https://aikar.co/donate.png "Donate with PayPal")](https://paypal.me/empireminecraft)

# Java Docs

- [ACF (Core)](https://aikar.github.io/commands/acf-core)
- [ACF (Bukkit)](https://aikar.github.io/commands/acf-bukkit)
- [ACF (Sponge)](https://aikar.github.io/commands/acf-sponge)
- [ACF (Bungee)](https://aikar.github.io/commands/acf-bungee)
- [ACF (JDA)](https://aikar.github.io/commands/acf-jda)

## Contributing

See Issues section.

Join [#aikar on Spigot IRC - irc.spi.gt](https://aikarchat.emc.gs) to discuss.

Or [Code With Aikar](https://aikardiscord.emc.gs) Discord.

## Other projects by Aikar / Empire Minecraft

- [TaskChain](https://taskchain.emc.gs) - Powerful context control to dispatch tasks Async, then access the result sync
  for API usage. Concurrency controls too.
- [IDB](https://idb.emc.gs) - Simple and Intuitive JDBC Wrapper for Java
- [Minecraft Timings](https://github.com/aikar/minecraft-timings/) - Add Timings to your plugin in a safe way that works
  on all Bukkit platforms (CraftBukkit - no timings, Spigot - Timings v1, Paper and Paper forks - Timings v2)

## License

As with all my other public projects

Commands (c) Daniel Ennis (Aikar) 2016-2022.

Commands is licensed [MIT](https://tldrlegal.com/license/mit-license). See [LICENSE](LICENSE)


