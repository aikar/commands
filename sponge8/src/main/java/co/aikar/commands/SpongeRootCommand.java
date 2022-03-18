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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import net.kyori.adventure.text.Component;

import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.ArgumentReader.Mutable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SpongeRootCommand implements Command.Raw, RootCommand {

    private final SpongeCommandManager manager;
    private final String name;
    private BaseCommand defCommand;
    private SetMultimap<String, RegisteredCommand> subCommands = HashMultimap.create();
    private List<BaseCommand> children = new ArrayList<>();
    boolean isRegistered = false;

    SpongeRootCommand(SpongeCommandManager manager, String name) {
        this.manager = manager;
        this.name = name;
    }

    @Override
    public CommandResult process(CommandCause cause, Mutable arguments) throws CommandException {
        final Mutable params = arguments;
        String[] args = params.totalLength() == 0 ? new String[0] : arguments.input().split(" ");
        return this.executeSponge(manager.getCommandIssuer(cause), this.name, args);
    }

    private CommandResult executeSponge(CommandIssuer sender, String commandLabel, String[] args) {
        BaseCommand cmd = execute(sender, commandLabel, args);
        SpongeCommandOperationContext lastContext = (SpongeCommandOperationContext) cmd.getLastCommandOperationContext();
        return lastContext != null ? lastContext.getResult() : CommandResult.success();
    }

    @Override
    public List<CommandCompletion> complete(CommandCause cause, Mutable arguments) throws CommandException {
        String[] args = arguments.input().isEmpty() ? new String[]{""} : arguments.input().split(" ");
        final List<String> completionStrings = getTabCompletions(manager.getCommandIssuer(cause), this.name, args);
        final List<CommandCompletion> completions = new ArrayList<>();
        for (String completion : completionStrings) {
            completions.add(CommandCompletion.of(completion));
        }
        return completions;
    }

    @Override
    public boolean canExecute(CommandCause cause) {
        return this.hasAnyPermission(manager.getCommandIssuer(cause));
    }

    @Override
    public Optional<Component> shortDescription(CommandCause cause) {
        String description = getDescription();
        return description != null ? Optional.of(Component.text(description)) : Optional.empty();
    }

    @Override
    public Optional<Component> extendedDescription(CommandCause cause) {
        return this.shortDescription(cause);
    }

    @Override
    public Component usage(CommandCause cause) {
        String usage = getUsage();
        return usage != null ? Component.text(usage) : Component.empty();
    }

    public void addChild(BaseCommand command) {
        if (this.defCommand == null || !command.subCommands.get(BaseCommand.DEFAULT).isEmpty()) {
            this.defCommand = command;
        }
        addChildShared(this.children, this.subCommands, command);
    }

    @Override
    public String getCommandName() {
        return name;
    }

    @Override
    public BaseCommand getDefCommand() {
        return defCommand;
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
}
