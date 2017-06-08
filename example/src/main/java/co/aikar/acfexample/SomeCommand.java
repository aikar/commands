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

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Values;
import co.aikar.commands.contexts.OnlinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("acf|somecommand|sc|somcom")
public class SomeCommand extends BaseCommand {

    @CommandAlias("%testcmd")
    public void onCommand(CommandSender sender, SomeObject someObject) {
        sender.sendMessage("You got an object of type: " + someObject.getClass().getName() + " with a value of: " + someObject.getValue());
    }

    @Subcommand("admin")
    @CommandPermission("some.perm")
    @CommandAlias("acfadmin|acfa")
    public void onAdminCommand(Player player) {
        player.sendMessage("You got permission!");
    }

    @Subcommand("optional")
    @CommandAlias("acfoptional|acfo")
    public void onOptional(CommandSender sender, @Optional String opt) {
        if (opt == null) {
            sender.sendMessage("You did not supply an option.");
        } else {
            sender.sendMessage("You supplied: " + opt);
        }
    }
    @Subcommand("default")
    @CommandAlias("acfdefault|acfd")
    public void onTestDefault(CommandSender sender, @Default("Unknown User") String name) {
        sender.sendMessage("Hello, " + name);
    }

    @Subcommand("completions")
    @CommandAlias("acfcompletions|acfc")
    @CommandCompletion("@players @worlds @test foo1|foo2|foo3")
    public void onTestCompletion(CommandSender sender, OnlinePlayer player, World world, String test, String misc) {
        sender.sendMessage("You got " + player.getPlayer().getName() + " - " + world.getName() + " - " + test + " - " + misc);
    }


    @Subcommand("testsub test1")
    @CommandCompletion("Foo")
    public void onTestSub1(CommandSender sender, String hi) {
        sender.sendMessage(hi);
    }

    @Subcommand("test|txt|tfoo")
    public class Test extends BaseCommand {

        @Subcommand("test1|td1")
        @CommandCompletion("%foo")
        public void onTest1(Player player, String testX) {
            player.sendMessage("You got test inner test1: " + testX);
        }
        @Subcommand("test2|td2|%test")
        @CommandCompletion("%test")
        @CommandPermission("%test")
        public void onTest2(Player player, @Values("%test") String testY) {
            player.sendMessage("You got test inner test2: " + testY);
        }

        @Subcommand("next")
        public class TestInner extends BaseCommand {

            @Subcommand("test3|td4")
            @CommandCompletion("FOO")
            public void onTest1(Player player, String testX) {
                player.sendMessage("You got test inner inner test3: " + testX);
            }
            @CommandAlias("deepinner")
            @Subcommand("test4|td4")
            @CommandCompletion("BAR")
            public void onTest2(Player player, String testY) {
                player.sendMessage("You got test inner inner test4: " + testY);
            }
        }
    }
}
