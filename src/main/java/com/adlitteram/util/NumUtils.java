/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.adlitteram.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.StringTokenizer;

public class NumUtils {
    private NumUtils() {
    }

    public static final double DEGREE_TO_RADIAN = Math.PI / 180d;
    public static final double RADIAN_TO_DEGGRE = 180d / Math.PI;

    public static final double INtoU = 72;
    public static final double UtoIN = 1f / 72f;
    public static final double MMtoU = 72f / 25.4f;
    public static final double UtoMM = 25.4f / 72f;
    public static final double CMtoU = 72f / 2.54f;
    public static final double UtoCM = 2.54f / 72f;

    public static final int MM = 0;
    public static final int CM = 1;
    public static final int PT = 2;
    public static final int IN = 3;

    private static final String[] UNIT_ARRAY = {"mm", "cm", "pt", "in"};

    public static int getUnit(String str) {
        for (int i = 0; i < UNIT_ARRAY.length; i++) {
            if (UNIT_ARRAY[i].equalsIgnoreCase(str)) {
                return i;
            }
        }
        return MM;
    }

    public static String getUnitName(int unit) {
        return UNIT_ARRAY[unit];
    }

    public static String[] getAllUnits() {
        return UNIT_ARRAY.clone();
    }

    // Convert String to point value
    public static double pointValue(String str) throws NumberFormatException {
        str = str.trim();
        int len = str.length() - 2;
        if (len > 0) {
            String unit = str.substring(len).toLowerCase();
            if (unit.compareTo("pt") == 0) {
                return Double.parseDouble(str.substring(0, len));
            }
            if (unit.compareTo("mm") == 0) {
                return MMtoU * Double.parseDouble(str.substring(0, len));
            }
            if (unit.compareTo("cm") == 0) {
                return CMtoU * Double.parseDouble(str.substring(0, len));
            }
            if (unit.compareTo("in") == 0) {
                return INtoU * Double.parseDouble(str.substring(0, len));
            }
        }
        return Double.parseDouble(str);
    }

    public static double pointValue(Object obj) throws NumberFormatException {
        return (obj instanceof String str) ? pointValue(str) : ((Number) obj).doubleValue();
    }

    public static String toUnit(Object value, int unit) {
        return (value == null) ? null : toUnit(pointValue(value), unit);
    }

    public static String toUnit(double value, int unit) {
        switch (unit) {
            case MM:
                return roundDecimal(value * UtoMM, 1) + " mm";

            case IN:
                return roundDecimal(value * UtoIN, 2) + " in";

            case CM:
                return roundDecimal(value * UtoCM, 2) + " cm";
        }
        return String.valueOf(value);
    }

    public static String roundDecimal(double d, int accuracy) {
        BigDecimal bd = BigDecimal.valueOf(d);
        return String.valueOf(bd.setScale(accuracy, RoundingMode.HALF_UP).doubleValue());
    }

    // Primitive type
    public static boolean booleanValue(Object obj) throws NumberFormatException {
        return (obj instanceof String str) ? Boolean.parseBoolean(str) : (Boolean) obj;
    }

    public static int intValue(Object obj) throws NumberFormatException {
        return (obj instanceof String str) ? Integer.parseInt(str) : (Integer) obj;
    }

    public static long longValue(Object obj) throws NumberFormatException {
        return (obj instanceof String str) ? Long.parseLong(str) : (Long) obj;
    }

    public static float floatValue(Object obj) throws NumberFormatException {
        return (obj instanceof String str) ? Float.parseFloat(str) : (Float) obj;
    }

    public static double doubleValue(Object obj) throws NumberFormatException {
        return (obj instanceof String str) ? Double.parseDouble(str) : (Double) obj;
    }

    public static boolean booleanValue(Object obj, boolean value) {
        try {
            return booleanValue(obj);
        } catch (RuntimeException e) {
            return value;
        }
    }

    public static int intValue(Object obj, int value) {
        try {
            return intValue(obj);
        } catch (RuntimeException e) {
            return value;
        }
    }

    public static long longValue(Object obj, long value) {
        try {
            return longValue(obj);
        } catch (RuntimeException e) {
            return value;
        }
    }

    public static float floatValue(Object obj, float value) {
        try {
            return floatValue(obj);
        } catch (RuntimeException e) {
            return value;
        }
    }

    public static double doubleValue(Object obj, double value) {
        try {
            return doubleValue(obj);
        } catch (RuntimeException e) {
            return value;
        }
    }

    // Validate value
    public static boolean isValidBoolean(Object obj) {
        try {
            booleanValue(obj);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    // Return value between Min & Max
    public static int clamp(int min, int value, int max) {
        return (value < min) ? min : Math.min(value, max);
    }

    public static long clamp(long min, long value, long max) {
        return (value < min) ? min : Math.min(value, max);
    }

    public static float clamp(float min, float value, float max) {
        return (value < min) ? min : Math.min(value, max);
    }

    public static double clamp(double min, double value, double max) {
        return (value < min) ? min : Math.min(value, max);
    }

    // Format Size to Byte
    public static String toByteSize(long s) {
        if (s < 1024) {
            return s + " Bytes";
        } else if (s < 1024 * 1024 * 1024) {
            return Math.round(s / 1024) + " Kb";
        } else if (s < 1024L * 1024 * 1024 * 1024) {
            return Math.round(s / (1024 * 1024)) + " Mb";
        } else {
            return Math.round(s / (1024 * 1024 * 1024)) + " Gb";
        }
    }

    public static float toDegree(float f) {
        return (float) (RADIAN_TO_DEGGRE * f);
    }

    public static double toDegree(double d) {
        return RADIAN_TO_DEGGRE * d;
    }

    public static float toRadian(float f) {
        return (float) (DEGREE_TO_RADIAN * f);
    }

    public static double toRadian(double d) {
        return DEGREE_TO_RADIAN * d;
    }
}
