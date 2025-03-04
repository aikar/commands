package co.aikar.commands;

import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.util.locale.LocaleSource;
import org.spongepowered.api.util.locale.Locales;

import java.util.Locale;

public class SpongeCommandSource {
    private final CommandCause commandCause;
    private final LocaleSource localeSource;

    public SpongeCommandSource(CommandCause cause) {
        this.commandCause = cause;
        if (cause instanceof LocaleSource) {
            localeSource = (LocaleSource) cause;
        } else if (cause.subject() instanceof LocaleSource) {
            localeSource = (LocaleSource) cause.subject();
        } else {
            localeSource = null;
        }
    }

    public Locale locale() {
        if (localeSource == null) {
            return Locales.DEFAULT;
        }
        return localeSource.locale();
    }

    public CommandCause commandCause() {
        return commandCause;
    }
}
