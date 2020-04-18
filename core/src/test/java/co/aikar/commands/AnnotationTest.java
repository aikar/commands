/*
 * Copyright (c) 2016-2019 Daniel Ennis (Aikar) - MIT License
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
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnnotationTest {

    private final CommandManager<?, ?, ?, ?, ?, ?> manager = Mockito.mock(CommandManager.class);
    private Annotations<?> annotations = new Annotations<>(this.manager);

    @Test
    public void testAnnotationsSimple() {
        final String aliasAnnotation = this.annotations.getAnnotationValue(TestClass.class, CommandAlias.class, Annotations.NOTHING);
        assertEquals("msg", aliasAnnotation);
        final String permissionAnnotation = this.annotations.getAnnotationValue(TestClass.class, CommandPermission.class, Annotations.NOTHING);
        assertEquals("test.test", permissionAnnotation);
        final String descriptionAnnotation = this.annotations.getAnnotationValue(TestClass.class, Description.class, Annotations.NOTHING);
        assertEquals("Just a test command", descriptionAnnotation);


        final String aliasAnnotationRoot = this.annotations.getAnnotationValue(TestWithRootAnnotation.class, CommandAlias.class, Annotations.NOTHING);
        assertEquals("test", aliasAnnotationRoot);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @CommandAlias("msg")
    @CommandPermission("test.test")
    @Description("Just a test command")
    private @interface TestAnnotation {
    }

    @TestAnnotation
    private static final class TestClass {
    }

    @CommandAlias("test")
    private static final class TestWithRootAnnotation {
    }
}
