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

import co.aikar.commands.apachecommonslang.ApacheCommonsLangUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public class CommandCompletions<C extends CommandCompletionContext> {
    private static final String DEFAULT_ENUM_ID = "@__defaultenum__";
    private final CommandManager manager;
    // TODO: use a CompletionProvider that can return a delegated Id or provide values such as enum support
    private Map<String, CommandCompletionHandler> completionMap = new HashMap<>();
    private Map<Class, String> defaultCompletions = new HashMap<>();

    public CommandCompletions(CommandManager manager) {
        this.manager = manager;
        registerStaticCompletion("empty", Collections.emptyList());
        registerStaticCompletion("nothing", Collections.emptyList());
        registerStaticCompletion("timeunits", Arrays.asList("minutes", "hours", "days", "weeks", "months", "years"));
        registerAsyncCompletion("range", (c) -> {
            String config = c.getConfig();
            if (config == null) {
                return Collections.emptyList();
            }
            final String[] ranges = ACFPatterns.DASH.split(config);
            int start;
            int end;
            if (ranges.length != 2) {
                start = 0;
                end = ACFUtil.parseInt(ranges[0], 0);
            } else {
                start = ACFUtil.parseInt(ranges[0], 0);
                end = ACFUtil.parseInt(ranges[1], 0);
            }
            return IntStream.rangeClosed(start, end).mapToObj(Integer::toString).collect(Collectors.toList());
        });
    }

    /**
     * Registr a completion handler to provide command completions based on the user input.
     *
     * @param id
     * @param handler
     * @return
     */
    public CommandCompletionHandler registerCompletion(String id, CommandCompletionHandler<C> handler) {
        return this.completionMap.put(prepareCompletionId(id), handler);
    }

    /**
     * Registr a completion handler to provide command completions based on the user input.
     * This handler is declared to be safe to be executed asynchronously.
     * <p>
     * Not all platforms support this, so if the platform does not support asynchronous execution,
     * your handler will be executed on the main thread.
     * <p>
     * Use this anytime your handler does not need to access state that is not considered thread safe.
     * <p>
     * Use context.isAsync() to determine if you are async or not.
     *
     * @param id
     * @param handler
     * @return
     */
    public CommandCompletionHandler registerAsyncCompletion(String id, AsyncCommandCompletionHandler<C> handler) {
        return this.completionMap.put(prepareCompletionId(id), handler);
    }

    /**
     * Register a static list of command completions that will never change.
     * Like @CommandCompletion, values are | (PIPE) separated.
     * <p>
     * Example: foo|bar|baz
     *
     * @param id
     * @param list
     * @return
     */
    public CommandCompletionHandler registerStaticCompletion(String id, String list) {
        return registerStaticCompletion(id, ACFPatterns.PIPE.split(list));
    }

    /**
     * Register a static list of command completions that will never change
     *
     * @param id
     * @param completions
     * @return
     */
    public CommandCompletionHandler registerStaticCompletion(String id, String[] completions) {
        return registerStaticCompletion(id, Arrays.asList(completions));
    }

    /**
     * Register a static list of command completions that will never change. The list is obtained from the supplier
     * immediately as part of this method call.
     *
     * @param id
     * @param supplier
     * @return
     */
    public CommandCompletionHandler registerStaticCompletion(String id, Supplier<Collection<String>> supplier) {
        return registerStaticCompletion(id, supplier.get());
    }

    /**
     * Register a static list of command completions that will never change
     *
     * @param id
     * @param completions
     * @return
     */
    public CommandCompletionHandler registerStaticCompletion(String id, Collection<String> completions) {
        return registerAsyncCompletion(id, x -> completions);
    }

    /**
     * Registers a completion handler such as @players to default apply to all command parameters of the specified types
     * <p>
     * This enables automatic completion support for parameters without manually defining it for custom objects
     *
     * @param id
     * @param classes
     */
    public void setDefaultCompletion(String id, Class... classes) {
        // get completion with specified id
        id = prepareCompletionId(id);
        CommandCompletionHandler completion = completionMap.get(id);

        if (completion == null) {
            // Throw something because no completion with specified id
            throw new IllegalStateException("Completion not registered for " + id);
        }

        for (Class clazz : classes) {
            defaultCompletions.put(clazz, id);
        }
    }

    @NotNull
    private static String prepareCompletionId(String id) {
        return (id.startsWith("@") ? "" : "@") + id.toLowerCase();
    }

    @NotNull
    List<String> of(RegisteredCommand cmd, CommandIssuer sender, String[] args, boolean isAsync) {
        String[] completions = ACFPatterns.SPACE.split(cmd.complete);
        final int argIndex = args.length - 1;

        String input = args[argIndex];

        String completion = argIndex < completions.length ? completions[argIndex] : null;
        if (completion == null || "*".equals(completion)) {
            completion = findDefaultCompletion(cmd, args);
        }

        if (completion == null && completions.length > 0) {
            String last = completions[completions.length - 1];
            if (last.startsWith("repeat@")) {
                completion = last;
            } else if (argIndex >= completions.length && cmd.parameters[cmd.parameters.length - 1].consumesRest) {
                completion = last;
            }
        }

        if (completion == null) {
            return Collections.singletonList(input);
        }

        return getCompletionValues(cmd, sender, completion, args, isAsync);
    }

    String findDefaultCompletion(RegisteredCommand cmd, String[] args) {
        int i = 0;
        for (CommandParameter param : cmd.parameters) {
            if (param.canConsumeInput() && ++i == args.length) {
                Class type = param.getType();
                while (type != null) {
                    String completion = this.defaultCompletions.get(type);
                    if (completion != null) {
                        return completion;
                    }
                    type = type.getSuperclass();
                }
                if (param.getType().isEnum()) {
                    CommandOperationContext ctx = CommandManager.getCurrentCommandOperationContext();
                    //noinspection unchecked
                    ctx.enumCompletionValues = ACFUtil.enumNames((Class<? extends Enum<?>>) param.getType());
                    return DEFAULT_ENUM_ID;
                }
                break;
            }
        }
        return null;
    }

    List<String> getCompletionValues(RegisteredCommand command, CommandIssuer sender, String completion, String[] args, boolean isAsync) {
        if (DEFAULT_ENUM_ID.equals(completion)) {
            CommandOperationContext<?> ctx = CommandManager.getCurrentCommandOperationContext();
            return ctx.enumCompletionValues;
        }
        boolean repeat = completion.startsWith("repeat@");
        if (repeat) {
            completion = completion.substring(6);
        }
        completion = manager.getCommandReplacements().replace(completion);

        List<String> allCompletions = new ArrayList<>();
        String input = args.length > 0 ? args[args.length - 1] : "";

        for (String value : ACFPatterns.PIPE.split(completion)) {
            String[] complete = ACFPatterns.COLONEQUALS.split(value, 2);
            CommandCompletionHandler handler = this.completionMap.get(complete[0].toLowerCase());
            if (handler != null) {
                if (isAsync && !(handler instanceof AsyncCommandCompletionHandler)) {
                    ACFUtil.sneaky(new SyncCompletionRequired());
                    return null;
                }
                String config = complete.length == 1 ? null : complete[1];
                CommandCompletionContext context = manager.createCompletionContext(command, sender, input, config, args);

                try {
                    //noinspection unchecked
                    Collection<String> completions = handler.getCompletions(context);

                    //Handle completions with more than one word:
                    if (!repeat && completions != null
                            && command.parameters[command.parameters.length - 1].consumesRest
                            && args.length > ACFPatterns.SPACE.split(command.complete).length) {
                        String start = String.join(" ", args);
                        completions = completions.stream()
                                .map(s -> {
                                    if (s != null && s.split(" ").length >= args.length && ApacheCommonsLangUtil.startsWithIgnoreCase(s, start)) {
                                        String[] completionArgs = s.split(" ");
                                        return String.join(" ", Arrays.copyOfRange(completionArgs, args.length - 1, completionArgs.length));
                                    } else {
                                        return s;
                                    }
                                }).collect(Collectors.toList());
                    }

                    if (completions != null) {
                        allCompletions.addAll(completions);
                        continue;
                    }
                    //noinspection ConstantIfStatement,ConstantConditions
                    if (false) { // Hack to fool compiler. since its sneakily thrown.
                        throw new CommandCompletionTextLookupException();
                    }
                } catch (CommandCompletionTextLookupException ignored) {
                    // This should only happen if some other feedback error occured.
                } catch (Exception e) {
                    command.handleException(sender, Arrays.asList(args), e);
                }
                // Something went wrong in lookup, fall back to input
                return Collections.singletonList(input);
            } else {
                // Plaintext value
                allCompletions.add(value);
            }
        }
        return allCompletions;
    }

    public interface CommandCompletionHandler<C extends CommandCompletionContext> {
        Collection<String> getCompletions(C context) throws InvalidCommandArgument;
    }

    public interface AsyncCommandCompletionHandler<C extends CommandCompletionContext> extends CommandCompletionHandler<C> {
    }

    public static class SyncCompletionRequired extends RuntimeException {
    }

}
