package co.aikar.commands;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.util.locale.LocaleSource;
import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3d;

import java.util.Locale;
import java.util.Optional;

public class SpongeCommandSource implements LocaleSource, CommandCause {
    private CommandCause commandCause;
    private LocaleSource localeSource;

    public SpongeCommandSource(CommandContext commandContext) {
        commandCause = commandContext.cause();
        Subject subject = commandContext.subject();
        if (subject instanceof LocaleSource) {
            localeSource = (LocaleSource) subject;
        } else {
            localeSource = null;
        }
    }

    public <T> SpongeCommandSource(T obj) {
        if (obj instanceof CommandCause) {
            commandCause = (CommandCause) obj;
        }

        if (obj instanceof LocaleSource) {
            localeSource = (LocaleSource) obj;
        }
    }

    @Override
    public Locale locale() {
        if (localeSource == null) {
            return Locales.DEFAULT;
        }
        return localeSource.locale();
    }

    @Override
    public Cause cause() {
        return commandCause.cause();
    }

    @Override
    public Subject subject() {
        return commandCause.subject();
    }

    @Override
    public Audience audience() {
        return commandCause.audience();
    }

    @Override
    public Optional<ServerLocation> location() {
        return commandCause.location();
    }

    @Override
    public Optional<Vector3d> rotation() {
        return commandCause.rotation();
    }

    @Override
    public Optional<BlockSnapshot> targetBlock() {
        return commandCause.targetBlock();
    }

    @Override
    public void sendMessage(Component message) {
        commandCause.sendMessage(message);
    }

    @Override
    public void sendMessage(Identified source, Component message) {
        commandCause.sendMessage(source, message);
    }

    @Override
    public void sendMessage(Identity source, Component message) {
        commandCause.sendMessage(source, message);
    }
}
