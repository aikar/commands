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
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SpongeRootCommand implements RootCommand {

    private final SpongeCommandManager manager;
    private final String name;
    private BaseCommand defCommand;
    private SetMultimap<String, RegisteredCommand> subCommands = HashMultimap.create();
    private List<BaseCommand> children = new ArrayList<>();
    private Command.Parameterized rootCommand;
    boolean isRegistered = false;


    SpongeRootCommand(SpongeCommandManager manager, String name) {
        this.manager = manager;
        this.name = name;
    }

    private void pushRegister() {
        Sponge.server()
                .commandManager()
                .registrar(Command.Parameterized.class)
                .get().register(manager.plugin, rootCommand, name);

    }

    public void register() {
        Parameter.Value<String> argument = Parameter.string()
                .key("main_arg")
                .addParser((parameterKey, reader, context) -> {
                    String input = reader.input();

                    // Split the input into an argument list
                    String[] args = input.isEmpty() ? new String[0] : input.split(" ");

                    // Get the tab completions using your existing logic
                    List<String> completions = SpongeRootCommand.this.getTabCompletions(manager.getCommandIssuer(context), SpongeRootCommand.this.name, args);

                    String string = String.join(" ", completions);

                    return Optional.of(string);
                })
                .build();

        // Build the command
        rootCommand = Command.builder()
                .executionRequirements(this::testPermission)
                .executor(this::executeSponge)  // Command execution logic
                .shortDescription(Component.text(getDescription()))
                .extendedDescription(Component.text(getUsage()))
                .addParameter(argument)
                .build();

        this.pushRegister();

        isRegistered = true;
    }

    private CommandResult executeSponge(CommandContext context) {
        SpongeCommandSource scchl = new SpongeCommandSource(context);
        String[] args = context
                .executedCommand()
                .get().parameters()
                .stream().map(Object::toString)
                .toArray(String[]::new);
        return this.executeSponge(manager.getCommandIssuer(scchl), this.name, args);
    }

    public boolean testPermission(@NotNull CommandCause source) {
        return this.hasAnyPermission(manager.getCommandIssuer(source));
    }

    private CommandResult executeSponge(CommandIssuer sender, String commandLabel, String[] args) {
        BaseCommand cmd = execute(sender, commandLabel, args);
        SpongeCommandOperationContext lastContext = (SpongeCommandOperationContext) cmd.getLastCommandOperationContext();
        return lastContext != null ? lastContext.getResult() : CommandResult.success();
    }

    public void addChild(BaseCommand command) {
        if (this.defCommand == null || !command.subCommands.get(BaseCommand.DEFAULT).isEmpty()) {
            this.defCommand = command;
        }
        addChildShared(this.children, this.subCommands, command);
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

    @Override
    public String getCommandName() {
        return name;
    }
}
