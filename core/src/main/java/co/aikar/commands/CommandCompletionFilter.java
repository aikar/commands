/*
 * Copyright (c) 2016-2026 Daniel Ennis (Aikar) - MIT License
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

import java.util.Locale;

/**
 * A filter for completions, allowing you to control which completions are shown to user based on the input and context.
 *
 * @param <C>
 */
public interface CommandCompletionFilter<C extends CommandCompletionContext> {

    /**
     * Accepts all completions without filtering.
     */
    CommandCompletionFilter NONE = (context, completion) -> true;

    /**
     * Filters completions that start with the input (case-insensitive).
     */
    CommandCompletionFilter STARTS_WITH = (context, completion) ->
            ApacheCommonsLangUtil.startsWithIgnoreCase(completion, context.getInput());

    /**
     * Filters completions that contain the input (case-insensitive).
     */
    CommandCompletionFilter CONTAINS = (context, completion) ->
            completion.toLowerCase(Locale.ENGLISH).contains(context.getInput().toLowerCase(Locale.ENGLISH));

    /**
     * Gets a filter that accepts all completions. Use this instead of {@link CommandCompletionFilter#NONE} to prevent
     * rawtype unchecked warnings.
     *
     * @param <C> completion context type
     * @return the no-op filter
     */
    static <C extends CommandCompletionContext> CommandCompletionFilter<C> none() {
        return NONE;
    }

    /**
     * Gets a filter that matches completions starting with the input (case-insensitive). Use this instead of
     * {@link CommandCompletionFilter#STARTS_WITH} to prevent rawtype unchecked warnings.
     *
     * @param <C> completion context type
     * @return the starts-with filter
     */
    static <C extends CommandCompletionContext> CommandCompletionFilter<C> startsWith() {
        return STARTS_WITH;
    }

    /**
     * Gets a filter that matches completions containing the input (case-insensitive). Use this instead of
     * {@link CommandCompletionFilter#CONTAINS} to prevent rawtype unchecked warnings.
     *
     * @param <C> completion context type
     * @return the contains filter
     */
    static <C extends CommandCompletionContext> CommandCompletionFilter<C> contains()  {
        return CONTAINS;
    }

    /**
     * Tests whether a completion should be included based on the context.
     *
     * @param context the completion context
     * @param completion the completion string to test
     * @return true if the completion passes the filter, false otherwise.
     */
    boolean test(C context, String completion);
}
