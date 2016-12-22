/*
 * Copyright (c) 2016. Starlis LLC / dba Empire Minecraft
 *
 * This source code is proprietary software and must not be redistributed without Starlis LLC's approval
 *
 */

package co.aikar.commands;

import org.apache.commons.lang.StringUtils;

public final class NumUtil {
    private NumUtil() {}

    public static int rand(int min, int max) {
        return min + CommandUtil.RANDOM.nextInt(max - min + 1);
    }

    /**
     * Calculate random between 2 points, excluding a center
     * ex: Util.rand(-12, -6, 6, 12) would not return -5 to 5
     * @param min1
     * @param max1
     * @param min2
     * @param max2
     * @return
     */
    public static int rand(int min1, int max1, int min2, int max2) {
        return CommandUtil.randBool() ? rand(min1, max1) : rand(min2, max2);
    }

    public static double rand(double min, double max) {
        return CommandUtil.RANDOM.nextDouble() * (max - min) + min;
    }

    public static boolean isNumber(String str) {
        return StringUtils.isNumeric(str);
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
        if (!Patterns.INTEGER.matcher(string).matches()) {
            return false;
        }
        return true;
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
        if (num >= min && num <= max){
            return true;
        } else {
            return false;
        }
    }

    public static double precision(double x, int p) {
        double pow = Math.pow(10, p);
        return Math.round(x * pow) / pow;
    }
}
