package co.aikar.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class SpongeMessageFormatter extends MessageFormatter<TextColor> {

    public SpongeMessageFormatter(TextColor... colors) {
        super(colors);
    }

    public String format(TextColor color, String message) {
        return LegacyComponentSerializer.legacyAmpersand().serialize(Component.text(message).color(color));
    }
}
