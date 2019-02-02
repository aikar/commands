package co.aikar.commands;

import net.kyori.text.format.TextColor;
import net.kyori.text.serializer.ComponentSerializers;

public class VelocityMessageFormatter extends MessageFormatter<TextColor> {

    public VelocityMessageFormatter(TextColor... colors) {
        super(colors);
    }

    @Override
    @SuppressWarnings("deprecation")
    String format(TextColor color, String message) {
        return ComponentSerializers.LEGACY.serialize(ComponentSerializers.LEGACY.deserialize(message).color(color));
    }
}