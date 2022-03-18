package co.aikar.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class SpongeMessageFormatter extends MessageFormatter<NamedTextColor> {

    public SpongeMessageFormatter(NamedTextColor... colors) {
        super(colors);
    }

    public String format(NamedTextColor color, String message) {
        return PlainTextComponentSerializer.plainText().serialize(Component.text(message).color(color));
    }
}
