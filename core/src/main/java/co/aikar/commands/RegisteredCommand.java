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

import co.aikar.commands.annotation.*;
import co.aikar.commands.contexts.ContextResolver;
import co.aikar.commands.contexts.IssuerAwareContextResolver;
import co.aikar.commands.contexts.IssuerOnlyContextResolver;
import co.aikar.commands.contexts.OptionalContextResolver;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class RegisteredCommand <R extends CommandExecutionContext<? extends CommandExecutionContext, ? extends CommandIssuer>> {
    final BaseCommand scope;
    final String command;
    final Method method;
    final String prefSubCommand;
    final Parameter[] parameters;
    final ContextResolver<?, R>[] resolvers;
    final String syntaxText;
    final String helpText;

    private final String permission;
    final String complete;
    final int requiredResolvers;
    final int optionalResolvers;
    final List<String> registeredSubcommands = new ArrayList<>();

    RegisteredCommand(BaseCommand scope, String command, Method method, String prefSubCommand) {
        this.scope = scope;
        if ("__unknown".equals(prefSubCommand) || "__default".equals(prefSubCommand)) {
            prefSubCommand = "";
        }
        this.command = command + (method.getAnnotation(CommandAlias.class) == null && !prefSubCommand.isEmpty() ? prefSubCommand : "");
        this.method = method;
        this.prefSubCommand = prefSubCommand;
        CommandPermission permissionAnno = method.getAnnotation(CommandPermission.class);
        this.permission = permissionAnno != null ? scope.manager.getCommandReplacements().replace(permissionAnno.value()) : null;
        CommandCompletion completionAnno = method.getAnnotation(CommandCompletion.class);
        this.complete = completionAnno != null ? scope.manager.getCommandReplacements().replace(completionAnno.value()) : null;
        this.parameters = method.getParameters();

        Description descriptionAnno = method.getAnnotation(Description.class);
        this.helpText = descriptionAnno != null ? descriptionAnno.value() : "";
        //noinspection unchecked
        this.resolvers = new ContextResolver[this.parameters.length];
        final Syntax syntaxStr = method.getAnnotation(Syntax.class);
        final CommandManager manager = scope.manager;
        final CommandContexts commandContexts = manager.getCommandContexts();

        int requiredResolvers = 0;
        int optionalResolvers = 0;
        StringBuilder syntaxB = new StringBuilder(64);

        for (int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];
            final Class<?> type = parameter.getType();

            //noinspection unchecked
            final ContextResolver<?, R> resolver = commandContexts.getResolver(type);
            if (resolver != null) {
                resolvers[i] = resolver;

                if (!scope.manager.isCommandIssuer(type)) {
                    String name = parameter.getName();
                    if (isOptionalResolver(resolver, parameter)) {
                        optionalResolvers++;
                        if (!(resolver instanceof IssuerOnlyContextResolver)) {
                            syntaxB.append('[').append(name).append("] ");
                        }
                    } else {
                        requiredResolvers++;
                        syntaxB.append('<').append(name).append("> ");
                    }
                }
            } else {
                ACFUtil.sneaky(new InvalidCommandContextException(
                    "Parameter " + type.getSimpleName() + " of " + this.command + " has no applicable context resolver"
                ));
            }
        }
        String syntaxText = syntaxB.toString();
        this.syntaxText = manager.getCommandReplacements().replace(syntaxStr != null ?
                ACFUtil.replace(syntaxStr.value(), "@syntax", syntaxText) : syntaxText);
        this.requiredResolvers = requiredResolvers;
        this.optionalResolvers = optionalResolvers;
    }

    private boolean isOptionalResolver(ContextResolver<?, R> resolver, Parameter parameter) {
        return isOptionalResolver(resolver)
                || parameter.getAnnotation(Optional.class) != null
                || parameter.getAnnotation(Default.class) != null;
    }

    private boolean isOptionalResolver(ContextResolver<?, R> resolver) {
        return resolver instanceof IssuerAwareContextResolver || resolver instanceof IssuerOnlyContextResolver
                || resolver instanceof OptionalContextResolver;
    }

    void invoke(CommandIssuer sender, List<String> args) {
        if (!scope.canExecute(sender, this)) {
            return;
        }
        preCommand();
        try {
            Map<String, Object> passedArgs = resolveContexts(sender, args);
            if (passedArgs == null) return;

            method.invoke(scope, passedArgs.values().toArray());
        } catch (Exception e) {
            handleException(sender, args, e);
        }
        postCommand();
    }
    public void preCommand() {}
    public void postCommand() {}

    void handleException(CommandIssuer sender, List<String> args, Exception e) {
        if (e instanceof InvocationTargetException && e.getCause() instanceof InvalidCommandArgument) {
            e = (Exception) e.getCause();
        }
        if (e instanceof InvalidCommandArgument) {
            InvalidCommandArgument ica = (InvalidCommandArgument) e;
            if (ica.key != null) {
                sender.sendMessage(MessageType.ERROR, ica.key, ica.replacements);
            } else if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                sender.sendMessage(MessageType.ERROR, MessageKeys.ERROR_PREFIX, "{message}", e.getMessage());
            }
            if (ica.showSyntax) {
                scope.showSyntax(sender, this);
            }
        } else {
            boolean handeled = this.scope.manager.handleUncaughtException(scope, this, sender, args, e);
            if(!handeled){
                sender.sendMessage(MessageType.ERROR, MessageKeys.ERROR_PERFORMING_COMMAND);
            }
            this.scope.manager.log(LogLevel.ERROR, "Exception in command: " + command + " " + ACFUtil.join(args), e);
        }
    }

    @Nullable
    Map<String, Object> resolveContexts(CommandIssuer sender, List<String> args) throws InvalidCommandArgument {
        return resolveContexts(sender, args, parameters.length);
    }
    @Nullable
    Map<String, Object> resolveContexts(CommandIssuer sender, List<String> args, int argLimit) throws InvalidCommandArgument {
        args = Lists.newArrayList(args);
        String[] origArgs = args.toArray(new String[args.size()]);
        Map<String, Object> passedArgs = Maps.newLinkedHashMap();
        int remainingRequired = requiredResolvers;
        for (int i = 0; i < parameters.length && i < argLimit; i++) {
            boolean isLast = i == parameters.length - 1;
            boolean allowOptional = remainingRequired == 0;
            final Parameter parameter = parameters[i];
            final String parameterName = parameter.getName();
            final Class<?> type = parameter.getType();
            //noinspection unchecked
            final ContextResolver<?, R> resolver = resolvers[i];
            R context = this.scope.manager.createCommandContext(this, parameter, sender, args, i, passedArgs);
            boolean isOptionalResolver = isOptionalResolver(resolver, parameter);
            if (!isOptionalResolver) {
                remainingRequired--;
            }
            if (args.isEmpty() && !(isLast && type == String[].class)) {
                Default def = parameter.getAnnotation(Default.class);
                Optional opt = parameter.getAnnotation(Optional.class);
                if (allowOptional && def != null) {
                    args.add(scope.manager.getCommandReplacements().replace(def.value()));
                } else if (allowOptional && opt != null) {
                    passedArgs.put(parameterName, isOptionalResolver(resolver) ? resolver.getContext(context) : null);
                    //noinspection UnnecessaryContinue
                    continue;
                } else if (!isOptionalResolver) {
                    scope.showSyntax(sender, this);
                    return null;
                }
            }
            final Values values = parameter.getAnnotation(Values.class);
            if (values != null) {
                String arg = !args.isEmpty() ? args.get(0) : "";

                final String[] split = ACFPatterns.PIPE.split(scope.manager.getCommandReplacements().replace(values.value()));
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
                    throw new InvalidCommandArgument(MessageKeys.PLEASE_SPECIFY_ONE_OF,
                            "{valid}", ACFUtil.join(possible, ", "));
                }
            }
            passedArgs.put(parameterName, resolver.getContext(context));
        }
        return passedArgs;
    }

    boolean hasPermission(CommandIssuer issuer) {
        return (permission == null || permission.isEmpty() || scope.manager.hasPermission(issuer, permission)) && scope.hasPermission(issuer);
    }


    /**
     * @see #getRequiredPermissions()
     * @deprecated
     */
    @Deprecated
    public String getPermission() {
        if (this.permission == null || this.permission.isEmpty()) {
            return null;
        }
        return ACFPatterns.COMMA.split(this.permission)[0];
    }

    public Set<String> getRequiredPermissions() {
        if (this.permission == null || this.permission.isEmpty()) {
            return ImmutableSet.of();
        }
        return Sets.newHashSet(ACFPatterns.COMMA.split(this.permission));
    }

    public boolean requiresPermission(String permission) {
        return getRequiredPermissions().contains(permission) || scope.requiresPermission(permission);
    }

    public String getPrefSubCommand() {
        return prefSubCommand;
    }

    public String getSyntaxText() {
        return syntaxText;
    }

    public String getCommand() {
        return command;
    }

    public void addSubcommand(String cmd) {
        this.registeredSubcommands.add(cmd);
    }
    public void addSubcommands(Collection<String> cmd) {
        this.registeredSubcommands.addAll(cmd);
    }
}
