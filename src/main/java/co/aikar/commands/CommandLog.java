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

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;

import java.util.logging.Logger;

@SuppressWarnings("WeakerAccess")
final class CommandLog {
    private static final Logger LOGGER = Bukkit.getLogger();

    private CommandLog() {}


    public static void log(String message) {
        info(message);
    }


    public static void info(String message) {
        for (String s : CommandPatterns.NEWLINE.split(message)) {
            LOGGER.info(s);
        }
    }

    public static void warn(String message) {
        for (String s : CommandPatterns.NEWLINE.split(message)) {
            LOGGER.warning(s);
        }
    }

    public static void severe(String message) {
        for (String s : CommandPatterns.NEWLINE.split(message)) {
            LOGGER.severe(s);
        }
    }

    public static void error(String message) {
        severe(message);
    }


    public static void exception(String msg) {
        exception(new Throwable(msg));
    }

    public static void exception(Throwable e) {
        exception(e.getMessage(), e);
    }

    public static void exception(String msg, Throwable e) {
        if (msg != null) {
            severe(msg);
        }
        severe(ExceptionUtils.getFullStackTrace(e));
    }

    public static void exception(Throwable dbg, int lines) {
        if (dbg == null) {
            return;
        }
        severe(dbg.getMessage());
        final StackTraceElement current = new Throwable().getStackTrace()[1];
        severe("c: "+ current.getClassName()+":" + current.getLineNumber());

        final StackTraceElement[] stack = dbg.getStackTrace();
        for (int i = 0; i < lines && i < stack.length; i++) {
            final StackTraceElement cur = stack[i];
            Logger.getGlobal().severe("  " + cur);
        }
    }
}
