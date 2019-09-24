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
import java.awt.image.Kernel;
import java.util.concurrent.RecursiveAction;

/**
 * A filter which applies Gaussian blur to an image. This is a subclass of
 * ConvolveFilter which simply creates a kernel with a Gaussian distribution for
 * blurring.
 *
 * @author Jerry Huxtable
 */
public class GaussianFilter extends ConvolveFilter {

    /**
     * The blur radius.
     */
    protected float radius;

    /**
     * Construct a Gaussian filter.
     */
    public GaussianFilter() {
        this(2);
    }

    /**
     * Construct a Gaussian filter.
     *
     * @param radius blur radius in pixels
     */
    public GaussianFilter(float radius) {
        setRadius(radius);
    }

    /**
     * Set the radius of the kernel, and hence the amount of blur. The bigger
     * the radius, the longer this filter will take.
     *
     * @param radius the radius of the blur in pixels.
     * @min-value 0
     * @max-value 100+
     * @see #getRadius
     */
    public void setRadius(float radius) {
        this.radius = radius;
        kernel = makeKernel(radius);
    }

    /**
     * Get the radius of the kernel.
     *
     * @return the radius
     * @see #setRadius
     */
    public float getRadius() {
        return radius;
    }

    @Override
    public BufferedImage filterRGB32(BufferedImage src, BufferedImage dst) {
        if (dst == null) {
            dst = createCompatibleDestImage(src);
        }

        int width = src.getWidth();
        int height = src.getHeight();

        int[] inPixels = new int[width * height];
        int[] outPixels = new int[width * height];
        getRGB(src, 0, 0, width, height, inPixels);

        if (radius > 0) {
            boolean preMul = (premultiplyAlpha && src.getColorModel().hasAlpha() && !src.isAlphaPremultiplied());
            convolve(kernel, inPixels, outPixels, width, height, alpha, preMul, false, CLAMP_EDGES);
        }
        setRGB(dst, 0, 0, width, height, outPixels);

        return dst;
    }

    public static void convolve(Kernel kernel, int[] inPixels, int[] outPixels, int width, int height,
            boolean alpha, boolean premultiply, boolean unpremultiply, int edgeAction) {

        int[] tmpPixels = new int[inPixels.length];
        int threshold = Math.max(ThreadUtils.THRESHOLD, (width * height) / (ThreadUtils.getAvailableProcessors() * 10));
        ThreadUtils.forkJoin(new GaussianAction(0, height, kernel, inPixels, tmpPixels, width, height, alpha, premultiply, unpremultiply, edgeAction, threshold));
        ThreadUtils.forkJoin(new GaussianAction(0, width, kernel, tmpPixels, outPixels, height, width, alpha, unpremultiply, premultiply, edgeAction, threshold));
    }

    private static class GaussianAction extends RecursiveAction {

        private int threshold;
        private int start;
        private int end;
        private Kernel kernel;
        private int[] inPixels;
        private int[] outPixels;
        private int width;
        private int height;
        private boolean alpha;
        private boolean premultiply;
        private boolean unpremultiply;
        private int edgeAction;

        private GaussianAction(int start, int end, Kernel kernel, int[] inPixels, int[] outPixels, int width, int height,
                boolean alpha, boolean premultiply, boolean unpremultiply, int edgeAction, int threshold) {
            this.start = start;
            this.end = end;
            this.kernel = kernel;
            this.inPixels = inPixels;
            this.outPixels = outPixels;
            this.width = width;
            this.height = height;
            this.alpha = alpha;
            this.premultiply = premultiply;
            this.unpremultiply = unpremultiply;
            this.edgeAction = edgeAction;
            this.threshold = threshold;
        }

        @Override
        public void compute() {
            int t = (end - start) * width;
            if (t < threshold) {
                GaussianFilter.convolveAndTranspose(start, end, kernel, inPixels, outPixels, width, height, alpha, premultiply, unpremultiply, edgeAction);
            }
            else {
                int split = (end - start) / 2;
                invokeAll(new GaussianAction(start, start + split, kernel, inPixels, outPixels, width, height, alpha, premultiply, unpremultiply, edgeAction, threshold),
                        new GaussianAction(start + split, end, kernel, inPixels, outPixels, width, height, alpha, premultiply, unpremultiply, edgeAction, threshold));
            }
        }
    }

