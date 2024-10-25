package co.aikar.commands;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.serializer.TextSerializers;

public class SpongeMessageFormatter extends MessageFormatter<TextColor> {

    public SpongeMessageFormatter(TextColor... colors) {
        super(colors);
    }

    public String format(TextColor color, String message) {
        return TextSerializers.LEGACY_FORMATTING_CODE.serialize(Text.of(color, message));
    }
}
