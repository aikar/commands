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


import co.aikar.commands.apachecommonslang.ApacheCommonsLangUtil;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class ACFUtil {

    public static final Random RANDOM = new Random();

    private ACFUtil() {
    }

    public static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }

    public static String padLeft(String s, int n) {
        return String.format("%1$" + n + "s", s);
    }

    public static String formatNumber(Integer balance) {
        return NumberFormat.getInstance().format(balance);
    }

    public static <T extends Enum> T getEnumFromName(T[] types, String name) {
        return getEnumFromName(types, name, null);
    }

    public static <T extends Enum> T getEnumFromName(T[] types, String name, T def) {
        for (T type : types) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return def;
    }

    public static <T extends Enum> T getEnumFromOrdinal(T[] types, int ordinal) {
        for (T type : types) {
            if (type.ordinal() == ordinal) {
                return type;
            }
        }
        return null;
    }

    public static String ucfirst(String str) {
        return ApacheCommonsLangUtil.capitalizeFully(str);
    }

    public static Double parseDouble(String var) {
        return parseDouble(var, null);
    }

    public static Double parseDouble(String var, Double def) {
        if (var == null) {
            return def;
        }
        try {
            return Double.parseDouble(var);
        } catch (NumberFormatException ignored) {
        }
        return def;
    }

    public static Float parseFloat(String var) {
        return parseFloat(var, null);
    }

    public static Float parseFloat(String var, Float def) {
        if (var == null) {
            return def;
        }
        try {
            return Float.parseFloat(var);
        } catch (NumberFormatException ignored) {
        }
        return def;
    }

    public static Long parseLong(String var) {
        return parseLong(var, null);
    }

    public static Long parseLong(String var, Long def) {
        if (var == null) {
            return def;
        }
        try {
            return Long.parseLong(var);
        } catch (NumberFormatException ignored) {
        }
        return def;
    }

    public static Integer parseInt(String var) {
        return parseInt(var, null);
    }

    public static Integer parseInt(String var, Integer def) {
        if (var == null) {
            return def;
        }
        try {
            return Integer.parseInt(var);
        } catch (NumberFormatException ignored) {
        }
        return def;
    }

    public static boolean randBool() {
        return RANDOM.nextBoolean();
    }

    public static <T> T nullDefault(Object val, Object def) {
        //noinspection unchecked
        return (T) (val != null ? val : def);
    }

    public static String join(Collection<String> args) {
        return ApacheCommonsLangUtil.join(args, " ");
    }

    public static String join(Collection<String> args, String sep) {
        return ApacheCommonsLangUtil.join(args, sep);
    }

    public static String join(String[] args) {
        return join(args, 0, ' ');
    }

    public static String join(String[] args, String sep) {
        return ApacheCommonsLangUtil.join(args, sep);
    }

    public static String join(String[] args, char sep) {
        return join(args, 0, sep);
    }

    public static String join(String[] args, int index) {
        return join(args, index, ' ');
    }

    public static String join(String[] args, int index, char sep) {
        return ApacheCommonsLangUtil.join(args, sep, index, args.length);
    }

    public static String simplifyString(String str) {
        if (str == null) {
            return null;
        }
        return ACFPatterns.NON_ALPHA_NUMERIC.matcher(str.toLowerCase(Locale.ENGLISH)).replaceAll("");
    }

    public static double round(double x, int scale) {
        try {
            return (new BigDecimal
                    (Double.toString(x))
                    .setScale(scale, BigDecimal.ROUND_HALF_UP))
                    .doubleValue();
        } catch (NumberFormatException ex) {
            if (Double.isInfinite(x)) {
                return x;
            } else {
                return Double.NaN;
            }
        }
    }

    public static int roundUp(int num, int multiple) {
        if (multiple == 0) {
            return num;
        }

        int remainder = num % multiple;
        if (remainder == 0) {
            return num;
        }
        return num + multiple - remainder;

    }

    public static String limit(String str, int limit) {
        return str.length() > limit ? str.substring(0, limit) : str;
    }

    /**
     * Plain string replacement, escapes replace value.
     *
     * @param string
     * @param pattern
     * @param repl
     * @return
     */
    public static String replace(String string, Pattern pattern, String repl) {
        return pattern.matcher(string).replaceAll(Matcher.quoteReplacement(repl));
    }

    /**
     * Regex version of {@link #replace(String, Pattern, String)}
     *
     * @param string
     * @param pattern
     * @param repl
     * @return
     */
    public static String replacePattern(String string, Pattern pattern, String repl) {
        return pattern.matcher(string).replaceAll(repl);
    }

    /**
     * Plain String replacement. If you need regex patterns, see {@link #replacePattern(String, String, String)}
     *
     * @param string
     * @param pattern
     * @param repl
     * @return
     */
    public static String replace(String string, String pattern, String repl) {
        return replace(string, ACFPatterns.getPattern(Pattern.quote(pattern)), repl);
    }

    /**
     * Regex version of {@link #replace(String, String, String)}
     *
     * @param string
     * @param pattern
     * @param repl
     * @return
     */
    public static String replacePattern(String string, String pattern, String repl) {
        return replace(string, ACFPatterns.getPattern(pattern), repl);
    }

    /**
     * Pure Regex Pattern matching and replacement, no escaping
     *
     * @param string
     * @param pattern
     * @param repl
     * @return
     */
    public static String replacePatternMatch(String string, Pattern pattern, String repl) {
        return pattern.matcher(string).replaceAll(repl);
    }

    /**
     * Pure Regex Pattern matching and replacement, no escaping
     *
     * @param string
     * @param pattern
     * @param repl
     * @return
     */
    public static String replacePatternMatch(String string, String pattern, String repl) {
        return replacePatternMatch(string, ACFPatterns.getPattern(pattern), repl);
    }

    public static String replaceStrings(String string, String... replacements) {
        if (replacements.length < 2 || replacements.length % 2 != 0) {
            throw new IllegalArgumentException("Invalid Replacements");
        }
        for (int i = 0; i < replacements.length; i += 2) {
            String key = replacements[i];
            String value = replacements[i + 1];
            if (value == null) value = "";
            string = replace(string, key, value);
        }
        return string;
    }

    public static String replacePatterns(String string, String... replacements) {
        if (replacements.length < 2 || replacements.length % 2 != 0) {
            throw new IllegalArgumentException("Invalid Replacements");
        }
        for (int i = 0; i < replacements.length; i += 2) {
            String key = replacements[i];
            String value = replacements[i + 1];
            if (value == null) value = "";
            string = replacePattern(string, key, value);
        }
        return string;
    }

    public static String capitalize(String str, char[] delimiters) {
        return ApacheCommonsLangUtil.capitalize(str, delimiters);
    }

    private static boolean isDelimiter(char ch, char[] delimiters) {
        return ApacheCommonsLangUtil.isDelimiter(ch, delimiters);
    }

    public static <T> T random(List<T> arr) {
        if (arr == null || arr.isEmpty()) {
            return null;
        }
        return arr.get(RANDOM.nextInt(arr.size()));
    }

    public static <T> T random(T[] arr) {
        if (arr == null || arr.length == 0) {
            return null;
        }
        return arr[RANDOM.nextInt(arr.length)];
    }

    /**
     * Added as im sure we will try to "Find this" again. This is no different than Enum.values() passed to above method logically
     * but the array version is slightly faster.
     *
     * @param enm
     * @param <T>
     * @return
     */
    @Deprecated
    public static <T extends Enum<?>> T random(Class<? extends T> enm) {
        return random(enm.getEnumConstants());
    }

    public static String normalize(String s) {
        if (s == null) {
            return null;
        }
        return ACFPatterns.NON_PRINTABLE_CHARACTERS.matcher(Normalizer.normalize(s, Form.NFD)).replaceAll("");
    }

    public static int indexOf(String arg, String[] split) {
        for (int i = 0; i < split.length; i++) {
            if (arg == null) {
                if (split[i] == null) {
                    return i;
                }
            } else if (arg.equals(split[i])) {
                return i;
            }
        }
        return -1;
    }

    public static String capitalizeFirst(String name) {
        return capitalizeFirst(name, '_');
    }

    public static String capitalizeFirst(String name, char separator) {
        name = name.toLowerCase(Locale.ENGLISH);
        String[] split = name.split(Character.toString(separator));
        StringBuilder total = new StringBuilder(3);
        for (String s : split) {
            total.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).append(' ');
        }

        return total.toString().trim();
    }

    public static String ltrim(String s) {
        int i = 0;
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
            i++;
        }
        return s.substring(i);
    }

    public static String rtrim(String s) {
        int i = s.length() - 1;
        while (i >= 0 && Character.isWhitespace(s.charAt(i))) {
            i--;
        }
        return s.substring(0, i + 1);
    }

    public static List<String> enumNames(Enum<?>[] values) {
        return Stream.of(values).map(Enum::name).collect(Collectors.toList());
    }

    public static List<String> enumNames(Class<? extends Enum<?>> cls) {
        return enumNames(cls.getEnumConstants());
    }

    public static String combine(String[] args) {
        return combine(args, 0);
    }

    public static String combine(String[] args, int start) {
        int size = 0;
        for (int i = start; i < args.length; i++) {
            size += args[i].length();
        }
        StringBuilder sb = new StringBuilder(size);
        for (int i = start; i < args.length; i++) {
            sb.append(args[i]);
        }
        return sb.toString();
    }


    @Nullable
    public static <E extends Enum<E>> E simpleMatch(Class<? extends Enum<?>> list, String item) {
        if (item == null) {
            return null;
        }
        item = ACFUtil.simplifyString(item);
        for (Enum<?> s : list.getEnumConstants()) {
            String simple = ACFUtil.simplifyString(s.name());
            if (item.equals(simple)) {
                //noinspection unchecked
                return (E) s;
            }
        }

        return null;
    }

    public static boolean isTruthy(String test) {
        switch (test) {
            case "t":
            case "true":
            case "on":
            case "y":
            case "yes":
            case "1":
                return true;
        }
        return false;
    }


    public static Number parseNumber(String num, boolean suffixes) {
        if (ACFPatterns.getPattern("^0x([0-9A-Fa-f]*)$").matcher(num).matches()) {
            return Long.parseLong(num.substring(2), 16);
        } else if (ACFPatterns.getPattern("^0b([01]*)$").matcher(num).matches()) {
            return Long.parseLong(num.substring(2), 2);
        } else {
            ApplyModifierToNumber applyModifierToNumber = new ApplyModifierToNumber(num, suffixes).invoke();
            num = applyModifierToNumber.getNum();
            double mod = applyModifierToNumber.getMod();

            return Double.parseDouble(num) * mod;
        }
    }

    public static BigDecimal parseBigNumber(String num, boolean suffixes) {
        ApplyModifierToNumber applyModifierToNumber = new ApplyModifierToNumber(num, suffixes).invoke();
        num = applyModifierToNumber.getNum();
        double mod = applyModifierToNumber.getMod();

        BigDecimal big = new BigDecimal(num);
        return (mod == 1) ? big : big.multiply(new BigDecimal(mod));
    }

    public static <T> boolean hasIntersection(Collection<T> list1, Collection<T> list2) {
        for (T t : list1) {
            if (list2.contains(t)) {
                return true;
            }
        }

        return false;
    }

    public static <T> Collection<T> intersection(Collection<T> list1, Collection<T> list2) {
        List<T> list = new ArrayList<>();

        for (T t : list1) {
            if (list2.contains(t)) {
                list.add(t);
            }
        }

        return list;
    }

    public static int rand(int min, int max) {
        return min + RANDOM.nextInt(max - min + 1);
    }

    /**
     * Calculate random between 2 points, excluding a center
     * ex: Util.rand(-12, -6, 6, 12) would not return -5 to 5
     *
     * @param min1
     * @param max1
     * @param min2
     * @param max2
     * @return
     */
    public static int rand(int min1, int max1, int min2, int max2) {
        return randBool() ? rand(min1, max1) : rand(min2, max2);
    }

    public static double rand(double min, double max) {
        return RANDOM.nextDouble() * (max - min) + min;
    }

    public static boolean isNumber(String str) {
        return ApacheCommonsLangUtil.isNumeric(str);
    }

    public static String intToRoman(int integer) {
        if (integer == 1) {
            return "I";
        }
        if (integer == 2) {
            return "II";
        }
        if (integer == 3) {
            return "III";
        }
        if (integer == 4) {
            return "IV";
        }
        if (integer == 5) {
            return "V";
        }
        if (integer == 6) {
            return "VI";
        }
        if (integer == 7) {
            return "VII";
        }
        if (integer == 8) {
            return "VIII";
        }
        if (integer == 9) {
            return "IX";
        }
        if (integer == 10) {
            return "X";
        }
        return null;
    }

    public static boolean isInteger(String string) {
        return ACFPatterns.INTEGER.matcher(string).matches();
    }

    public static boolean isFloat(String string) {
        try {
            Float.parseFloat(string);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isDouble(String string) {
        try {
            Double.parseDouble(string);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isBetween(float num, double min, double max) {
        return num >= min && num <= max;
    }

    @SuppressWarnings("SameParameterValue")
    public static double precision(double x, int p) {
        double pow = Math.pow(10, p);
        return Math.round(x * pow) / pow;
    }

    public static void sneaky(Throwable t) {
        //noinspection RedundantTypeArguments
        throw ACFUtil.<RuntimeException>superSneaky(t);
    }

    private static <T extends Throwable> T superSneaky(Throwable t) throws T {
        // noinspection unchecked
        throw (T) t;
    }

    public static <T> List<T> preformOnImmutable(List<T> list, Consumer<List<T>> action) {
        try {
            action.accept(list);
        } catch (UnsupportedOperationException ex) {
            list = new ArrayList<>(list);
            action.accept(list);
        }

        return list;
    }

    public static <T> T getFirstElement(Iterable<T> iterable) {
        if (iterable == null) {
            return null;
        }
        Iterator<T> iterator = iterable.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }

        return null;
    }

    private static class ApplyModifierToNumber {
        private String num;
        private boolean suffixes;
        private double mod;

        public ApplyModifierToNumber(String num, boolean suffixes) {
            this.num = num;
            this.suffixes = suffixes;
        }

        public String getNum() {
            return num;
        }

        public double getMod() {
            return mod;
        }

        public ApplyModifierToNumber invoke() {
            mod = 1;
            if (suffixes) {
                switch (num.charAt(num.length() - 1)) {
                    case 'M':
                    case 'm':
                        mod = 1000000D;
                        num = num.substring(0, num.length() - 1);
                        break;
                    case 'K':
                    case 'k':
                        mod = 1000D;
                        num = num.substring(0, num.length() - 1);
                }
            }
            return this;
        }
    }
}