    /**
     * Blur and transpose a block of ARGB pixels.
     *
     * @param kernel the blur kernel
     * @param inPixels the input pixels
     * @param outPixels the output pixels
     * @param width the width of the pixel array
     * @param height the height of the pixel array
     * @param alpha whether to blur the alpha channel
     * @param edgeAction what to do at the edges
     */
    public static void convolveAndTranspose(int start, int end, Kernel kernel, int[] inPixels, int[] outPixels, int width, int height,
            boolean alpha, boolean premultiply, boolean unpremultiply, int edgeAction) {
        //long time = System.currentTimeMillis();

        if (alpha && !premultiply && !unpremultiply & edgeAction == WRAP_EDGES) {
            convolveAndTransposeTFFW(start, end, kernel, inPixels, outPixels, width, height);
        }
        else if (alpha && premultiply && !unpremultiply & edgeAction == WRAP_EDGES) {
            convolveAndTransposeTTFW(start, end, kernel, inPixels, outPixels, width, height);
        }
        else if (alpha && !premultiply && unpremultiply & edgeAction == WRAP_EDGES) {
            convolveAndTransposeTFTW(start, end, kernel, inPixels, outPixels, width, height);
        }
        else if (!alpha && !premultiply && !unpremultiply & edgeAction == WRAP_EDGES) {
            convolveAndTransposeFFFW(start, end, kernel, inPixels, outPixels, width, height);
        }
        else if (alpha && !premultiply && !unpremultiply & edgeAction == CLAMP_EDGES) {
            convolveAndTransposeTFFC(start, end, kernel, inPixels, outPixels, width, height);
        }
        else if (alpha && premultiply && !unpremultiply & edgeAction == CLAMP_EDGES) {
            convolveAndTransposeTTFC(start, end, kernel, inPixels, outPixels, width, height);
        }
        else if (alpha && !premultiply && unpremultiply & edgeAction == CLAMP_EDGES) {
            convolveAndTransposeTFTC(start, end, kernel, inPixels, outPixels, width, height);
        }
        else if (!alpha && !premultiply && !unpremultiply & edgeAction == CLAMP_EDGES) {
            convolveAndTransposeFFFC(start, end, kernel, inPixels, outPixels, width, height);
        }
        else {
            System.err.println("convolveAndTranspose() - ");
            convolveAndTranspose2(start, end, kernel, inPixels, outPixels, width, height, alpha, premultiply, unpremultiply, edgeAction);
        }

        //System.err.println("convolveAndTranspose - time: " + (System.currentTimeMillis() - time));
    }

    private static void convolveAndTranspose2(int start, int end, Kernel kernel, int[] inPixels, int[] outPixels, int width, int height,
            boolean alpha, boolean premultiply, boolean unpremultiply, int edgeAction) {
        float[] matrix = kernel.getKernelData(null);
        int cols = kernel.getWidth();
        int cols2 = cols / 2;

        for (int y = start; y < end; y++) {
            int index = y;
            int ioffset = y * width;
            for (int x = 0; x < width; x++) {
                float r = 0, g = 0, b = 0, a = 0;
                int moffset = cols2;
                for (int col = -cols2; col <= cols2; col++) {
                    float f = matrix[moffset + col];

                    if (f != 0) {
                        int ix = x + col;
                        if (ix < 0) {
                            if (edgeAction == CLAMP_EDGES) {
                                ix = 0;
                            }
                            else if (edgeAction == WRAP_EDGES) {
                                ix = (x + width) % width;
                            }
                        }
                        else if (ix >= width) {
                            if (edgeAction == CLAMP_EDGES) {
                                ix = width - 1;
                            }
                            else if (edgeAction == WRAP_EDGES) {
                                ix = (x + width) % width;
                            }
                        }
                        int rgb = inPixels[ioffset + ix];
                        int pa = (rgb >> 24) & 0xff;
                        int pr = (rgb >> 16) & 0xff;
                        int pg = (rgb >> 8) & 0xff;
                        int pb = rgb & 0xff;
                        if (premultiply) {
                            float a255 = pa * (1.0f / 255.0f);
                            pr *= a255;
                            pg *= a255;
                            pb *= a255;
                        }
                        a += f * pa;
                        r += f * pr;
                        g += f * pg;
                        b += f * pb;
                    }
                }
                if (unpremultiply && a != 0 && a != 255) {
                    float f = 255.0f / a;
                    r *= f;
                    g *= f;
                    b *= f;
                }
                int ia = alpha ? PixelUtils.clamp((int) (a + 0.5)) : 0xff;
                int ir = PixelUtils.clamp((int) (r + 0.5));
                int ig = PixelUtils.clamp((int) (g + 0.5));
                int ib = PixelUtils.clamp((int) (b + 0.5));
                outPixels[index] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
                index += height;
            }
        }
    }

