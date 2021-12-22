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

import co.aikar.util.Table;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("BooleanMethodIsAlwaysInverted") // No IDEA, you are wrong
public class CommandConditions<
        I extends CommandIssuer,
        CEC extends CommandExecutionContext<CEC, I>,
        CC extends ConditionContext<I>
        > {
    private CommandManager manager;
    private Map<String, Condition<I>> conditions = new HashMap<>();
    private Table<Class<?>, String, ParameterCondition<?, ?, ?>> paramConditions = new Table<>();

    CommandConditions(CommandManager manager) {
        this.manager = manager;
    }

    public Condition<I> addCondition(@NotNull String id, @NotNull Condition<I> handler) {
        return this.conditions.put(id.toLowerCase(Locale.ENGLISH), handler);
    }

    public <P> ParameterCondition addCondition(Class<P> clazz, @NotNull String id,
                                               @NotNull ParameterCondition<P, CEC, I> handler) {
        return this.paramConditions.put(clazz, id.toLowerCase(Locale.ENGLISH), handler);
    }

    void validateConditions(CommandOperationContext context) throws InvalidCommandArgument {
        RegisteredCommand cmd = context.getRegisteredCommand();

        validateConditions(cmd.conditions, context);
        validateConditions(cmd.scope, context);
    }

    private void validateConditions(BaseCommand scope, CommandOperationContext operationContext) throws InvalidCommandArgument {
        validateConditions(scope.conditions, operationContext);

        if (scope.parentCommand != null) {
            validateConditions(scope.parentCommand, operationContext);
        }
    }

    private void validateConditions(String conditions, CommandOperationContext context) throws InvalidCommandArgument {
        if (conditions == null) {
            return;
        }

        conditions = this.manager.getCommandReplacements().replace(conditions);
        CommandIssuer issuer = context.getCommandIssuer();
        for (String cond : ACFPatterns.PIPE.split(conditions)) {
            String[] split = ACFPatterns.COLON.split(cond, 2);
            String id = split[0].toLowerCase(Locale.ENGLISH);
            Condition<I> condition = this.conditions.get(id);
            if (condition == null) {
                RegisteredCommand cmd = context.getRegisteredCommand();
                this.manager.log(LogLevel.ERROR, "Could not find command condition " + id + " for " + cmd.method.getName());
                continue;
            }

            String config = split.length == 2 ? split[1] : null;
            //noinspection unchecked
            CC conditionContext = (CC) this.manager.createConditionContext(issuer, config);
            condition.validateCondition(conditionContext);
        }
    }

    void validateConditions(CEC execContext, Object value) throws InvalidCommandArgument {
        String conditions = execContext.getCommandParameter().getConditions();
        if (conditions == null) {
            return;
        }
        conditions = this.manager.getCommandReplacements().replace(conditions);
        I issuer = execContext.getIssuer();
        for (String cond : ACFPatterns.PIPE.split(conditions)) {
            String[] split = ACFPatterns.COLON.split(cond, 2);
            ParameterCondition condition;
            Class<?> cls = execContext.getParam().getType();
            String id = split[0].toLowerCase(Locale.ENGLISH);
            do {
                condition = this.paramConditions.get(cls, id);
                if (condition == null && cls.getSuperclass() != null && cls.getSuperclass() != Object.class) {
                    cls = cls.getSuperclass();
                } else {
                    break;
                }
            } while (cls != null);


            if (condition == null) {
                RegisteredCommand cmd = execContext.getCmd();
                this.manager.log(LogLevel.ERROR, "Could not find command condition " + id + " for " + cmd.method.getName() + "::" + execContext.getParam().getName());
                continue;
            }
            String config = split.length == 2 ? split[1] : null;
            //noinspection unchecked
            CC conditionContext = (CC) this.manager.createConditionContext(issuer, config);

            //noinspection unchecked
            condition.validateCondition(conditionContext, execContext, value);
        }
    }

    public interface Condition<I extends CommandIssuer> {
        void validateCondition(ConditionContext<I> context) throws InvalidCommandArgument;
    }

    public interface ParameterCondition<P, CEC extends CommandExecutionContext, I extends CommandIssuer> {
        void validateCondition(ConditionContext<I> context, CEC execContext, P value) throws InvalidCommandArgument;
    }
}
