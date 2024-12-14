package co.aikar.commands;

public class JDAMessageFormatter extends MessageFormatter<String> {
    public JDAMessageFormatter() {
        // JDA does not support coloring messages outside of embed fields.
        // We pass three empty strings to remove color coded messages from appearing.
        super("", "", "");
    }

    @Override
    String format(String color, String message) {
        return message;
    }
}
