package co.aikar.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VelocityMessageFormatter extends MessageFormatter<TextColor> {

    private static char COLOR_CHAR = '§';

    public VelocityMessageFormatter(TextColor... colors) {
        super(colors);
    }

    @Override
    String format(TextColor color, String message) {
        return hex(message, "#", "");
    }

    /**
     * Colors a String with Hex colors (1.16+ only)
     * Usage: pre + hex code + post
     *
     * @author  Elementeral
     * @param   string The string to color
     * @return  The colored string
     */
    public static String hex(String string, String pre, String post) {
        final Pattern hexPattern = Pattern.compile(pre+"([A-Fa-f0-9]{6})"+post);
        Matcher matcher = hexPattern.matcher(string);
        StringBuffer buffer = new StringBuffer(string.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
            );
        }
        return matcher.appendTail(buffer).toString();
    }
}