package co.aikar.commands.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@link CrossGuild} annotation is to define whether the parameter should be guild-specific or global.
 * <p>
 *     If a supported parameter is marked with the CrossGuild annotation, the parameter will be filled from
 *     a global perspective (i.e., all of the guilds the bot is connected to). Otherwise, the parameter will
 *     be filled from command input.
 * </p>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CrossGuild {
}
