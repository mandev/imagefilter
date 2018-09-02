/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.adlitteram.util;

import java.math.BigDecimal;
import java.util.StringTokenizer;

public class NumUtils {

    public static final double DEGREE_TO_RADIAN = Math.PI / 180d;
    public static final double RADIAN_TO_DEGGRE = 180d / Math.PI;
    //
    public static final double INtoU = 72;
    public static final double UtoIN = 1f / 72f;
    public static final double MMtoU = 72f / 25.4f;
    public static final double UtoMM = 25.4f / 72f;
    public static final double CMtoU = 72f / 2.54f;
    public static final double UtoCM = 2.54f / 72f;
    //
    public static final int MM = 0;
    public static final int CM = 1;
    public static final int PT = 2;
    public static final int IN = 3;
    //
    private static final String[] unitArray = {"mm", "cm", "pt", "in"};

    public static int getUnit(String str) {
        for (int i = 0; i < unitArray.length; i++) {
            if (unitArray[i].equalsIgnoreCase(str)) return i;
        }
        return MM;
    }

    public static String getUnitName(int unit) {
        return unitArray[unit];
    }

    public static String[] getAllUnits() {
        return unitArray.clone();
    }

    // Convert String to point value
    public static double pointValue(String str) throws NumberFormatException {
        str = str.trim();
        int len = str.length() - 2;
        if (len > 0) {
            String unit = str.substring(len).toLowerCase();
            if (unit.compareTo("pt") == 0) return Double.parseDouble(str.substring(0, len));
            if (unit.compareTo("mm") == 0) return MMtoU * Double.parseDouble(str.substring(0, len));
            if (unit.compareTo("cm") == 0) return CMtoU * Double.parseDouble(str.substring(0, len));
            if (unit.compareTo("in") == 0) return INtoU * Double.parseDouble(str.substring(0, len));
        }
        return Double.parseDouble(str);
    }

    public static double pointValue(Object obj) throws NumberFormatException {
        return (obj instanceof String) ? pointValue((String) obj) : ((Number) obj).doubleValue();
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
        BigDecimal bd = new BigDecimal(d);
        return String.valueOf(bd.setScale(accuracy, BigDecimal.ROUND_HALF_UP).doubleValue());
    }

    // Primitive type
    public static boolean booleanValue(Object obj) throws NumberFormatException {
        return (obj instanceof String) ? Boolean.valueOf((String) obj).booleanValue() : ((Boolean) obj).booleanValue();
    }

    public static int intValue(Object obj) throws NumberFormatException {
        return (obj instanceof String) ? Integer.parseInt((String) obj) : ((Integer) obj).intValue();
    }

    public static long longValue(Object obj) throws NumberFormatException {
        return (obj instanceof String) ? Long.parseLong((String) obj) : ((Long) obj).longValue();
    }

    public static float floatValue(Object obj) throws NumberFormatException {
        return (obj instanceof String) ? Float.parseFloat((String) obj) : ((Float) obj).floatValue();
    }

    public static double doubleValue(Object obj) throws NumberFormatException {
        return (obj instanceof String) ? Double.parseDouble((String) obj) : ((Double) obj).doubleValue();
    }

    public static boolean booleanValue(Object obj, boolean value) {
        try {
            return booleanValue(obj);
        }
        catch (RuntimeException e) {
            return value;
        }
    }

    public static int intValue(Object obj, int value) {
        try {
            return intValue(obj);
        }
        catch (RuntimeException e) {
            return value;
        }
    }

    public static long longValue(Object obj, long value) {
        try {
            return longValue(obj);
        }
        catch (RuntimeException e) {
            return value;
        }
    }

    public static float floatValue(Object obj, float value) {
        try {
            return floatValue(obj);
        }
        catch (RuntimeException e) {
            return value;
        }
    }

    public static double doubleValue(Object obj, double value) {
        try {
            return doubleValue(obj);
        }
        catch (RuntimeException e) {
            return value;
        }
    }

    // Validate value
    public static boolean isValidBoolean(Object obj) {
        try {
            booleanValue(obj);
            return true;
        }
        catch (RuntimeException e) {
            return false;
        }
    }

    public static boolean isValidInt(Object obj) {
        try {
            intValue(obj);
            return true;
        }
        catch (RuntimeException e) {
            return false;
        }
    }

    public static boolean isValidLong(Object obj) {
        try {
            longValue(obj);
            return true;
        }
        catch (RuntimeException e) {
            return false;
        }
    }

    public static boolean isValidFloat(Object obj) {
        try {
            floatValue(obj);
            return true;
        }
        catch (RuntimeException e) {
            return false;
        }
    }

    public static boolean isValidDouble(Object obj) {
        try {
            doubleValue(obj);
            return true;
        }
        catch (RuntimeException e) {
            return false;
        }
    }

    // Math
    public static boolean isEven(int a) {    // pair
        return (a & 1) == 0;
    }

    public static boolean isEven(long a) {
        return (a & 1L) == 0;
    }

    public static boolean isEven(double a) {
        return a % 2 == 0;
    }

    public static boolean isEven(float a) {
        return a % 2 == 0;
    }

    public static boolean isOdd(int a) {    // impair
        return (a & 1) != 0;
    }

    public static boolean isOdd(long a) {
        return (a & 1L) != 0;
    }

    public static boolean isOdd(double a) {
        return a % 2 != 0;
    }

    public static boolean isOdd(float a) {
        return a % 2 != 0;
    }

    // Return value between Min & Max
    public static int clamp(int min, int value, int max) {
        return (value < min) ? min : (value > max) ? max : value;
    }

    public static long clamp(long min, long value, long max) {
        return (value < min) ? min : (value > max) ? max : value;
    }

    public static float clamp(float min, float value, float max) {
        return (value < min) ? min : (value > max) ? max : value;
    }

    public static double clamp(double min, double value, double max) {
        return (value < min) ? min : (value > max) ? max : value;
    }

    // String <=> Array
    public static int[] stringToIntArray(String str) {
        StringTokenizer st = new StringTokenizer(str);
        int[] array = new int[st.countTokens()];
        for (int i = 0; i < array.length; i++) {
            array[i] = intValue(st.nextToken());
        }
        return array;
    }

    public static String intArrayToString(int[] array) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            buffer.append(array[i]).append(" ");
        }
        return buffer.toString();
    }

    public static float[] stringToFloatArray(String str) {
        StringTokenizer st = new StringTokenizer(str);
        float[] array = new float[st.countTokens()];
        for (int i = 0; i < array.length; i++) {
            array[i] = floatValue(st.nextToken());
        }
        return array;
    }

    public static String floatArrayToString(float[] array) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            buffer.append(array[i]).append(" ");
        }
        return buffer.toString();
    }

    public static double[] stringToDoubleArray(String str) {
        StringTokenizer st = new StringTokenizer(str);
        double[] array = new double[st.countTokens()];
        for (int i = 0; i < array.length; i++) {
            array[i] = doubleValue(st.nextToken());
        }
        return array;
    }

    public static String doubleArrayToString(double[] array) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            buffer.append(array[i]).append(" ");
        }
        return buffer.toString();
    }

    // Format Size to Byte
    public static String toByteSize(long s) {
        if (s < 1024) return s + " Bytes";
        else if (s < 1024 * 1024 * 1024) return Math.round(s / 1024) + " Kb";
        else if (s < 1024 * 1024 * 1024 * 1024) return Math.round(s / (1024 * 1024)) + " Mb";
        else return Math.round(s / (1024 * 1024 * 1024)) + " Gb";
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
