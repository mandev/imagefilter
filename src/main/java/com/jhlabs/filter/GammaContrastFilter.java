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
package com.jhlabs.filter;

import com.jhlabs.image.PixelUtils;
import com.jhlabs.image.TransferFilter;

/**
 * A filter for changing the gamma of an image.
 */
public class GammaContrastFilter extends TransferFilter {

    private float rGamma, gGamma, bGamma;
    private float brightness = 1.0f;
    private float contrast = 1.0f;

    /**
     * Construct a GammaFilter.
     */
    public GammaContrastFilter() {
        this(1.0f);
    }

    /**
     * Construct a GammaFilter.
     *
     * @param gamma the gamma level for all RGB channels
     */
    public GammaContrastFilter(float gamma) {
        this(gamma, gamma, gamma);
    }

    /**
     * Construct a GammaFilter.
     *
     * @param rGamma the gamma level for the red channel
     * @param gGamma the gamma level for the blue channel
     * @param bGamma the gamma level for the green channel
     */
    public GammaContrastFilter(float rGamma, float gGamma, float bGamma) {
        setGamma(rGamma, gGamma, bGamma);
    }

    /**
     * Set the filter brightness.
     *
     * @param brightness the brightness in the range 0 to 1
     * @min-value 0
     * @max-value 0
     * @see #getBrightness
     */
    public void setBrightness(float brightness) {
        this.brightness = brightness;
        initialize();
    }

    /**
     * Get the filter brightness.
     *
     * @return the brightness in the range 0 to 1
     * @see #setBrightness
     */
    public float getBrightness() {
        return brightness;
    }

    /**
     * Set the filter contrast.
     *
     * @param contrast the contrast in the range 0 to 1
     * @min-value 0
     * @max-value 0
     * @see #getContrast
     */
    public void setContrast(float contrast) {
        this.contrast = contrast;
        initialize();
    }

    /**
     * Get the filter contrast.
     *
     * @return the contrast in the range 0 to 1
     * @see #setContrast
     */
    public float getContrast() {
        return contrast;
    }

    /**
     * Set the gamma levels.
     *
     * @param rGamma the gamma level for the red channel
     * @param gGamma the gamma level for the blue channel
     * @param bGamma the gamma level for the green channel
     * @see #getGamma
     */
    public void setGamma(float rGamma, float gGamma, float bGamma) {
        this.rGamma = rGamma;
        this.gGamma = gGamma;
        this.bGamma = bGamma;
        initialize();
    }

    /**
     * Set the gamma level.
     *
     * @param gamma the gamma level for all RGB channels
     * @see #getGamma
     */
    public void setGamma(float gamma) {
        setGamma(gamma, gamma, gamma);
    }

    /**
     * Get the gamma level.
     *
     * @return the gamma level for all RGB channels
     * @see #setGamma
     */
    public float getGamma() {
        return rGamma;
    }

    @Override
    protected void initialize() {
        rTable = makeTable(rGamma);
        gTable = (gGamma == rGamma) ? rTable : makeTable(gGamma);
        bTable = (bGamma == rGamma) ? rTable : makeTable(bGamma);
    }

    private int[] makeTable(float gamma) {
        int[] table = new int[256];
        for (int i = 0; i < 256; i++) {
            int v = PixelUtils.clamp((int) (255f * ((i / 255f * brightness - 0.5f) * contrast + 0.5f)));  // Contrast
            v = (int) ((255f * Math.pow(v / 255f, 1f / gamma)) + 0.5f); // Gamma
            if (v > 255) {
                v = 255;
            }
            table[i] = v;
        }
        return table;
    }

    @Override
    public String toString() {
        return "Colors/GammaContrast...";
    }

    @Override
    protected float transferFunction(float v) {
        return 0;
    }
}
