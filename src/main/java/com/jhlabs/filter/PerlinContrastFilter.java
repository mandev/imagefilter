/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jhlabs.filter;

import com.jhlabs.image.PointFilter;

/**
 * A filter which tries to remove the red eye effect.
 */
public class PerlinContrastFilter extends PointFilter {

    private final double LOG_POINTFIVE = -0.6931471805599453d;
    //
    private float contrast;
    private final int[] lookUp = new int[256];

    public PerlinContrastFilter() {
        this(0.5f);
    }

    public PerlinContrastFilter(float contrast) {
        setContrast(contrast);
    }

    public void setContrast(float contrast) {
        this.contrast = contrast;
        initialize();
    }

    public float getContrast() {
        return contrast;
    }

    private void initialize() {
        double p = Math.log(1f - contrast) / LOG_POINTFIVE;

        for (int i = 0; i < lookUp.length; i++) {
            float gainValue = gain(i / 255f, p);
            int v = Math.round(255f * gainValue);
            if (v > 255) {
                v = 255;
            }
            lookUp[i] = v;
        }
    }

    private float gain(float a, double p) {
        return (a < 0.5f) ? (float) (Math.pow(2 * a, p) / 2d) : (float) (1 - Math.pow(2 * (1. - a), p) / 2d);
    }

    @Override
    public int filterRGB(int x, int y, int rgb) {
        int a = rgb & 0xff000000;
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;

        return a | (lookUp[r] << 16) | (lookUp[g] << 8) | lookUp[b];
    }

    @Override
    public String toString() {
        return "Colors/PerlinContrast";
    }
}
