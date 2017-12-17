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

import co.aikar.commands.annotation.Conditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

import java.util.Map;

@SuppressWarnings("BooleanMethodIsAlwaysInverted") // No IDEA, you are wrong
public class CommandConditions <
        I extends CommandIssuer,
        M extends CommandManager,
        CC extends ConditionContext,
        CEC extends CommandExecutionContext<CEC, I>,
        PCC extends ParameterConditionContext<?, CEC, I>
    > {
    M manager;
    Map<String, Condition<CC>> conditions = Maps.newHashMap();
    Table<Class<?>, String, ParameterCondition<PCC>> paramConditions = HashBasedTable.create();
    CommandConditions(M manager) {
        this.manager = manager;
    }

    Condition<CC> addCondition(String id, Condition<CC> handler) {
        return this.conditions.put(id.toLowerCase(), handler);
    }

    <P> ParameterCondition addCondition(Class<P> clazz, String id, ParameterCondition<PCC> handler) {
        return this.paramConditions.put(clazz, id.toLowerCase(), handler);
    }

    boolean validateConditions(CommandOperationContext context, CEC cec) {
        Conditions conditions = cec.getParam().getAnnotation(Conditions.class);
        return conditions == null || validateConditions(conditions, context, cec);
    }

    boolean validateConditions(CommandOperationContext context) {
        RegisteredCommand cmd = context.getRegisteredCommand();
        Conditions conditions = cmd.method.getAnnotation(Conditions.class);
        if (conditions != null) {
            if (!validateConditions(conditions, context)) {
                return false;
            }
        }
        return validateConditions(cmd.scope, context);
    }


    private boolean validateConditions(BaseCommand scope, CommandOperationContext operationContext) {
        Conditions conditions = scope.getClass().getAnnotation(Conditions.class);
        //noinspection SimplifiableIfStatement
        if (!validateConditions(conditions, operationContext)) {
            return false;
        }
        return scope.parentCommand == null || validateConditions(scope.parentCommand, operationContext);
    }

    private boolean validateConditions(Conditions condAnno, CommandOperationContext context) {
        if (condAnno == null) {
            return true;
        }
        //noinspection unchecked
        CC conditionContext = (CC) this.manager.createConditionContext(context, condAnno);
        String conditions = this.manager.getCommandReplacements().replace(condAnno.value());
        for (String cond : ACFPatterns.PIPE.split(conditions)) {
            String[] split = ACFPatterns.EQUALS.split(cond, 2);
            Condition<CC> condition = this.conditions.get(split[0].toLowerCase());

            if (!condition.validateCondition(conditionContext)) {
                return false;
            }
        }

        return true;
    }

    private boolean validateConditions(Conditions condAnno, CommandOperationContext context, CEC execContext) {
        if (condAnno == null) {
            return true;
        }
        //noinspection unchecked
        ParameterConditionContext conditionContext = this.manager.createConditionContext(context, execContext, condAnno);
        String conditions = this.manager.getCommandReplacements().replace(condAnno.value());
        for (String cond : ACFPatterns.PIPE.split(conditions)) {
            String[] split = ACFPatterns.EQUALS.split(cond, 2);
            ParameterCondition condition;
            Class<?> cls = execContext.getParam().getClass();
            String id = split[0].toLowerCase();
            do {
                condition = this.paramConditions.get(cls, id);
                if (condition == null && cls.getSuperclass() != null && cls.getSuperclass() != Object.class) {
                    cls = cls.getSuperclass();
                } else {
                    break;
                }
            } while (cls != null);

            //noinspection unchecked
            if (condition != null && !condition.validateCondition(conditionContext)) {
                return false;
            }
        }

        return true;
    }

    interface Condition <CC extends ConditionContext> {
        boolean validateCondition(CC context);
    }
    interface ParameterCondition <PCC extends ParameterConditionContext> {
        boolean validateCondition(PCC context);
    }
}
