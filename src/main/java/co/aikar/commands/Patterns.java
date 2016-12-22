/*
 * Copyright (c) 2016. Starlis LLC / dba Empire Minecraft
 *
 * This source code is proprietary software and must not be redistributed without Starlis LLC's approval
 *
 */

package co.aikar.commands;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@SuppressWarnings("WeakerAccess")
public final class Patterns {
    public static final Pattern COMMA = Pattern.compile(",");
    public static final Pattern NEWLINE = Pattern.compile("\n");
    public static final Pattern DASH = Pattern.compile("-");
    public static final Pattern SPACE = Pattern.compile(" ");
    public static final Pattern SEMICOLON = Pattern.compile(";");
    public static final Pattern COLON = Pattern.compile(":");
    public static final Pattern PIPE = Pattern.compile("\\|");
    public static final Pattern NON_ALPHA_NUMERIC = Pattern.compile("[^a-zA-Z0-9]");
    public static final Pattern INTEGER = Pattern.compile("^[0-9]+$");
    public static final Pattern VALID_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{2,16}$");
    public static final Pattern NON_PRINTABLE_CHARACTERS = Pattern.compile("[^\\x20-\\x7F]");

    public static final Pattern EQUALS = Pattern.compile("=");



    private Patterns() {}
    @SuppressWarnings("Convert2MethodRef")
    static final LoadingCache<String, Pattern> patternCache =
            CacheBuilder.newBuilder()
                    .expireAfterAccess(90, TimeUnit.DAYS)
                    .maximumSize(1024)
                    // has to be this or fails to compile
                    .build(CacheLoader.from((pattern) -> Pattern.compile(pattern)));
    public static Pattern getPattern(String pattern) {
        try {
            return patternCache.get(pattern);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return Pattern.compile(pattern);
        }
    }
}
