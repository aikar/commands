package co.aikar.commands.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@link Author} annotation is to define whether the parameter should be the author object from the event or
 * parsed from the user's input.
 * <p>
 *      Using this on a User/Member will fetch the author and otherwise it'll parse the input.
 * </p>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Author {
}