    private static void convolveAndTransposeFFFC(int start, int end, Kernel kernel, int[] inPixels, int[] outPixels, int width, int height) {

        float[] matrix = kernel.getKernelData(null);
        int col2 = kernel.getWidth() / 2;

        for (int y = start; y < end; y++) {
            int index = y;
            int ioffset = y * width;
            for (int x = 0; x < width; x++) {
                float r = 0, g = 0, b = 0, a = 0;
                for (int col = -col2; col <= col2; col++) {
                    float f = matrix[col2 + col];
                    if (f != 0) {
                        int ix = x + col;
                        if (ix < 0) {
                            ix = 0;
                        }
                        else if (ix >= width) {
                            ix = width - 1;
                        }

                        int rgb = inPixels[ioffset + ix];
                        a += f * ((rgb >> 24) & 0xff);
                        r += f * ((rgb >> 16) & 0xff);
                        g += f * ((rgb >> 8) & 0xff);
                        b += f * (rgb & 0xff);
                    }
                }
                // Clamp
                int ia = 0xff;
                int ir = (r < 0) ? 0 : (r > 254.5f) ? 255 : (int) (r + .5f);
                int ig = (g < 0) ? 0 : (g > 254.5f) ? 255 : (int) (g + .5f);
                int ib = (b < 0) ? 0 : (b > 254.5f) ? 255 : (int) (b + .5f);

                outPixels[index] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
                index += height;
            }
        }
    }

    private static void convolveAndTransposeTFTC(int start, int end, Kernel kernel, int[] inPixels, int[] outPixels, int width, int height) {

        float[] matrix = kernel.getKernelData(null);
        int cols = kernel.getWidth();
        int cols2 = cols / 2;

        for (int y = start; y < end; y++) {
            int index = y;
            int ioffset = y * width;
            for (int x = 0; x < width; x++) {
                float r = 0, g = 0, b = 0, a = 0;
                for (int col = -cols2; col <= cols2; col++) {
                    float f = matrix[cols2 + col];
                    if (f != 0) {
                        int ix = x + col;
                        if (ix < 0) {
                            ix = 0;
                        }
                        else if (ix >= width) {
                            ix = width - 1;
                        }
                        int rgb = inPixels[ioffset + ix];
                        a += f * ((rgb >> 24) & 0xff);
                        r += f * ((rgb >> 16) & 0xff);
                        g += f * ((rgb >> 8) & 0xff);
                        b += f * (rgb & 0xff);
                    }
                }
                if (a != 0 && a != 255) {
                    float f = 255.0f / a;
                    r *= f;
                    g *= f;
                    b *= f;
                }

                // Clamp
                int ia = (a < 0) ? 0 : (a > 254.5f) ? 255 : (int) (a + .5f);
                int ir = (r < 0) ? 0 : (r > 254.5f) ? 255 : (int) (r + .5f);
                int ig = (g < 0) ? 0 : (g > 254.5f) ? 255 : (int) (g + .5f);
                int ib = (b < 0) ? 0 : (b > 254.5f) ? 255 : (int) (b + .5f);

                outPixels[index] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
                index += height;
            }
        }
    }

    private static void convolveAndTransposeTTFC(int start, int end, Kernel kernel, int[] inPixels, int[] outPixels, int width, int height) {

        float[] matrix = kernel.getKernelData(null);
        int cols = kernel.getWidth();
        int cols2 = cols / 2;
        float NORM = 1.0f / 255.0f;

        for (int y = start; y < end; y++) {
            int index = y;
            int ioffset = y * width;
            for (int x = 0; x < width; x++) {
                float r = 0, g = 0, b = 0, a = 0;
                for (int col = -cols2; col <= cols2; col++) {
                    float f = matrix[cols2 + col];
                    if (f != 0) {
                        int ix = x + col;
                        if (ix < 0) {
                            ix = 0;
                        }
                        else if (ix >= width) {
                            ix = width - 1;
                        }
                        int rgb = inPixels[ioffset + ix];
                        int pa = (rgb >> 24) & 0xff;
                        float a255 = pa * NORM;
                        a += f * pa;
                        r += f * ((rgb >> 16) & 0xff) * a255;
                        g += f * ((rgb >> 8) & 0xff) * a255;
                        b += f * (rgb & 0xff) * a255;
                    }
                }

                // Clamp
                int ia = (a < 0) ? 0 : (a > 254.5f) ? 255 : (int) (a + .5f);
                int ir = (r < 0) ? 0 : (r > 254.5f) ? 255 : (int) (r + .5f);
                int ig = (g < 0) ? 0 : (g > 254.5f) ? 255 : (int) (g + .5f);
                int ib = (b < 0) ? 0 : (b > 254.5f) ? 255 : (int) (b + .5f);

                outPixels[index] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
                index += height;
            }
        }
    }

