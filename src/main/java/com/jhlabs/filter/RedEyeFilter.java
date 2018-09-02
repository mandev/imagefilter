/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jhlabs.filter;

import com.jhlabs.image.PointFilter;

/**
 * A filter which tries to remove the red eye effect.
 */
public class RedEyeFilter extends PointFilter {

    private int amount;

    public RedEyeFilter() {
        this(20);
    }

    public RedEyeFilter(int amount) {
        canFilterIndexColorModel = true;
        this.amount = amount;
    }

    public void setAmount(int a) {
        amount = a;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public int filterRGB(int x, int y, int rgb) {
        int a = rgb & 0xff000000;
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;


        if (r > g * 3 || r > b * 3) {
            r = (g + b) / 2;
        }
        else if (r > g * 2 && r > b * 2) {
            r = (g + b) / 2;
            g = g * 3 / 2;
            b = b * 3 / 2;
        }

        return a | (r << 16) | (g << 8) | b;
    }

    @Override
    public String toString() {
        return "Colors/RedEye";
    }
}


