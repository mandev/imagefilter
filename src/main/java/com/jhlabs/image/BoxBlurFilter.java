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

/**
 * A filter which performs a box blur on an image. The horizontal and vertical
 * blurs can be specified separately and a number of iterations can be given
 * which allows an approximation to Gaussian blur.
 */
public class BoxBlurFilter extends AbstractBufferedImageOp {

    private float hRadius;
    private float vRadius;
    private int iterations = 1;
    private boolean premultiplyAlpha = true;

    /**
     * Construct a default BoxBlurFilter.
     */
    public BoxBlurFilter() {
    }

    /**
     * Construct a BoxBlurFilter.
     *
     * @param hRadius the horizontal radius of blur
     * @param vRadius the vertical radius of blur
     * @param iterations the number of time to iterate the blur
     */
    public BoxBlurFilter(float hRadius, float vRadius, int iterations) {
        this.hRadius = hRadius;
        this.vRadius = vRadius;
        this.iterations = iterations;
    }

    /**
     * Set whether to premultiply the alpha channel.
     *
     * @param premultiplyAlpha true to premultiply the alpha
     * @see #getPremultiplyAlpha
     */
    public void setPremultiplyAlpha(boolean premultiplyAlpha) {
        this.premultiplyAlpha = premultiplyAlpha;
    }

    /**
     * Get whether to premultiply the alpha channel.
     *
     * @return true to premultiply the alpha
     * @see #setPremultiplyAlpha
     */
    public boolean getPremultiplyAlpha() {
        return premultiplyAlpha;
    }

    public BufferedImage filterRGB32(BufferedImage src, BufferedImage dst) {
        int width = src.getWidth();
        int height = src.getHeight();

        if (dst == null) {
            dst = createCompatibleDestImage(src);
        }

        int[] inPixels = new int[width * height];
        int[] outPixels = new int[width * height];

        getRGB(src, 0, 0, width, height, inPixels);

        boolean preMul = (premultiplyAlpha && src.getColorModel().hasAlpha() && !src.isAlphaPremultiplied());
        if (preMul) {
            ImageMath.premultiply(inPixels, 0, inPixels.length);
        }

        for (int i = 0; i < iterations; i++) {
            blur(inPixels, outPixels, width, height, hRadius);
            blur(outPixels, inPixels, height, width, vRadius);
        }

        if ((hRadius - (int) hRadius) > 0.001 || (vRadius - (int) vRadius) > 0.001) {
            blurFractional(inPixels, outPixels, width, height, hRadius);
            blurFractional(outPixels, inPixels, height, width, vRadius);
        }

        if (preMul) {
            ImageMath.unpremultiply(inPixels, 0, inPixels.length);
        }

        setRGB(dst, 0, 0, width, height, inPixels);
        return dst;
    }

    public static void blur(int[] inPixels, int[] outPixels, int width, int height, float radius) {

        int tn = ThreadUtils.getThreadNumber(width, height, 250);

        Thread[] threads = new Thread[tn];
        for (int start = 0, i = 0; i < tn; i++) {
            int end = Math.min(height, (int) (height * ((float) (i + 1) / (float) tn)) + 1);
            threads[i] = new FilterThread(start, end, inPixels, outPixels, width, height, radius, false);
            threads[i].start();
            start = end;
        }

        ThreadUtils.joinThreads(threads);
    }

    public static void blurFractional(int[] inPixels, int[] outPixels, int width, int height, float radius) {

        int tn = ThreadUtils.getThreadNumber(width, height, 250);

        Thread[] threads = new Thread[tn];
        for (int start = 0, i = 0; i < tn; i++) {
            int end = Math.min(height, (int) (height * ((float) (i + 1) / (float) tn)) + 1);
            threads[i] = new FilterThread(start, end, inPixels, outPixels, width, height, radius, true);
            threads[i].start();
            start = end;
        }

        ThreadUtils.joinThreads(threads);
    }

    private static class FilterThread extends Thread {

        private int start;
        private int end;
        private int[] inPixels;
        private int[] outPixels;
        private int width;
        private int height;
        private float radius;
        private boolean fractionnal;

        public FilterThread(int start, int end, int[] inPixels, int[] outPixels, int width, int height, float radius, boolean fractionnal) {

            this.start = start;
            this.end = end;
            this.inPixels = inPixels;
            this.outPixels = outPixels;
            this.width = width;
            this.height = height;
            this.radius = radius;
            this.fractionnal = fractionnal;
        }

        @Override
        public void run() {
            if (fractionnal) {
                BoxBlurFilter.blurFractional(start, end, inPixels, outPixels, width, height, radius);
            }
            else {
                BoxBlurFilter.blur(start, end, inPixels, outPixels, width, height, radius);
            }
        }
    }

