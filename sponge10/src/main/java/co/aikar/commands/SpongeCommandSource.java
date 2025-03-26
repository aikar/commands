package co.aikar.commands;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.util.locale.LocaleSource;
import org.spongepowered.api.util.locale.Locales;

import java.util.Locale;

public class SpongeCommandSource {
    private final @NotNull CommandCause commandCause;
    private final @NotNull Locale locale;

    public SpongeCommandSource(@NotNull CommandCause cause) {
        this.commandCause = cause;
        if (cause instanceof LocaleSource) {
            locale = ((LocaleSource) cause).locale();
        } else if (cause.subject() instanceof LocaleSource) {
            locale = ((LocaleSource) cause.subject()).locale();
        } else {
            locale = Locales.DEFAULT;
        }
    }

    @Deprecated
    public SpongeCommandSource(CommandContext commandContext) {
        this(commandContext.cause());
    }

    @Deprecated
    public <T> SpongeCommandSource(T obj) {
        if (obj instanceof CommandCause) {
            commandCause = (CommandCause) obj;
        } else {
            //having it set to null will lead to unexpected behaviour/NPEs
            throw new IllegalArgumentException("When creating SpongeCommandSource the object must extend CommandCause");
        }

        if (obj instanceof LocaleSource) {
            locale = ((LocaleSource) obj).locale();
        } else {
            locale = Locales.DEFAULT;
        }
    }

    public @NotNull Locale locale() {
        return locale;
    }

    public @NotNull CommandCause commandCause() {
        return commandCause;
    }
}
