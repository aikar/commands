/*
 * Copyright (c) 2016. Starlis LLC / dba Empire Minecraft
 *
 * This source code is proprietary software and must not be redistributed without Starlis LLC's approval
 *
 */

package co.aikar.commands;

import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.logging.Logger;

public final class Log {
    public static Logger LOGGER;

    private Log() {}


    public static void log(String message) {
        info(message);
    }


    public static void info(String message) {
        for (String s : Patterns.NEWLINE.split(message)) {
            LOGGER.info(s);
        }
    }

    public static void warn(String message) {
        for (String s : Patterns.NEWLINE.split(message)) {
            LOGGER.warning(s);
        }
    }

    public static void severe(String message) {
        for (String s : Patterns.NEWLINE.split(message)) {
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
