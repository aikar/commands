/*
 * Copyright (c) 2016-2018 Daniel Ennis (Aikar) - MIT License
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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.regex.Pattern;

abstract class AnnotationLookups {
    /**
     * Checks whether or not the AnnotatedElement has an annotation of type annoClass.
     *
     * @param object    The element to check
     * @param annoClass The class to check whether is attached to the element
     * @return Whether an annotation of annoClass is attached to object
     */
    boolean hasAnnotation(AnnotatedElement object, Class<? extends Annotation> annoClass) {
        return getAnnotationValue(object, annoClass, Annotations.NOTHING) != null;
    }

    /**
     * Checks whether the object has an annotation or not. If the value is empty and allowEmpty is true, it will return false.
     *
     * @param object     The element to check
     * @param annoClass  The class to check whether is attached or not
     * @param allowEmpty Whether or not to allow for empty values
     * @return Whether or not the annotation is present or empty with allowEmpty as true
     */
    boolean hasAnnotation(AnnotatedElement object, Class<? extends Annotation> annoClass, boolean allowEmpty) {
        return getAnnotationValue(object, annoClass, Annotations.NOTHING | (allowEmpty ? 0 : Annotations.NO_EMPTY)) != null;
    }

    String[] getAnnotationValues(AnnotatedElement object, Class<? extends Annotation> annoClass) {
        return getAnnotationValues(object, annoClass, ACFPatterns.PIPE, Annotations.REPLACEMENTS);
    }

    String[] getAnnotationValues(AnnotatedElement object, Class<? extends Annotation> annoClass, Pattern pattern) {
        return getAnnotationValues(object, annoClass, pattern, Annotations.REPLACEMENTS);
    }

    String[] getAnnotationValues(AnnotatedElement object, Class<? extends Annotation> annoClass, int options) {
        return getAnnotationValues(object, annoClass, ACFPatterns.PIPE, options);
    }

    String[] getAnnotationValues(AnnotatedElement object, Class<? extends Annotation> annoClass, Pattern pattern, int options) {
        String value = getAnnotationValue(object, annoClass, options);
        if (value == null) {
            return null;
        }
        return pattern.split(value);
    }

    abstract String getAnnotationValue(AnnotatedElement object, Annotation annotation, int options);

    String getAnnotationValue(AnnotatedElement object, Class<? extends Annotation> annoClass) {
        return getAnnotationValue(object, annoClass, Annotations.REPLACEMENTS);
    }

    abstract String getAnnotationValue(AnnotatedElement annotation, Class<? extends Annotation> annoClass, int options);
}
