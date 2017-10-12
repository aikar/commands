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

    // %testcmd was defined in ACFExample plugin and defined as "test4|foobar|barbaz"
    // This means, /test4, /foobar and /barbaz all are aliased here.
    // functionally equivalent to @CommandAlias("test4|foobar|barbaz") but could be dynamic (Read from config)
    // Any @CommandAlias implies an automatic @Subcommand too, so this is also accessible from /acf test4
    @CommandAlias("%testcmd")
    public void onCommand(CommandSender sender, SomeObject someObject) {
        sender.sendMessage("You got an object of type: " + someObject.getClass().getName() + " with a value of: " + someObject.getValue());
    }

    // /acf admin - requires permission some.perm
    // May also be accessed with /acfa or /acfadmin
    @Subcommand("admin")
    @CommandPermission("some.perm")
    @CommandAlias("acfadmin|acfa")
    public void onAdminCommand(Player player) {
        player.sendMessage("You got permission!");
    }

    // Has an optional parameter opt, /acfo and /acfo <something> both work
    @Subcommand("optional")
    @CommandAlias("acfoptional|acfo")
    public void onOptional(CommandSender sender, @Optional String opt) {
        if (opt == null) {
            sender.sendMessage("You did not supply an option.");
        } else {
            sender.sendMessage("You supplied: " + opt);
        }
    }

    // Like optional above, but name will always have a value, Unknown User if /acfd is executed
    @Subcommand("default")
    @CommandAlias("acfdefault|acfd")
    public void onTestDefault(CommandSender sender, @Default("Unknown User") String name) {
        sender.sendMessage("Hello, " + name);
    }

    // Pressing tab after typing /acfc A<tab> might pop up command completions for Aikar if Aikar was online,
    // /acf Aikar wo<tab> with a world named "world" would provide world as a completion.
    // @test was custom defined in ACFExample as foobar, so only that value would show up as a completion
    // then finally /acfc Aikar world foobar <tab> would show foo1, foo2, foo3 statically
    @Subcommand("completions")
    @CommandAlias("acfcompletions|acfc")
    @CommandCompletion("@players @worlds @test foo1|foo2|foo3")
    public void onTestCompletion(CommandSender sender, OnlinePlayer player, World world, String test, String misc) {
        sender.sendMessage("You got " + player.getPlayer().getName() + " - " + world.getName() + " - " + test + " - " + misc);
    }


    // This sub command requires that `/acf testsub test1` be typed to be executed, or /Foo
    @Subcommand("testsub test1")
    @CommandCompletion("Foo")
    public void onTestSub1(CommandSender sender, String hi) {
        sender.sendMessage(hi);
    }

    // Nested inner classes are automatically loaded when the parent is registered.
    // When @Subcommand is defined in an inner class, it is assumed that the values defined here are automatically prepended
    // to all of the children methods @Subcommand
    // A CommandPermission defined at this spot would also require that permission on every sub command without
    // Every sub command having to define that annotation. This is good for grouping like-permissioned commands.
    // Since %test was defined as a command replacement to "foobar", every inner method will require that permission.
    @Subcommand("test|txt|tfoo")
    @CommandPermission("%test")
    public class Test extends BaseCommand {

        // Will require /acf test test1 to access (or any of the alternate formats such as /acf txt td1)
        @Subcommand("test1|td1")
        @CommandCompletion("%foo")
        public void onTest1(Player player, String testX) {
            player.sendMessage("You got test inner test1: " + testX);
        }

        // same again, but /acf test foobar (command replacement) will work here
        @Subcommand("test2|td2|%test")
        @CommandCompletion("%test")
        public void onTest2(Player player, @Values("%test") String testY) {
            player.sendMessage("You got test inner test2: " + testY);
        }

        // Nesting inner classes works infinitely recursive
        // All of these inner methods require /acf test next <inner subcommand> to acces
        @Subcommand("next")
        public class TestInner extends BaseCommand {

            @Subcommand("test3|td4")
            @CommandCompletion("FOO")
            public void onTest1(Player player, String testX) {
                player.sendMessage("You got test inner inner test3: " + testX);
            }
            // Requires /acf test next test4 or simply /deepinner command alias
            @CommandAlias("deepinner")
            @Subcommand("test4|td4")
            @CommandCompletion("BAR")
            public void onTest2(Player player, String testY) {
                player.sendMessage("You got test inner inner test4: " + testY);
            }
        }
    }
}
