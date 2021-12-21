package co.aikar.commands;

import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class VelocityMessageFormatter extends MessageFormatter<TextColor> {

    public VelocityMessageFormatter(TextColor... colors) {
        super(colors);
    }

    @Override
    @SuppressWarnings("deprecation")
    String format(TextColor color, String message) {
        return LegacyComponentSerializer.legacy('&').serialize(LegacyComponentSerializer.legacy('&').deserialize(message).color(color));
    }
}