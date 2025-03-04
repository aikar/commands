package co.aikar.commands;

import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.util.locale.LocaleSource;
import org.spongepowered.api.util.locale.Locales;

import java.util.Locale;

public class SpongeCommandSource {
    private final CommandCause commandCause;
    private final LocaleSource localeSource;

    public SpongeCommandSource(CommandContext commandContext) {
        commandCause = commandContext.cause();
        Subject subject = commandContext.subject();
        if (subject instanceof LocaleSource) {
            localeSource = (LocaleSource) subject;
        } else {
            localeSource = null;
        }
    }

    public SpongeCommandSource(CommandCause cause) {
        this.commandCause = cause;
        if (cause instanceof LocaleSource) {
            localeSource = (LocaleSource) cause;
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