    private static void convolveAndTransposeTFFC(int start, int end, Kernel kernel, int[] inPixels, int[] outPixels, int width, int height) {

        float[] matrix = kernel.getKernelData(null);
        int cols = kernel.getWidth();
        int cols2 = cols / 2;

        for (int y = start; y < end; y++) {
            int index = y;
            int ioffset = y * width;
            for (int x = 0; x < width; x++) {
                float r = 0, g = 0, b = 0, a = 0;
                for (int col = -cols2; col <= cols2; col++) {
                    float f = matrix[cols2 + col];
                    if (f != 0) {
                        int ix = x + col;
                        if (ix < 0) {
                            ix = 0;
                        }
                        else if (ix >= width) {
                            ix = width - 1;
                        }
                        int rgb = inPixels[ioffset + ix];
                        a += f * ((rgb >> 24) & 0xff);
                        r += f * ((rgb >> 16) & 0xff);
                        g += f * ((rgb >> 8) & 0xff);
                        b += f * (rgb & 0xff);
                    }
                }

                // Clamp
                int ia = (a < 0) ? 0 : (a > 254.5f) ? 255 : (int) (a + .5f);
                int ir = (r < 0) ? 0 : (r > 254.5f) ? 255 : (int) (r + .5f);
                int ig = (g < 0) ? 0 : (g > 254.5f) ? 255 : (int) (g + .5f);
                int ib = (b < 0) ? 0 : (b > 254.5f) ? 255 : (int) (b + .5f);

                outPixels[index] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
                index += height;
            }
        }
    }

    private static void convolveAndTransposeFFFW(int start, int end, Kernel kernel, int[] inPixels, int[] outPixels, int width, int height) {

        float[] matrix = kernel.getKernelData(null);
        int cols = kernel.getWidth();
        int cols2 = cols / 2;

        for (int y = start; y < end; y++) {
            int index = y;
            int ioffset = y * width;
            for (int x = 0; x < width; x++) {
                float r = 0, g = 0, b = 0, a = 0;
                for (int col = -cols2; col <= cols2; col++) {
                    float f = matrix[cols2 + col];
                    if (f != 0) {
                        int ix = x + col;
                        if (ix < 0 || ix >= width) {
                            ix = (x + width) % width;
                        }
                        int rgb = inPixels[ioffset + ix];
                        a += f * ((rgb >> 24) & 0xff);
                        r += f * ((rgb >> 16) & 0xff);
                        g += f * ((rgb >> 8) & 0xff);
                        b += f * (rgb & 0xff);
                    }
                }
                int ia = 0xff;
                int ir = (r < 0) ? 0 : (r > 254.5f) ? 255 : (int) (r + .5f);
                int ig = (g < 0) ? 0 : (g > 254.5f) ? 255 : (int) (g + .5f);
                int ib = (b < 0) ? 0 : (b > 254.5f) ? 255 : (int) (b + .5f);

                outPixels[index] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
                index += height;
            }
        }
    }

    private static void convolveAndTransposeTFTW(int start, int end, Kernel kernel, int[] inPixels, int[] outPixels, int width, int height) {

        float[] matrix = kernel.getKernelData(null);
        int cols = kernel.getWidth();
        int cols2 = cols / 2;

        for (int y = start; y < end; y++) {
            int index = y;
            int ioffset = y * width;
            for (int x = 0; x < width; x++) {
                float r = 0, g = 0, b = 0, a = 0;
                for (int col = -cols2; col <= cols2; col++) {
                    float f = matrix[cols2 + col];
                    if (f != 0) {
                        int ix = x + col;
                        if (ix < 0 || ix >= width) {
                            ix = (x + width) % width;
                        }
                        int rgb = inPixels[ioffset + ix];
                        a += f * ((rgb >> 24) & 0xff);
                        r += f * ((rgb >> 16) & 0xff);
                        g += f * ((rgb >> 8) & 0xff);
                        b += f * (rgb & 0xff);
                    }
                }
                if (a != 0 && a != 255) {
                    float f = 255.0f / a;
                    r *= f;
                    g *= f;
                    b *= f;
                }

                int ia = PixelUtils.clamp((int) (a + 0.5));
                int ir = PixelUtils.clamp((int) (r + 0.5));
                int ig = PixelUtils.clamp((int) (g + 0.5));
                int ib = PixelUtils.clamp((int) (b + 0.5));
                outPixels[index] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
                index += height;
            }
        }
    }

