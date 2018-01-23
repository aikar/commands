package co.aikar.commands;

public class JDAMessageFormatter extends MessageFormatter<String> {
    @Override
    String format(String color, String message) {
        return message;
    }
}
