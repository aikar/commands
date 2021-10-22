package co.aikar.commands;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class VelocityMessageFormatter extends MessageFormatter<NamedTextColor> {

    public VelocityMessageFormatter(NamedTextColor... colors) {
        super(colors);
    }

    @Override
    String format(NamedTextColor color, String message) {
        return LegacyComponentSerializer.legacySection().serialize(LegacyComponentSerializer.legacySection().deserialize(message).color(color));
    }
}