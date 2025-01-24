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

import com.jhlabs.utils.ThreadUtils;

import java.awt.image.BufferedImage;
import java.util.concurrent.RecursiveAction;

/**
 * A filter which subtracts Gaussian blur from an image, sharpening it.
 *
 * @author Jerry Huxtable
 */
public class UnsharpFilter extends GaussianFilter {

    private float amount = 0.5f;
    private int threshold = 1;

    public UnsharpFilter() {
        radius = 2;
    }

    /**
     * Set the threshold value.
     *
     * @param threshold the threshold value
     * @see #getThreshold
     */
    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    /**
     * Get the threshold value.
     *
     * @return the threshold value
     * @see #setThreshold
     */
    public int getThreshold() {
        return threshold;
    }

    /**
     * Set the amount of sharpening.
     *
     * @param amount the amount
     * @min-value 0
     * @max-value 1
     * @see #getAmount
     */
    public void setAmount(float amount) {
        this.amount = amount;
    }

    /**
     * Get the amount of sharpening.
     *
     * @return the amount
     * @see #setAmount
     */
    public float getAmount() {
        return amount;
    }

    @Override
    public BufferedImage filterRGB32(BufferedImage src, BufferedImage dst) {
        int width = src.getWidth();
        int height = src.getHeight();

        if (dst == null) {
            dst = createCompatibleDestImage(src);
        }

        int[] inPixels = new int[width * height];
        int[] outPixels = new int[width * height];

        getRGB(src, 0, 0, width, height, inPixels);

        if (radius > 0) {
            boolean pre = (premultiplyAlpha && src.getColorModel().hasAlpha() && !src.isAlphaPremultiplied());
            convolve(kernel, inPixels, outPixels, width, height, alpha, pre, false, CLAMP_EDGES);
        }

        long tt = System.nanoTime();
        int tresh = Math.max(ThreadUtils.THRESHOLD, (width * height) / (ThreadUtils.getAvailableProcessors() * 10));
        ThreadUtils.forkJoin(new UnsharpAction(0, height, inPixels, outPixels, width, height, tresh));
        //unsharp(0, height, inPixels, outPixels, width, height) ;
        System.err.println("time3: " + (System.nanoTime() - tt) / 1000000);

        setRGB(dst, 0, 0, width, height, outPixels);

        return dst;
    }

    private void unsharp(int start, int end, int[] inPixels, int[] outPixels, int width, int height) {
        float a = 4 * amount;
        int index = start * width;
        for (int y = start; y < end; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = inPixels[index];
                int r1 = (rgb1 >> 16) & 0xff;
                int g1 = (rgb1 >> 8) & 0xff;
                int b1 = rgb1 & 0xff;

                int rgb2 = outPixels[index];
                int r2 = (rgb2 >> 16) & 0xff;
                int g2 = (rgb2 >> 8) & 0xff;
                int b2 = rgb2 & 0xff;

                if (Math.abs(r1 - r2) >= threshold) {
                    r1 = PixelUtils.clamp((int) ((a + 1) * (r1 - r2) + r2));
                }
                if (Math.abs(g1 - g2) >= threshold) {
                    g1 = PixelUtils.clamp((int) ((a + 1) * (g1 - g2) + g2));
                }
                if (Math.abs(b1 - b2) >= threshold) {
                    b1 = PixelUtils.clamp((int) ((a + 1) * (b1 - b2) + b2));
                }

                outPixels[index] = (rgb1 & 0xff000000) | (r1 << 16) | (g1 << 8) | b1;
                index++;
            }
        }

    }

    private class UnsharpAction extends RecursiveAction {

        private final int thresh;
        private final int start;
        private final int end;
        private final int[] inPixels;
        private final int[] outPixels;
        private final int width;
        private final int height;

        private UnsharpAction(int start, int end, int[] inPixels, int[] outPixels, int width, int height, int thresh) {
            this.start = start;
            this.end = end;
            this.inPixels = inPixels;
            this.outPixels = outPixels;
            this.width = width;
            this.height = height;
            this.thresh = thresh;
        }

        @Override
        public void compute() {
            int t = (end - start) * width;
            if (t < thresh) {
                unsharp(start, end, inPixels, outPixels, width, height);
            } else {
                int split = (end - start) / 2;
                invokeAll(new UnsharpAction(start, start + split, inPixels, outPixels, width, height, thresh),
                        new UnsharpAction(start + split, end, inPixels, outPixels, width, height, thresh));
            }
        }
    }

    @Override
    public String toString() {
        return "Blur/Unsharp Mask...";
    }
}
