package co.aikar.commands;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.serializer.TextSerializers;

public class SpongeMessageFormatter implements MessageFormatter {
    private final TextColor color1;
    private final TextColor color2;
    private final TextColor color3;

    public SpongeMessageFormatter(TextColor color1) {
        this(color1, color1);
    }
    public SpongeMessageFormatter(TextColor color1, TextColor color2) {
        this(color1, color2, color2);
    }
    public SpongeMessageFormatter(TextColor color1, TextColor color2, TextColor color3) {
        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;
    }

    @Override
    public String c1(String message) {
        return convert(color1, message);
    }

    @Override
    public String c2(String message) {
        return convert(color2, message);
    }

    @Override
    public String c3(String message) {
        return convert(color3, message);
    }

    private String convert(TextColor color, String message) {
        return TextSerializers.LEGACY_FORMATTING_CODE.serialize(Text.of(color, message));
    }
}
