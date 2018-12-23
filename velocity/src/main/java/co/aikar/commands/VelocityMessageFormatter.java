package co.aikar.commands;

import net.kyori.text.format.TextColor;

public class VelocityMessageFormatter extends MessageFormatter<TextColor> {

    public VelocityMessageFormatter(TextColor... colors) {
        super(colors);
    }

    @Override
    String format(TextColor color, String message) {
        return color + message;
    }
}