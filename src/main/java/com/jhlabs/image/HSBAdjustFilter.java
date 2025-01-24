/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.jhlabs.image;

import java.awt.*;

public class HSBAdjustFilter extends PointFilter {

    public float hFactor, sFactor, bFactor;

    public HSBAdjustFilter() {
        this(0, 0, 0);
    }

    public HSBAdjustFilter(float r, float g, float b) {
        hFactor = r;
        sFactor = g;
        bFactor = b;
        canFilterIndexColorModel = true;
    }

    public void setHFactor(float hFactor) {
        this.hFactor = hFactor;
    }

    public float getHFactor() {
        return hFactor;
    }

    public void setSFactor(float sFactor) {
        this.sFactor = sFactor;
    }

    public float getSFactor() {
        return sFactor;
    }

    public void setBFactor(float bFactor) {
        this.bFactor = bFactor;
    }

    public float getBFactor() {
        return bFactor;
    }

    public int filterRGB(int x, int y, int rgb) {
        int a = rgb & 0xff000000;
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;

        float[] hsb = Color.RGBtoHSB(r, g, b, null);
        float hue = hsb[0];
        float sat = hsb[1];
        float bri = hsb[2];

        hue += hFactor;
        while (hue < 0) {
            hue += Math.PI * 2;
        }

        sat += sFactor;
        if (sat < 0) {
            sat = 0;
        } else if (sat > 1.0) {
            sat = 1.0f;
        }

        bri += bFactor;
        if (bri < 0) {
            bri = 0;
        } else if (bri > 1.0) {
            bri = 1.0f;
        }

        rgb = Color.HSBtoRGB(hue, sat, bri);
        return a | (rgb & 0xffffff);
    }

    @Override
    public String toString() {
        return "Colors/Adjust HSB...";
    }
}
