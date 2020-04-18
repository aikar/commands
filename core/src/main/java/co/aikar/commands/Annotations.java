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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.Map;

@SuppressWarnings("TypeParameterExplicitlyExtendsObject")
class Annotations <M extends CommandManager> extends AnnotationLookups {

    public static final int NOTHING = 0;
    public static final int REPLACEMENTS = 1;
    public static final int LOWERCASE = 1 << 1;
    public static final int UPPERCASE = 1 << 2;
    public static final int NO_EMPTY = 1 << 3;
    public static final int DEFAULT_EMPTY = 1 << 4;

    private final M manager;

    private final Map<Class<? extends Annotation>, Method> valueMethods = new IdentityHashMap<>();
    private final Map<Class<? extends Annotation>, Void> noValueAnnotations = new IdentityHashMap<>();

    Annotations(M manager) {
        this.manager = manager;
    }

    String getAnnotationValue(AnnotatedElement object, Class<? extends Annotation> annoClass, int options) {
        Annotation annotation = getAnnotationRecursive(object, annoClass);
        String value = null;

        if (annotation != null) {
            Method valueMethod = valueMethods.get(annoClass);
            if (noValueAnnotations.containsKey(annoClass)) {
                value = "";
            } else {
                try {
                    if (valueMethod == null) {
                        valueMethod = annoClass.getMethod("value");
                        valueMethod.setAccessible(true);
                        valueMethods.put(annoClass, valueMethod);
                    }
                    value = (String) valueMethod.invoke(annotation);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    if (!(e instanceof NoSuchMethodException)) {
                        manager.log(LogLevel.ERROR, "Error getting annotation value", e);
                    }
                    noValueAnnotations.put(annoClass, null);
                    value = "";
                }
            }
        }

        // TODO: Aliases

        if (value == null) {
            if (hasOption(options, DEFAULT_EMPTY)) {
                value = "";
            } else {
                return null;
            }
        }

        // transforms
        if (hasOption(options, REPLACEMENTS)) {
            value = manager.getCommandReplacements().replace(value);
        }
        if (hasOption(options, LOWERCASE)) {
            value = value.toLowerCase(manager.getLocales().getDefaultLocale());
        } else if (hasOption(options, UPPERCASE)) {
            value = value.toUpperCase(manager.getLocales().getDefaultLocale());
        }

        // validation
        if (value.isEmpty() && hasOption(options, NO_EMPTY)) {
            value = null;
        }

        return value;
    }

    private static Annotation getAnnotationRecursive(AnnotatedElement object, Class<? extends Annotation> annoClass) {
        if (object.isAnnotationPresent(annoClass)) {
            return object.getAnnotation(annoClass);
        } else {
            for (Annotation otherAnnotation : object.getDeclaredAnnotations()) {
                if (!otherAnnotation.annotationType().getPackage().getName().startsWith("java.")) {
                    final Annotation foundAnnotation = getAnnotationRecursive(otherAnnotation.annotationType(), annoClass);
                    if (foundAnnotation != null) {
                        return foundAnnotation;
                    }
                }
            }
        }
        return null;
    }

    private static boolean hasOption(int options, int option) {
        return (options & option) == option;
    }

}
