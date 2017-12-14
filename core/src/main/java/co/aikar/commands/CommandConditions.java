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
import com.google.common.collect.Maps;

import java.util.Map;

@SuppressWarnings("BooleanMethodIsAlwaysInverted") // No IDEA, you are wrong
public class CommandConditions <M extends CommandManager, CC extends ConditionContext> {
    M manager;
    Map<String, Condition<CC>> conditions = Maps.newHashMap();
    CommandConditions(M manager) {
        this.manager = manager;
    }

    Condition<CC> addCondition(String id, Condition<CC> handler) {
        return this.conditions.put(id.toLowerCase(), handler);
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
        CC conditionContext = (CC) this.manager.createConditionContext(context);
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

    interface Condition <CC extends ConditionContext> {
        boolean validateCondition(CC context);
    }
}