    private static void convolveAndTransposeTTFW(int start, int end, Kernel kernel, int[] inPixels, int[] outPixels, int width, int height) {

        float[] matrix = kernel.getKernelData(null);
        int cols = kernel.getWidth();
        int cols2 = cols / 2;
        float NORM = 1.0f / 255.0f;

        for (int y = start; y < end; y++) {
            int index = y;
            int ioffset = y * width;
            for (int x = 0; x < width; x++) {
                float r = 0, g = 0, b = 0, a = 0;
                for (int col = -cols2; col <= cols2; col++) {
                    float f = matrix[cols2 + col];
                    if (f != 0) {
                        int ix = x + col;
                        if (ix < 0 || ix >= width) {
                            ix = (x + width) % width;
                        }
                        int rgb = inPixels[ioffset + ix];
                        int pa = (rgb >> 24) & 0xff;
                        int pr = (rgb >> 16) & 0xff;
                        int pg = (rgb >> 8) & 0xff;
                        int pb = rgb & 0xff;
                        float a255 = pa * NORM;
                        a += f * pa;
                        r += f * pr * a255;
                        g += f * pg * a255;
                        b += f * pb * a255;
                    }
                }
                int ia = PixelUtils.clamp((int) (a + 0.5));
                int ir = PixelUtils.clamp((int) (r + 0.5));
                int ig = PixelUtils.clamp((int) (g + 0.5));
                int ib = PixelUtils.clamp((int) (b + 0.5));
                outPixels[index] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
                index += height;
            }
        }
    }

    private static void convolveAndTransposeTFFW(int start, int end, Kernel kernel, int[] inPixels, int[] outPixels, int width, int height) {

        float[] matrix = kernel.getKernelData(null);
        int cols = kernel.getWidth();
        int cols2 = cols / 2;

        for (int y = start; y < end; y++) {
            int index = y;
            int ioffset = y * width;
            for (int x = 0; x < width; x++) {
                float r = 0, g = 0, b = 0, a = 0;

                for (int col = -cols2; col <= cols2; col++) {
                    float f = matrix[cols2 + col];
                    if (f != 0) {
                        int ix = x + col;
                        if (ix < 0 || ix >= width) {
                            ix = (x + width) % width;
                        }

                        int rgb = inPixels[ioffset + ix];
                        a += f * ((rgb >> 24) & 0xff);
                        r += f * ((rgb >> 16) & 0xff);
                        g += f * ((rgb >> 8) & 0xff);
                        b += f * (rgb & 0xff);
                    }
                }

                // Clamp
                int ia = (a < 0) ? 0 : (a > 254.5f) ? 255 : (int) (a + .5f);
                int ir = (r < 0) ? 0 : (r > 254.5f) ? 255 : (int) (r + .5f);
                int ig = (g < 0) ? 0 : (g > 254.5f) ? 255 : (int) (g + .5f);
                int ib = (b < 0) ? 0 : (b > 254.5f) ? 255 : (int) (b + .5f);

                outPixels[index] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
                index += height;
            }
        }

    }

    /**
     * Make a Gaussian blur kernel.
     *
     * @param radius the blur radius
     * @return the kernel
     */
    public static Kernel makeKernel(float radius) {
        int r = (int) Math.ceil(radius);
        int rows = r * 2 + 1;
        float[] matrix = new float[rows];
        float sigma = radius / 3;
        float sigma22 = 2 * sigma * sigma;
        float sigmaPi2 = 2 * ImageMath.PI * sigma;
        float sqrtSigmaPi2 = (float) Math.sqrt(sigmaPi2);
        float radius2 = radius * radius;
        float total = 0;
        int index = 0;
        for (int row = -r; row <= r; row++) {
            float distance = row * row;
            matrix[index] = (distance > radius2) ? 0 : (float) Math.exp(-(distance) / sigma22) / sqrtSigmaPi2;
            total += matrix[index];
            index++;
        }

        for (int i = 0; i < rows; i++) {
            matrix[i] /= total;
        }
        return new Kernel(rows, 1, matrix);
    }

    @Override
    public String toString() {
        return "Blur/Gaussian Blur...";
    }
}