    /**
     * Blur and transpose a block of ARGB pixels.
     *
     * @param in the input pixels
     * @param out the output pixels
     * @param width the width of the pixel array
     * @param height the height of the pixel array
     * @param radius the radius of blur
     */
    private static void blur(int start, int end, int[] in, int[] out, int width, int height, float radius) {
        int widthMinus1 = width - 1;
        int r = (int) radius;
        int tableSize = 2 * r + 1;
        int divide[] = new int[256 * tableSize];

        for (int i = 0; i < 256 * tableSize; i++) {
            divide[i] = i / tableSize;
        }

        int inIndex = start * width;

        for (int y = start; y < end; y++) {
            int outIndex = y;
            int ta = 0, tr = 0, tg = 0, tb = 0;

            for (int i = -r; i <= r; i++) {
                int rgb = in[inIndex + ImageMath.clamp(i, 0, widthMinus1)];
                ta += (rgb >> 24) & 0xff;
                tr += (rgb >> 16) & 0xff;
                tg += (rgb >> 8) & 0xff;
                tb += rgb & 0xff;
            }

            for (int x = 0; x < width; x++) {
                out[outIndex] = (divide[ta] << 24) | (divide[tr] << 16) | (divide[tg] << 8) | divide[tb];

                int i1 = x + r + 1;
                if (i1 > widthMinus1) {
                    i1 = widthMinus1;
                }
                int i2 = x - r;
                if (i2 < 0) {
                    i2 = 0;
                }
                int rgb1 = in[inIndex + i1];
                int rgb2 = in[inIndex + i2];

                ta += ((rgb1 >> 24) & 0xff) - ((rgb2 >> 24) & 0xff);
                tr += ((rgb1 & 0xff0000) - (rgb2 & 0xff0000)) >> 16;
                tg += ((rgb1 & 0xff00) - (rgb2 & 0xff00)) >> 8;
                tb += (rgb1 & 0xff) - (rgb2 & 0xff);
                outIndex += height;
            }
            inIndex += width;
        }
    }

    public static void blurFractional(int start, int end, int[] in, int[] out, int width, int height, float radius) {
        radius -= (int) radius;

        float f = 1.0f / (1 + 2 * radius);
        int inIndex = start * width;

        for (int y = start; y < end; y++) {
            int outIndex = y;
            out[outIndex] = in[inIndex];

            outIndex += height;
            for (int x = 1; x < width - 1; x++) {
                int i = inIndex + x;
                int rgb1 = in[i - 1];
                int rgb2 = in[i];
                int rgb3 = in[i + 1];

                int a1 = (rgb1 >> 24) & 0xff;
                int r1 = (rgb1 >> 16) & 0xff;
                int g1 = (rgb1 >> 8) & 0xff;
                int b1 = rgb1 & 0xff;

                int a2 = (rgb2 >> 24) & 0xff;
                int r2 = (rgb2 >> 16) & 0xff;
                int g2 = (rgb2 >> 8) & 0xff;
                int b2 = rgb2 & 0xff;

                int a3 = (rgb3 >> 24) & 0xff;
                int r3 = (rgb3 >> 16) & 0xff;
                int g3 = (rgb3 >> 8) & 0xff;
                int b3 = rgb3 & 0xff;

                a1 = a2 + (int) ((a1 + a3) * radius);
                r1 = r2 + (int) ((r1 + r3) * radius);
                g1 = g2 + (int) ((g1 + g3) * radius);
                b1 = b2 + (int) ((b1 + b3) * radius);

                a1 *= f;
                r1 *= f;
                g1 *= f;
                b1 *= f;

                out[outIndex] = (a1 << 24) | (r1 << 16) | (g1 << 8) | b1;
                outIndex += height;
            }
            out[outIndex] = in[width - 1];
            inIndex += width;
        }
    }

    /**
     * Set the horizontal size of the blur.
     *
     * @param hRadius the radius of the blur in the horizontal direction
     * @min-value 0
     * @see #getHRadius
     */
    public void setHRadius(float hRadius) {
        this.hRadius = hRadius;
    }

    /**
     * Get the horizontal size of the blur.
     *
     * @return the radius of the blur in the horizontal direction
     * @see #setHRadius
     */
    public float getHRadius() {
        return hRadius;
    }

    /**
     * Set the vertical size of the blur.
     *
     * @param vRadius the radius of the blur in the vertical direction
     * @min-value 0
     * @see #getVRadius
     */
    public void setVRadius(float vRadius) {
        this.vRadius = vRadius;
    }

    /**
     * Get the vertical size of the blur.
     *
     * @return the radius of the blur in the vertical direction
     * @see #setVRadius
     */
    public float getVRadius() {
        return vRadius;
    }

    /**
     * Set both the horizontal and vertical sizes of the blur.
     *
     * @param radius the radius of the blur in both directions
     * @min-value 0
     * @see #getRadius
     */
    public void setRadius(float radius) {
        this.hRadius = this.vRadius = radius;
    }

    /**
     * Get the size of the blur.
     *
     * @return the radius of the blur in the horizontal direction
     * @see #setRadius
     */
    public float getRadius() {
        return hRadius;
    }

    /**
     * Set the number of iterations the blur is performed.
     *
     * @param iterations the number of iterations
     * @min-value 0
     * @see #getIterations
     */
    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    /**
     * Get the number of iterations the blur is performed.
     *
     * @return the number of iterations
     * @see #setIterations
     */
    public int getIterations() {
        return iterations;
    }

    @Override
    public String toString() {
        return "Blur/Box Blur...";
    }
}
