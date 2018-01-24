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
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.annotation.Values;
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
public class RegisteredCommand <CEC extends CommandExecutionContext<CEC, ? extends CommandIssuer>> {
    final BaseCommand scope;
    final String command;
    final Method method;
    final String prefSubCommand;
    final Parameter[] parameters;
    final ContextResolver<?, CEC>[] resolvers;
    final String syntaxText;
    final String helpText;

    private final String permission;
    final String complete;
    final int requiredResolvers;
    final int optionalResolvers;
    final List<String> registeredSubcommands = new ArrayList<>();
    private final CommandManager manager;

    RegisteredCommand(BaseCommand scope, String command, Method method, String prefSubCommand) {
        this.scope = scope;
        this.manager = this.scope.manager;
        if (BaseCommand.CATCHUNKNOWN.equals(prefSubCommand) || BaseCommand.DEFAULT.equals(prefSubCommand)) {
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
        //noinspection unchecked
        final CommandContexts commandContexts = this.manager.getCommandContexts();

        int requiredResolvers = 0;
        int optionalResolvers = 0;
        StringBuilder syntaxB = new StringBuilder(64);

        for (int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];
            final Class<?> type = parameter.getType();

            //noinspection unchecked
            final ContextResolver<?, CEC> resolver = commandContexts.getResolver(type);
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
        this.syntaxText = this.manager.getCommandReplacements().replace(syntaxStr != null ?
                ACFUtil.replace(syntaxStr.value(), "@syntax", syntaxText) : syntaxText);
        this.requiredResolvers = requiredResolvers;
        this.optionalResolvers = optionalResolvers;
    }

    private boolean isOptionalResolver(ContextResolver<?, CEC> resolver, Parameter parameter) {
        return isOptionalResolver(resolver)
                || parameter.getAnnotation(Optional.class) != null
                || parameter.getAnnotation(Default.class) != null;
    }

    private boolean isOptionalResolver(ContextResolver<?, CEC> resolver) {
        return resolver instanceof IssuerAwareContextResolver || resolver instanceof IssuerOnlyContextResolver
                || resolver instanceof OptionalContextResolver;
    }

    void invoke(CommandIssuer sender, List<String> args, CommandOperationContext context) {
        if (!scope.canExecute(sender, this)) {
            return;
        }
        preCommand();
        try {
            this.manager.conditions.validateConditions(context);
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
        if (e instanceof ShowCommandHelp) {
            ShowCommandHelp showHelp = (ShowCommandHelp) e;
            CommandHelp commandHelp = manager.generateCommandHelp();
            if (showHelp.search) {
                commandHelp.setSearch(showHelp.searchArgs == null ? args : showHelp.searchArgs);
            }
            commandHelp.showHelp(sender);
        } else if (e instanceof InvalidCommandArgument) {
            InvalidCommandArgument invalidCommandArg = (InvalidCommandArgument) e;
            if (invalidCommandArg.key != null) {
                sender.sendMessage(MessageType.ERROR, invalidCommandArg.key, invalidCommandArg.replacements);
            } else if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                sender.sendMessage(MessageType.ERROR, MessageKeys.ERROR_PREFIX, "{message}", e.getMessage());
            }
            if (invalidCommandArg.showSyntax) {
                scope.showSyntax(sender, this);
            }
        } else {
            try {
                if (!this.manager.handleUncaughtException(scope, this, sender, args, e)) {
                    sender.sendMessage(MessageType.ERROR, MessageKeys.ERROR_PERFORMING_COMMAND);
                }
                this.manager.log(LogLevel.ERROR, "Exception in command: " + command + " " + ACFUtil.join(args), e);
            } catch (Exception e2) {
                this.manager.log(LogLevel.ERROR, "Exception in handleException for command: " + command + " " + ACFUtil.join(args), e);
                this.manager.log(LogLevel.ERROR, "Exception triggered by exception handler:", e2);
            }
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
        CommandOperationContext opContext = CommandManager.getCurrentCommandOperationContext();
        for (int i = 0; i < parameters.length && i < argLimit; i++) {
            boolean isLast = i == parameters.length - 1;
            boolean allowOptional = remainingRequired == 0;
            final Parameter parameter = parameters[i];
            final String parameterName = parameter.getName();
            final Class<?> type = parameter.getType();
            //noinspection unchecked
            final ContextResolver<?, CEC> resolver = resolvers[i];
            //noinspection unchecked
            CEC context = (CEC) this.manager.createCommandContext(this, parameter, sender, args, i, passedArgs);
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
                    Object value = isOptionalResolver(resolver) ? resolver.getContext(context) : null;
                    if (value == null && parameter.getClass().isPrimitive()) {
                        throw new IllegalStateException("Parameter " + parameter.getName() + " is primitive and does not support Optional.");
                    }
                    //noinspection unchecked
                    this.manager.conditions.validateConditions(context, value);
                    passedArgs.put(parameterName, value);
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
                    //noinspection unchecked
                    List<String> check = this.manager.getCommandCompletions().getCompletionValues(this, sender, s, origArgs, opContext.isAsync());
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
            Object paramValue = resolver.getContext(context);
            //noinspection unchecked
            this.manager.conditions.validateConditions(context, paramValue);
            passedArgs.put(parameterName, paramValue);
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
