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

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("BooleanMethodIsAlwaysInverted") // No IDEA, you are wrong
public class CommandVisibilityConditions<
        I extends CommandIssuer,
        CC extends ConditionContext<I>
        > {
    private CommandManager manager;
    private Map<String, VisibilityCondition<I>> visibilityConditions = new HashMap<>();

    CommandVisibilityConditions(CommandManager manager) {
        this.manager = manager;
    }

    public VisibilityCondition<I> addCondition(@NotNull String id, @NotNull VisibilityCondition<I> handler) {
        return this.visibilityConditions.put(id.toLowerCase(Locale.ENGLISH), handler);
    }

    boolean shouldTabComplete(RegisteredCommand cmd, CommandIssuer issuer) {
        return validateShowConditions(cmd, issuer) && !validateHideConditions(cmd, issuer);
    }

    private boolean validateShowConditions(RegisteredCommand cmd, CommandIssuer issuer) {
        return validateConditions(cmd, cmd.showConditions, issuer, true)
                && validateShowConditions(cmd, cmd.scope, issuer);
    }

    private boolean validateShowConditions(RegisteredCommand cmd, BaseCommand scope, CommandIssuer issuer) {
        return validateConditions(cmd, scope.showConditions, issuer, true)
                && (scope.parentCommand == null || validateShowConditions(cmd, scope.parentCommand, issuer));
    }

    private boolean validateHideConditions(RegisteredCommand cmd, CommandIssuer issuer) {
        return validateConditions(cmd, cmd.hideConditions, issuer, false)
                || validateHideConditions(cmd, cmd.scope, issuer);
    }

    private boolean validateHideConditions(RegisteredCommand cmd, BaseCommand scope, CommandIssuer issuer) {
        return validateConditions(cmd, scope.hideConditions, issuer, false)
                || (scope.parentCommand != null && validateHideConditions(cmd, scope.parentCommand, issuer));
    }

    private boolean validateConditions(RegisteredCommand cmd, String conditions, CommandIssuer issuer, boolean defaultResult) {
        if (conditions == null) {
            return defaultResult;
        }

        conditions = this.manager.getCommandReplacements().replace(conditions);
        String[] splitConditions = ACFPatterns.PIPE.split(conditions);
        Boolean[] results = new Boolean[splitConditions.length];
        for (int i = 0; i < splitConditions.length; i++) {
            String[] split = ACFPatterns.COLON.split(splitConditions[i], 2);
            String id = split[0].toLowerCase(Locale.ENGLISH);
            VisibilityCondition<I> visibilityCondition = this.visibilityConditions.get(id);
            if (visibilityCondition == null) {
                this.manager.log(LogLevel.ERROR, "Could not find command visibility condition " + id + " for " + cmd.method.getName());
                continue;
            }

            String config = split.length == 2 ? split[1] : null;
            //noinspection unchecked
            CC conditionContext = (CC) this.manager.createConditionContext(issuer, config);
            results[i] = visibilityCondition.validateCondition(conditionContext);
        }
        return Arrays.stream(results).allMatch(Boolean.TRUE::equals);
    }

    public interface VisibilityCondition<I extends CommandIssuer> {
        boolean validateCondition(ConditionContext<I> context);
    }
}
