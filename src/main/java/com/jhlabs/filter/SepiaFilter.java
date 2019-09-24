/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jhlabs.filter;

import com.jhlabs.image.PointFilter;

/**
 * A filter which converts an image to grayscale using the NTSC brightness
 * calculation.
 */
public class SepiaFilter extends PointFilter {

    private int amount;

    public SepiaFilter() {
        this(30);
    }

    public SepiaFilter(int amount) {
        canFilterIndexColorModel = true;
        setAmount(amount);
    }

    public void setAmount(int a) {
        amount = a;
    }

    public int getAmoount() {
        return amount;
    }

    @Override
    public int filterRGB(int x, int y, int rgb) {
        int a = rgb & 0xff000000;
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;

        int average = (r + g + b) / 3;
        int red = average + amount;
        int green = average;
        int blue = average - amount;

        if (red > 255) {
            red = 255;
        }
        if (blue < 0) {
            blue = 0;
        }
        return a | (red << 16) | (green << 8) | blue;
    }

    @Override
    public String toString() {
        return "Colors/Sepia";
    }
}
