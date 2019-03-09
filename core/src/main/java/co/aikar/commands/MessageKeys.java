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

/**
 * Enum Name = MessageKey in lowercase prefixed with acf-core.
 */
@SuppressWarnings("WeakerAccess")
public enum MessageKeys implements MessageKeyProvider {
    PERMISSION_DENIED,
    PERMISSION_DENIED_PARAMETER,
    ERROR_GENERIC_LOGGED,
    UNKNOWN_COMMAND,
    INVALID_SYNTAX,
    ERROR_PREFIX,
    ERROR_PERFORMING_COMMAND,
    INFO_MESSAGE,
    PLEASE_SPECIFY_ONE_OF,
    MUST_BE_A_NUMBER,
    MUST_BE_MIN_LENGTH,
    MUST_BE_MAX_LENGTH,
    PLEASE_SPECIFY_AT_LEAST,
    PLEASE_SPECIFY_AT_MOST,
    NOT_ALLOWED_ON_CONSOLE,
    COULD_NOT_FIND_PLAYER,
    NO_COMMAND_MATCHED_SEARCH,
    HELP_PAGE_INFORMATION,
    HELP_NO_RESULTS,
    HELP_HEADER,
    HELP_FORMAT,
    HELP_DETAILED_HEADER,
    HELP_DETAILED_COMMAND_FORMAT,
    HELP_DETAILED_PARAMETER_FORMAT,
    HELP_SEARCH_HEADER,
    ;

    private final MessageKey key = MessageKey.of("acf-core." + this.name().toLowerCase());

    public MessageKey getMessageKey() {
        return key;
    }
}
