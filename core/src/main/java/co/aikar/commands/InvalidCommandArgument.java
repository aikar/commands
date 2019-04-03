/*
 * Copyright (c) 2016-2017 Daniel Ennis (Aikar) - MIT License
 *
 *  Permission is hereby granted, free of charge, to any person obtaining
 *  a copy of this software and associated documentation files (the
 *  "Software"), to deal in the Software without restriction, including
 *  without limitation the rights to use, copy, modify, merge, publish,
 *  distribute, sublicense, and/or sell copies of the Software, and to
 *  permit persons to whom the Software is furnished to do so, subject to
 *  the following conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 *  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 *  OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package co.aikar.commands;

import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;

public class InvalidCommandArgument extends RuntimeException {
    final boolean showSyntax;
    final MessageKey key;
    final String[] replacements;

    public InvalidCommandArgument() {
        this(null, true);
    }

    public InvalidCommandArgument(boolean showSyntax) {
        this(null, showSyntax);
    }

    public InvalidCommandArgument(MessageKeyProvider key, String... replacements) {
        this(key.getMessageKey(), replacements);
    }

    public InvalidCommandArgument(MessageKey key, String... replacements) {
        this(key, true, replacements);
    }

    public InvalidCommandArgument(MessageKeyProvider key, boolean showSyntax, String... replacements) {
        this(key.getMessageKey(), showSyntax, replacements);
    }

    public InvalidCommandArgument(MessageKey key, boolean showSyntax, String... replacements) {
        super(key.getKey(), null, false, false);
        this.showSyntax = showSyntax;
        this.key = key;
        this.replacements = replacements;
    }

    public InvalidCommandArgument(String message) {
        this(message, true);
    }

    public InvalidCommandArgument(String message, boolean showSyntax) {
        super(message, null, false, false);
        this.showSyntax = showSyntax;
        this.replacements = null;
        this.key = null;
    }
}
