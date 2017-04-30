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

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.annotation.Values;
import co.aikar.commands.contexts.ContextResolver;
import co.aikar.commands.contexts.SenderAwareContextResolver;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RegisteredCommand {
    private final BaseCommand scope;
    public final String command;
    private final Method method;
    final String prefSubCommand;
    final Parameter[] parameters;
    final ContextResolver<?>[] resolvers;
    final String syntaxText;

    private final CommandPermission permission;
    final CommandCompletion complete;
    final int nonSenderAwareResolvers;
    final int optionalResolvers;
    private CommandTiming timing;

    RegisteredCommand(BaseCommand scope, String command, Method method, String prefSubCommand) {
        this.scope = scope;
        if ("__unknown".equals(prefSubCommand) || "__default".equals(prefSubCommand)) {
            prefSubCommand = "";
        }
        this.command = command + (method.getAnnotation(CommandAlias.class) == null && !prefSubCommand.isEmpty() ? prefSubCommand : "");
        this.method = method;
        this.prefSubCommand = prefSubCommand;
        this.permission = method.getAnnotation(CommandPermission.class);
        this.complete = method.getAnnotation(CommandCompletion.class);
        this.parameters = method.getParameters();
        this.resolvers = new ContextResolver[this.parameters.length];
        final Syntax syntaxStr = method.getAnnotation(Syntax.class);
        final CommandManager manager = scope.manager;
        final CommandContexts commandContexts = manager.getCommandContexts();

        int nonSenderAwareResolvers = 0;
        int optionalResolvers = 0;
        StringBuilder syntaxB = new StringBuilder(64);

        for (int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];
            final Class<?> type = parameter.getType();

            final ContextResolver<?> resolver = commandContexts.getResolver(type);
            if (resolver != null) {
                resolvers[i] = resolver;

                if (!CommandSender.class.isAssignableFrom(parameter.getType())) {
                    if (resolver instanceof SenderAwareContextResolver || parameter.getAnnotation(Optional.class) != null
                            || parameter.getAnnotation(Default.class) != null) {
                        optionalResolvers++;
                    } else {
                        nonSenderAwareResolvers++;
                    }
                    if (parameter.getAnnotation(Default.class) != null ||
                        parameter.getAnnotation(Optional.class) != null ||
                        resolver instanceof SenderAwareContextResolver) {
                        syntaxB.append('[').append(parameter.getName()).append("] ");
                    } else {
                        syntaxB.append('<').append(parameter.getName()).append("> ");
                    }
                }
            } else {
                ACFUtil.sneaky(new InvalidParameterException(
                    "Parameter " + type.getSimpleName() + " of " + this.command + " has no resolver"
                ));
            }
        }
        if (syntaxStr != null) {
            this.syntaxText = syntaxStr.value();
        } else {
            this.syntaxText = syntaxB.toString();
        }
        this.nonSenderAwareResolvers = nonSenderAwareResolvers;
        this.optionalResolvers = optionalResolvers;
    }

    void invoke(CommandSender sender, List<String> args) {
        if (!scope.canExecute(sender, this)) {
            return;
        }
        try {
            Map<String, Object> passedArgs = resolveContexts(sender, args);
            if (passedArgs == null) return;

            method.invoke(scope, passedArgs.values().toArray());
        } catch (Exception e) {
            handleException(sender, args, e);
        }
    }

    void handleException(CommandSender sender, List<String> args, Exception e) {
        if (e instanceof InvocationTargetException && e.getCause() instanceof InvalidCommandArgument) {
            e = (Exception) e.getCause();
        }
        if (e instanceof InvalidCommandArgument) {
            if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                ACFUtil.sendMsg(sender, "&cError: " + e.getMessage());
            }
            if (((InvalidCommandArgument) e).showSyntax) {
                scope.showSyntax(sender, this);
            }
        } else {
            ACFUtil.sendMsg(sender, "&cI'm sorry, but there was an error performing this command.");
            ACFLog.exception("Exception in command: " + command + " " + ACFUtil.join(args), e);
        }
    }

    @Nullable
    Map<String, Object> resolveContexts(CommandSender sender, List<String> args) throws InvalidCommandArgument {
        return resolveContexts(sender, args, parameters.length);
    }
    @Nullable
    Map<String, Object> resolveContexts(CommandSender sender, List<String> args, int argLimit) throws InvalidCommandArgument {
        args = Lists.newArrayList(args);
        String[] origArgs = args.toArray(new String[args.size()]);
        Map<String, Object> passedArgs = Maps.newLinkedHashMap();
        for (int i = 0; i < parameters.length && i < argLimit; i++) {
            boolean isLast = i == parameters.length - 1;
            final Parameter parameter = parameters[i];
            final String parameterName = parameter.getName();
            final Class<?> type = parameter.getType();
            final ContextResolver<?> resolver = resolvers[i];
            CommandExecutionContext context = new CommandExecutionContext(this, parameter, sender, args, i, passedArgs);
            if (args.isEmpty() && !(isLast && type == String[].class)) {
                Default def = parameter.getAnnotation(Default.class);
                Optional opt = parameter.getAnnotation(Optional.class);
                if (isLast && def != null) {
                    args.add(def.value());
                } else if (isLast && opt != null) {
                    passedArgs.put(parameterName, resolver instanceof SenderAwareContextResolver ? resolver.getContext(context) : null);
                    //noinspection UnnecessaryContinue
                    continue;
                } else if (!(resolver instanceof SenderAwareContextResolver)) {
                    scope.showSyntax(sender, this);
                    return null;
                }
            }
            final Values values = parameter.getAnnotation(Values.class);
            if (values != null) {
                String arg = args.get(0);

                final String[] split = ACFPatterns.PIPE.split(values.value());
                Set<String> possible = Sets.newHashSet();
                for (String s : split) {
                    List<String> check = this.scope.manager.getCommandCompletions().getCompletionValues(this, sender, s, origArgs);
                    if (!check.isEmpty()) {
                        possible.addAll(check.stream().map(String::toLowerCase).collect(Collectors.toList()));
                    } else {
                        possible.add(s.toLowerCase());
                    }
                }

                if (!possible.contains(arg.toLowerCase())) {
                    throw new InvalidCommandArgument("Must be one of: " + ACFUtil.join(possible, ", "));
                }
            }
            passedArgs.put(parameterName, resolver.getContext(context));
        }
        return passedArgs;
    }

    CommandTiming getTiming() {
        if (this.timing == null) {
            this.timing = ACFUtil.getTiming(scope, command);
        }
        return this.timing;
    }

    boolean hasPermission(CommandSender check) {
        return permission == null || !(check instanceof Player) || check.hasPermission(permission.value());
    }

    public String getPrefSubCommand() {
        return prefSubCommand;
    }

    public String getSyntaxText() {
        return syntaxText;
    }

    public CommandPermission getPermission() {
        return permission;
    }
}
