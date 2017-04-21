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

package co.aikar.acfexample;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.contexts.ContextResolver;

public abstract class SomeObject {
    private final Integer thisValue;

    SomeObject(Integer thisValue) {
        this.thisValue = thisValue;
    }

    public Integer getValue() {
        return this.thisValue;
    }

    public static ContextResolver<SomeObject> getContextResolver() {
        return (c) -> {
            String first = c.popFirstArg();
            if (first == null) {
                throw new InvalidCommandArgument("Must supply a number");
            }
            if ("1".equals(first)) {
                return new Test1();
            } else if ("2".equals(first)) {
                return new Test2();
            } else {
                try {
                    return new TestOther(Integer.parseInt(first));
                } catch (NumberFormatException ignored) {
                    throw new InvalidCommandArgument("Must be a valid number");
                }
            }
        };
    }

    public static class Test1 extends SomeObject {
        Test1() {
            super(1);
        }
    }
    public static class Test2 extends SomeObject {
        Test2() {
            super(2);
        }
    }
    public static class TestOther extends SomeObject {
        TestOther(Integer other) {
            super(other);
        }
    }
}
