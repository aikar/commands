/*
 * Copyright (c) 2016. Starlis LLC / dba Empire Minecraft
 *
 * This source code is proprietary software and must not be redistributed without Starlis LLC's approval
 *
 */

package co.aikar.commands;

public class InvalidCommandArgument extends Exception {
    public boolean showSyntax = true;
    public InvalidCommandArgument() {
        this(null);
    }
    public InvalidCommandArgument(boolean showSyntax) {
        this(null, showSyntax);
    }
    public InvalidCommandArgument(String message) {
        this(message, true);
    }
    public InvalidCommandArgument(String message, boolean showSyntax) {
        super(message, null, false, false);
        this.showSyntax = showSyntax;
    }
}
