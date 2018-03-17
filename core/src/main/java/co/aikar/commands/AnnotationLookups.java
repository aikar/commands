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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.regex.Pattern;

abstract class AnnotationLookups {


    //
    // METHODS
    //


    boolean hasAnnotation(Method method, Class<? extends Annotation> annoClass) {
        return getAnnotationValue(method, annoClass, Annotations.NOTHING) != null;
    }

    String[] getAnnotationValues(Method method, Class<? extends Annotation> annoClass) {
        return getAnnotationValues(method, annoClass, ACFPatterns.PIPE, Annotations.REPLACEMENTS);
    }
    String[] getAnnotationValues(Method method, Class<? extends Annotation> annoClass, Pattern pattern) {
        return getAnnotationValues(method, annoClass, pattern, Annotations.REPLACEMENTS);
    }

    String[] getAnnotationValues(Method method, Class<? extends Annotation> annoClass, int options) {
        return getAnnotationValues(method, annoClass, ACFPatterns.PIPE, options);
    }

    String[] getAnnotationValues(Method method, Class<? extends Annotation> annoClass, Pattern pattern, int options) {
        String value = getAnnotationValue(method, annoClass, options);
        if (value == null) {
            return null;
        }
        return pattern.split(value);
    }

    String getAnnotationValue(Method method, Class<? extends Annotation> annoClass) {
        return getAnnotationValue(method, annoClass, Annotations.REPLACEMENTS);
    }

    String getAnnotationValue(Method method, Class<? extends Annotation> annoClass, int options) {
        return getValue(method.getAnnotation(annoClass), annoClass, options);
    }


    //
    // PARAMETERS
    //


    String[] getAnnotationValues(Parameter param, Class<? extends Annotation> annoClass) {
        return getAnnotationValues(param, annoClass, ACFPatterns.PIPE, Annotations.REPLACEMENTS);
    }
    String[] getAnnotationValues(Parameter param, Class<? extends Annotation> annoClass, Pattern pattern) {
        return getAnnotationValues(param, annoClass, pattern, Annotations.REPLACEMENTS);
    }

    String[] getAnnotationValues(Parameter param, Class<? extends Annotation> annoClass, int options) {
        return getAnnotationValues(param, annoClass, ACFPatterns.PIPE, options);
    }
    String[] getAnnotationValues(Parameter param, Class<? extends Annotation> annoClass, Pattern pattern, int options) {
        String value = getAnnotationValue(param, annoClass, options);
        if (value == null) {
            return null;
        }
        return pattern.split(value);
    }

    boolean hasAnnotation(Parameter param, Class<? extends Annotation> annoClass) {
        return getAnnotationValue(param, annoClass, Annotations.NOTHING) != null;
    }

    String getAnnotationValue(Parameter param, Class<? extends Annotation> annoClass) {
        return getAnnotationValue(param, annoClass, Annotations.REPLACEMENTS);
    }

    String getAnnotationValue(Parameter param, Class<? extends Annotation> annoClass, int options) {
        return getValue(param.getAnnotation(annoClass), annoClass, options);
    }

    //
    // FIELDS
    //

    String[] getAnnotationValues(Field field, Class<? extends Annotation> annoClass) {
        return getAnnotationValues(field, annoClass, ACFPatterns.PIPE, Annotations.REPLACEMENTS);
    }
    String[] getAnnotationValues(Field field, Class<? extends Annotation> annoClass, Pattern pattern) {
        return getAnnotationValues(field, annoClass, pattern, Annotations.REPLACEMENTS);
    }

    String[] getAnnotationValues(Field field, Class<? extends Annotation> annoClass, int options) {
        return getAnnotationValues(field, annoClass, ACFPatterns.PIPE, options);
    }
    String[] getAnnotationValues(Field field, Class<? extends Annotation> annoClass, Pattern pattern, int options) {
        String value = getAnnotationValue(field, annoClass, options);
        if (value == null) {
            return null;
        }
        return pattern.split(value);
    }

    boolean hasAnnotation(Field field, Class<? extends Annotation> annoClass) {
        return getAnnotationValue(field, annoClass, Annotations.NOTHING) != null;
    }

    String getAnnotationValue(Field field, Class<? extends Annotation> annoClass) {
        return getAnnotationValue(field, annoClass, Annotations.REPLACEMENTS);
    }

    String getAnnotationValue(Field field, Class<? extends Annotation> annoClass, int options) {
        return getValue(field.getAnnotation(annoClass), annoClass, options);
    }

    //
    // CLASSES
    //


    String[] getAnnotationValues(Class<? extends Object> clazz, Class<? extends Annotation> annoClass) {
        return getAnnotationValues(clazz, annoClass, ACFPatterns.PIPE, Annotations.REPLACEMENTS);
    }

    String[] getAnnotationValues(Class<? extends Object> clazz, Class<? extends Annotation> annoClass, Pattern pattern) {
        return getAnnotationValues(clazz, annoClass, pattern, Annotations.REPLACEMENTS);
    }

    String[] getAnnotationValues(Class<? extends Object> clazz, Class<? extends Annotation> annoClass, int options) {
        return getAnnotationValues(clazz, annoClass, ACFPatterns.PIPE, options);
    }
    String[] getAnnotationValues(Class<? extends Object> clazz, Class<? extends Annotation> annoClass, Pattern pattern, int options) {
        String value = getAnnotationValue(clazz, annoClass, options);
        if (value == null) {
            return null;
        }
        return pattern.split(value);
    }
    boolean hasAnnotation(Class<? extends Object> clazz, Class<? extends Annotation> annoClass) {
        return getAnnotationValue(clazz, annoClass, Annotations.NOTHING) != null;
    }

    String getAnnotationValue(Class<? extends Object> clazz, Class<? extends Annotation> annoClass) {
        return getAnnotationValue(clazz, annoClass, Annotations.REPLACEMENTS);
    }

    String getAnnotationValue(Class<? extends Object> clazz, Class<? extends Annotation> annoClass, int options) {
        return getValue(clazz.getAnnotation(annoClass), annoClass, options);
    }

    abstract String getValue(Annotation annotation, Class<? extends Annotation> annoClass, int options);
}
