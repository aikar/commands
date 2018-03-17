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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@SuppressWarnings("TypeParameterExplicitlyExtendsObject")
class Annotations <
        IT,
        I extends CommandIssuer,
        FT,
        MF extends MessageFormatter<FT>,
        CEC extends CommandExecutionContext<CEC, I>,
        CC extends ConditionContext<I>
        > {

    public static int NOTHING = 0;
    public static int REPLACEMENTS = 1;
    public static int LOWERCASE = 1 << 1;
    public static int UPPERCASE = 1 << 2;
    public static int NO_EMPTY = 1 << 3;

    private final CommandManager<IT, I, FT, MF, CEC, CC> manager;

    private Map<Class<? extends Annotation>, Method> valueMethods = new IdentityHashMap<>();

    Annotations(CommandManager<IT, I, FT, MF, CEC, CC> manager) {
        this.manager = manager;
    }


    //
    // METHODS
    //


    boolean hasAnnotation(Method method, Class<? extends Annotation> annoClass) {
        return getAnnotationValue(method, annoClass, NOTHING) != null;
    }

    String[] getAnnotationValues(Method method, Class<? extends Annotation> annoClass) {
        return getAnnotationValues(method, annoClass, ACFPatterns.PIPE, REPLACEMENTS);
    }
    String[] getAnnotationValues(Method method, Class<? extends Annotation> annoClass, Pattern pattern) {
        return getAnnotationValues(method, annoClass, pattern, REPLACEMENTS);
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
        return getAnnotationValue(method, annoClass, REPLACEMENTS);
    }

    String getAnnotationValue(Method method, Class<? extends Annotation> annoClass, int options) {
        return getValue(method.getAnnotation(annoClass), annoClass, options);
    }


    //
    // PARAMETERS
    //


    String[] getAnnotationValues(Parameter param, Class<? extends Annotation> annoClass) {
        return getAnnotationValues(param, annoClass, ACFPatterns.PIPE, REPLACEMENTS);
    }
    String[] getAnnotationValues(Parameter param, Class<? extends Annotation> annoClass, Pattern pattern) {
        return getAnnotationValues(param, annoClass, pattern, REPLACEMENTS);
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
        return getAnnotationValue(param, annoClass, NOTHING) != null;
    }

    String getAnnotationValue(Parameter param, Class<? extends Annotation> annoClass) {
        return getAnnotationValue(param, annoClass, REPLACEMENTS);
    }

    String getAnnotationValue(Parameter param, Class<? extends Annotation> annoClass, int options) {
        return getValue(param.getAnnotation(annoClass), annoClass, options);
    }

    //
    // FIELDS
    //

    String[] getAnnotationValues(Field field, Class<? extends Annotation> annoClass) {
        return getAnnotationValues(field, annoClass, ACFPatterns.PIPE, REPLACEMENTS);
    }
    String[] getAnnotationValues(Field field, Class<? extends Annotation> annoClass, Pattern pattern) {
        return getAnnotationValues(field, annoClass, pattern, REPLACEMENTS);
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
        return getAnnotationValue(field, annoClass, NOTHING) != null;
    }

    String getAnnotationValue(Field field, Class<? extends Annotation> annoClass) {
        return getAnnotationValue(field, annoClass, REPLACEMENTS);
    }

    String getAnnotationValue(Field field, Class<? extends Annotation> annoClass, int options) {
        return getValue(field.getAnnotation(annoClass), annoClass, options);
    }

    //
    // CLASSES
    //


    String[] getAnnotationValues(Class<? extends Object> clazz, Class<? extends Annotation> annoClass) {
        return getAnnotationValues(clazz, annoClass, ACFPatterns.PIPE, REPLACEMENTS);
    }

    String[] getAnnotationValues(Class<? extends Object> clazz, Class<? extends Annotation> annoClass, Pattern pattern) {
        return getAnnotationValues(clazz, annoClass, pattern, REPLACEMENTS);
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
        return getAnnotationValue(clazz, annoClass, NOTHING) != null;
    }

    String getAnnotationValue(Class<? extends Object> clazz, Class<? extends Annotation> annoClass) {
        return getAnnotationValue(clazz, annoClass, REPLACEMENTS);
    }

    String getAnnotationValue(Class<? extends Object> clazz, Class<? extends Annotation> annoClass, int options) {
        return getValue(clazz.getAnnotation(annoClass), annoClass, options);
    }


    //
    // BASE
    //


    private String getValue(Annotation annotation, Class<? extends Annotation> annoClass, int options) {
        if (annotation == null) {
            // TODO: Alias support
            return null;
        }
        try {
            Method valueMethod = valueMethods.get(annoClass);
            if (valueMethod == null) {
                valueMethod = annoClass.getMethod("value");
                valueMethod.setAccessible(true);
                valueMethods.put(annoClass, valueMethod);
            }
            String value = (String) valueMethod.invoke(annotation);
            if (value == null) {
                // TODO: Alias support
                return null;
            }
            if (hasOption(options, REPLACEMENTS)) {
                value = manager.getCommandReplacements().replace(value);
            }
            if (hasOption(options, LOWERCASE)) {
                value = value.toLowerCase(manager.getLocales().getDefaultLocale());
            } else if (hasOption(options, UPPERCASE)) {
                value = value.toUpperCase(manager.getLocales().getDefaultLocale());
            }
            if (value.isEmpty() && hasOption(options, NO_EMPTY)) {
                return null;
            }
            return value;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            manager.log(LogLevel.ERROR, "Error getting annotation value", e);
        }
        return null;
    }

    private static boolean hasOption(int options, int option) {
        return (options & option) == option;
    }

}
