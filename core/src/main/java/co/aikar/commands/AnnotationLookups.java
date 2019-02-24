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
     * This checks whether the {@link AnnotatedElement} given has an annotation of the type given as annoClass.
     *
     * Runs through annotation processing
     *
     * @param object
     *         The element to check whether has an annotation of annoClass.
     * @param annoClass
     *         The annotation type in form of a class.
     *
     * @return Whether an annotation of annoClass is attached to element.
     *
     * @see #hasAnnotation(AnnotatedElement, Class, boolean)
     */
    boolean hasAnnotation(AnnotatedElement object, Class<? extends Annotation> annoClass) {
        return getAnnotationValue(object, annoClass, Annotations.NOTHING) != null;
    }

    /**
     * This checks whether the {@link AnnotatedElement} given has an annotation of the type given as annoClass.
     * If the value is empty/null and allowEmpty is false, it will return false.
     *
     * @param object
     *         The element to check whether has an annotation of annoClass.
     * @param annoClass
     *         The annotation type in form of a class.
     * @param allowEmpty
     *         Whether or not to allow empty/null values.
     *
     * @return Whether an annotation of annoClass is attached to element, and if allowEmpty is false, whether it has a value.
     */
    boolean hasAnnotation(AnnotatedElement object, Class<? extends Annotation> annoClass, boolean allowEmpty) {
        return getAnnotationValue(object, annoClass, Annotations.NOTHING | (allowEmpty ? 0 : Annotations.NO_EMPTY)) != null;
    }

    /**
     * This fetches all the values the {@link AnnotatedElement}'s annotation of type annoClass has.
     * If the value contains a pipe (|), it will split on this and return an array of more indicies than 1.
     *
     * @param object
     *         The element to check the value of the annotation of type annoClass.
     * @param annoClass
     *         The annotation type in form of a class.
     *
     * @return All the values of annoClass on the object split by pipe (|). If the value is empty, this is null.
     *
     * @see #getAnnotationValues(AnnotatedElement, Class, Pattern, int)
     */
    String[] getAnnotationValues(AnnotatedElement object, Class<? extends Annotation> annoClass) {
        return getAnnotationValues(object, annoClass, ACFPatterns.PIPE, Annotations.REPLACEMENTS);
    }

    /**
     * This fetches all the values the {@link AnnotatedElement}'s annotation of type annoClass has.
     * The value is split by the pattern given and return an array of more indicies than 1.
     *
     * @param object
     *         The element to check the value of the annotation of type annoClass.
     * @param annoClass
     *         The annotation type in form of a class.
     * @param pattern
     *         The pattern the value element is split on.
     *
     * @return All the values of annoClass on the object split by the pattern given. If the value is empty, this is null.
     *
     * @see #getAnnotationValues(AnnotatedElement, Class, Pattern, int)
     */
    String[] getAnnotationValues(AnnotatedElement object, Class<? extends Annotation> annoClass, Pattern pattern) {
        return getAnnotationValues(object, annoClass, pattern, Annotations.REPLACEMENTS);
    }

    /**
     * This fetches all the values the {@link AnnotatedElement}'s annotation of type annoClass has.
     * The value is split by all pipes (|), but must follow the options.
     *
     * @param object
     *         The element to check the value of the annotation of type annoClass.
     * @param annoClass
     *         The annotation type in form of a class.
     * @param options
     *         The options to use. If several options are wanted, use the OR operator (opt1 | opt2).
     *
     * @return All the values of annoClass on the object split by a pipe (|). Nullability depends on options.
     *
     * @see #getAnnotationValues(AnnotatedElement, Class, Pattern, int)
     */
    String[] getAnnotationValues(AnnotatedElement object, Class<? extends Annotation> annoClass, int options) {
        return getAnnotationValues(object, annoClass, ACFPatterns.PIPE, options);
    }

    /**
     * This fetches all the values the {@link AnnotatedElement}'s annotation of type annoClass has.
     * The value is split by the pattern given, and must also follow the options.
     *
     * @param object
     *         The element to check the value of the annotation of type annoClass.
     * @param annoClass
     *         The annotation type in form of a class.
     * @param options
     *         The options to use. If several options are wanted, use the OR operator (opt1 | opt2).
     * @param pattern
     *         The pattern to split by each occurrence of.
     *
     * @return All the values of annoClass on the object split by the pattern given. Nullability depends on options.
     */
    String[] getAnnotationValues(AnnotatedElement object, Class<? extends Annotation> annoClass, Pattern pattern, int options) {
        String value = getAnnotationValue(object, annoClass, options);
        if (value == null) {
            return null;
        }
        return pattern.split(value);
    }

    /**
     * Gets the value of the {@link AnnotatedElement}'s annotation of type annoClass as a string.
     *
     * @param object
     *         The element to check the value of the annotation of type annoClass.
     * @param annoClass
     *         The annotation type in form of a class.
     *
     * @return The value of the annotation on the object given.
     */
    String getAnnotationValue(AnnotatedElement object, Class<? extends Annotation> annoClass) {
        return getAnnotationValue(object, annoClass, Annotations.REPLACEMENTS);
    }

    /**
     * Gets the value of the {@link AnnotatedElement}'s annotation of type annoClass as a string.
     * The value has to follow the given options.
     *
     * @param object
     *         The element to check the value of the annotation of type annoClass.
     * @param annoClass
     *         The annotation type in form of a class.
     * @param options
     *         The options to use. If several options are wanted, use the OR operator (opt1 | opt2).
     *
     * @return The value of the annotation on the object given. Nullability depends on options.
     */
    abstract String getAnnotationValue(AnnotatedElement object, Class<? extends Annotation> annoClass, int options);

    <T extends Annotation> T getAnnotationFromClass(Class<?> clazz, Class<T> annoClass) {
        while (clazz != null && BaseCommand.class.isAssignableFrom(clazz)) {
            T annotation = clazz.getAnnotation(annoClass);
            if (annotation != null) {
                return annotation;
            }
            Class<?> superClass = clazz.getSuperclass();
            while (superClass != null && BaseCommand.class.isAssignableFrom(superClass)) {
                annotation = superClass.getAnnotation(annoClass);
                if (annotation != null) {
                    return annotation;
                }

                superClass = superClass.getSuperclass();
            }

            clazz = clazz.getEnclosingClass();
        }
        return null;
    }

}
