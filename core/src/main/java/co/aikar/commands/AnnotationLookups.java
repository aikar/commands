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
     * @param object
     *         The element to check
     * @param annoClass
     *         The class to check whether is attached to the element
     *
     * @return Whether an annotation of annoClass is attached to object
     */
    boolean hasAnnotation(AnnotatedElement object, Class<? extends Annotation> annoClass) {
        return getAnnotationValue(object, annoClass, Annotations.NOTHING) != null;
    }

    /**
     * Checks whether the object has an annotation or not. If the value is empty and allowEmpty is true, it will return
     * false.
     *
     * @param object
     *         The element to check
     * @param annoClass
     *         The class to check whether is attached or not
     * @param allowEmpty
     *         Whether or not to allow for empty values
     *
     * @return Whether or not the annotation is present or empty with allowEmpty as true
     */
    boolean hasAnnotation(AnnotatedElement object, Class<? extends Annotation> annoClass, boolean allowEmpty) {
        return getAnnotationValue(object, annoClass, Annotations.NOTHING | (allowEmpty ? 0 : Annotations.NO_EMPTY)) != null;
    }

    /**
     * Gets the values of the annotated object's annotation.
     * It splits all values on a pipe.
     *
     * @param object
     *         The element to check
     * @param annoClass
     *         The attached annotation class to read
     *
     * @return All values split by a pipe
     */
    String[] getAnnotationValues(AnnotatedElement object, Class<? extends Annotation> annoClass) {
        return getAnnotationValues(object, annoClass, ACFPatterns.PIPE, Annotations.REPLACEMENTS);
    }

    /**
     * Gets all values of the annotated object's annotation.
     * All values get split on a specific pattern.
     *
     * @param object
     *         The object to check
     * @param annoClass
     *         The annotation to read
     * @param pattern
     *         The pattern all values are split on
     *
     * @return All values found
     */
    String[] getAnnotationValues(AnnotatedElement object, Class<? extends Annotation> annoClass, Pattern pattern) {
        return getAnnotationValues(object, annoClass, pattern, Annotations.REPLACEMENTS);
    }

    /**
     * Gets all values of the annotated object's annotation.
     * All values go through the specific set of options.
     *
     * @param object
     *         The object to check
     * @param annoClass
     *         The annotation to read
     * @param options
     *         Options passed to {@link #getAnnotationValues(AnnotatedElement, Class, Pattern, int)}
     *
     * @return All values found
     */
    String[] getAnnotationValues(AnnotatedElement object, Class<? extends Annotation> annoClass, int options) {
        return getAnnotationValues(object, annoClass, ACFPatterns.PIPE, options);
    }

    /**
     * Gets all values of the annotated object's annotation.
     *
     * @param object
     *         The object to check
     * @param annoClass
     *         The annotation to read
     * @param pattern
     *         The pattern all options
     * @param options
     *         The options to follow during finding values
     *
     * @return null if no values were found, if not, all values found
     */
    String[] getAnnotationValues(AnnotatedElement object, Class<? extends Annotation> annoClass, Pattern pattern, int options) {
        String value = getAnnotationValue(object, annoClass, options);
        if (value == null) {
            return null;
        }
        return pattern.split(value);
    }

    String getAnnotationValue(AnnotatedElement object, Class<? extends Annotation> annoClass) {
        return getAnnotationValue(object, annoClass, Annotations.REPLACEMENTS);
    }

    String getAnnotationValue(AnnotatedElement element, Class<? extends Annotation> annoClass, int options) {
        Annotation annotation = element.getAnnotation(annoClass);
        return getAnnotationValue(element, annotation, options);
    }

    /**
     * Gets the value field of the annotated element's annotation with the specific set of options.
     *
     * @param object
     *         The object to check
     * @param annotation
     *         The annotation instance to read
     * @param options
     *         The options to follow
     *
     * @return
     */
    abstract String getAnnotationValue(AnnotatedElement object, Annotation annotation, int options);
}
