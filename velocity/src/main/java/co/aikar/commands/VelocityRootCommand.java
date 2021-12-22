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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.velocitypowered.api.command.SimpleCommand;

public class VelocityRootCommand implements SimpleCommand, RootCommand {

    private final VelocityCommandManager manager;
    private final String name;
    private BaseCommand defCommand;
    private SetMultimap<String, RegisteredCommand> subCommands = HashMultimap.create();
    private List<BaseCommand> children = new ArrayList<>();
    boolean isRegistered = false;

    VelocityRootCommand(VelocityCommandManager manager, String name) {
        this.manager = manager;
        this.name = name;
    }

    @Override
    public String getCommandName() {
        return name;
    }

    @Override
    public void addChild(BaseCommand command) {
        if (this.defCommand == null || !command.subCommands.get(BaseCommand.DEFAULT).isEmpty()) {
            this.defCommand = command;

        }
        this.addChildShared(this.children, this.subCommands, command);
    }

    @Override
    public CommandManager getManager() {
        return manager;
    }

    @Override
    public SetMultimap<String, RegisteredCommand> getSubCommands() {
        return subCommands;
    }

    @Override
    public List<BaseCommand> getChildren() {
        return children;
    }

    @Override
    public BaseCommand getDefCommand() {
        return defCommand;
    }

    @Override
    public void execute(Invocation invocation) {
        execute(manager.getCommandIssuer(invocation.source()), getCommandName(), invocation.arguments());
    }


    @Override
    public List<String> suggest(Invocation invocation) {
        return getTabCompletions(manager.getCommandIssuer(invocation.source()), getCommandName(), invocation.arguments());
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.completedFuture(getTabCompletions(manager.getCommandIssuer(invocation.source()), getCommandName(), invocation.arguments()));
    }
}
