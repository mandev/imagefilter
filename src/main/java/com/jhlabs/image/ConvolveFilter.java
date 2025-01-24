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
 * A filter which applies a convolution kernel to an image.
 *
 * @author Jerry Huxtable
 */
public class ConvolveFilter extends AbstractBufferedImageOp {

    /**
     * Treat pixels off the edge as zero.
     */
    public static int ZERO_EDGES = 0;
    /**
     * Clamp pixels off the edge to the nearest edge.
     */
    public static int CLAMP_EDGES = 1;
    /**
     * Wrap pixels off the edge to the opposite edge.
     */
    public static int WRAP_EDGES = 2;
    /**
     * The convolution kernel.
     */
    protected Kernel kernel = null;
    /**
     * Whether to convolve alpha.
     */
    protected boolean alpha = true;
    /**
     * Whether to promultiply the alpha before convolving.
     */
    protected boolean premultiplyAlpha = true;
    /**
     * What do do at the image edges.
     */
    private int edgeAction = CLAMP_EDGES;
    private int iteration = 1;

    /**
     * Construct a filter with a null kernel. This is only useful if you're
     * going to change the kernel later on.
     */
    public ConvolveFilter() {
        this(new float[9]);
    }

    /**
     * Construct a filter with the given 3x3 kernel.
     *
     * @param matrix an array of 9 floats containing the kernel
     */
    public ConvolveFilter(float[] matrix) {
        this(new Kernel(3, 3, matrix));
    }

    /**
     * Construct a filter with the given kernel.
     *
     * @param rows   the number of rows in the kernel
     * @param cols   the number of columns in the kernel
     * @param matrix an array of rows*cols floats containing the kernel
     */
    public ConvolveFilter(int rows, int cols, float[] matrix) {
        this(new Kernel(cols, rows, matrix));
    }

    /**
     * Construct a filter with the given 3x3 kernel.
     *
     * @param kernel the convolution kernel
     */
    public ConvolveFilter(Kernel kernel) {
        this.kernel = kernel;
    }

    /**
     * Set the convolution kernel.
     *
     * @param kernel the kernel
     * @see #getKernel
     */
    public void setKernel(Kernel kernel) {
        this.kernel = kernel;
    }

    /**
     * Get the convolution kernel.
     *
     * @return the kernel
     * @see #setKernel
     */
    public Kernel getKernel() {
        return kernel;
    }

    /**
     * Set the action to perfomr for pixels off the image edges.
     *
     * @param edgeAction the action
     * @see #getEdgeAction
     */
    public void setEdgeAction(int edgeAction) {
        this.edgeAction = edgeAction;
    }

    /**
     * Get the action to perfomr for pixels off the image edges.
     *
     * @return the action
     * @see #setEdgeAction
     */
    public int getEdgeAction() {
        return edgeAction;
    }

    /**
     * @return the iteration
     */
    public int getIteration() {
        return iteration;
    }

    /**
     * @param iteration the iteration to set
     */
    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    /**
     * Set whether to convolve the alpha channel.
     *
     * @param useAlpha true to convolve the alpha
     * @see #getUseAlpha
     */
    public void setUseAlpha(boolean useAlpha) {
        this.alpha = useAlpha;
    }

    /**
     * Get whether to convolve the alpha channel.
     *
     * @return true to convolve the alpha
     * @see #setUseAlpha
     */
    public boolean getUseAlpha() {
        return alpha;
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

        boolean preMul = (premultiplyAlpha && src.getColorModel().hasAlpha() && !src.isAlphaPremultiplied());
        if (preMul) {
            ImageMath.premultiply(inPixels, 0, inPixels.length);
        }

        for (int i = 0; i < iteration; i++) {
            convolve(kernel, inPixels, outPixels, width, height, alpha, edgeAction);
            inPixels = outPixels;
        }

        if (preMul) {
            ImageMath.unpremultiply(inPixels, 0, inPixels.length);
        }

        setRGB(dst, 0, 0, width, height, inPixels);
        return dst;
    }

    public void convolve(Kernel kernel, int[] inPixels, int[] outPixels, int width, int height, boolean alpha, int edgeAction) {
        int threshold = Math.max(ThreadUtils.THRESHOLD, (width * height) / (ThreadUtils.getAvailableProcessors() * 10));
        ThreadUtils.forkJoin(new ConvolveAction(0, height, kernel, inPixels, outPixels, width, height, alpha, edgeAction, threshold));
    }

    private class ConvolveAction extends RecursiveAction {

        private final int threshold;
        private final int start;
        private final int end;
        private final Kernel kernel;
        private final int[] inPixels;
        private final int[] outPixels;
        private final int width;
        private final int height;
        private final boolean alpha;
        private final int edgeAction;

        public ConvolveAction(int start, int end, Kernel kernel, int[] inPixels, int[] outPixels, int width, int height, boolean alpha, int edgeAction, int threshold) {
            this.start = start;
            this.end = end;
            this.kernel = kernel;
            this.inPixels = inPixels;
            this.outPixels = outPixels;
            this.width = width;
            this.height = height;
            this.alpha = alpha;
            this.edgeAction = edgeAction;
            this.threshold = threshold;
        }

        @Override
        public void compute() {
            int t = (end - start) * width;
            if (t < threshold) {
                ConvolveFilter.convolve(start, end, kernel, inPixels, outPixels, width, height, alpha, edgeAction);
            } else {
                int split = (end - start) / 2;
                invokeAll(new ConvolveAction(start, start + split, kernel, inPixels, outPixels, width, height, alpha, edgeAction, threshold),
                        new ConvolveAction(start + split, end, kernel, inPixels, outPixels, width, height, alpha, edgeAction, threshold));
            }

        }
    }

    /**
     * Convolve a block of pixels.
     *
     * @param start
     * @param end
     * @param kernel     the kernel
     * @param inPixels   the input pixels
     * @param outPixels  the output pixels
     * @param width      the width
     * @param height     the height
     * @param alpha      include alpha channel
     * @param edgeAction what to do at the edges
     */
    public static void convolve(int start, int end, Kernel kernel, int[] inPixels, int[] outPixels, int width, int height, boolean alpha, int edgeAction) {
        if (kernel.getHeight() == 1) {
            convolveH(start, end, kernel, inPixels, outPixels, width, height, alpha, edgeAction);
        } else if (kernel.getWidth() == 1) {
            convolveV(start, end, kernel, inPixels, outPixels, width, height, alpha, edgeAction);
        } else {
            convolveHV(start, end, kernel, inPixels, outPixels, width, height, alpha, edgeAction);
        }
    }

    /**
     * Convolve with a 2D kernel.
     *
     * @param start
     * @param end
     * @param kernel     the kernel
     * @param inPixels   the input pixels
     * @param outPixels  the output pixels
     * @param width      the width
     * @param height     the height
     * @param alpha      include alpha channel
     * @param edgeAction what to do at the edges
     */
    public static void convolveHV(int start, int end, Kernel kernel, int[] inPixels, int[] outPixels, int width, int height, boolean alpha, int edgeAction) {
        int index = start * width;
        float[] matrix = kernel.getKernelData(null);
        int rows = kernel.getHeight();
        int cols = kernel.getWidth();
        int rows2 = rows / 2;
        int cols2 = cols / 2;

        for (int y = start; y < end; y++) {
            for (int x = 0; x < width; x++) {
                float r = 0, g = 0, b = 0, a = 0;

                for (int row = -rows2; row <= rows2; row++) {
                    int iy = y + row;
                    int ioffset;
                    if (0 <= iy && iy < height) {
                        ioffset = iy * width;
                    } else if (edgeAction == CLAMP_EDGES) {
                        ioffset = y * width;
                    } else if (edgeAction == WRAP_EDGES) {
                        ioffset = ((iy + height) % height) * width;
                    } else {
                        continue;
                    }

                    int moffset = cols * (row + rows2) + cols2;
                    for (int col = -cols2; col <= cols2; col++) {
                        float f = matrix[moffset + col];

                        if (f != 0) {
                            int ix = x + col;
                            if (!(0 <= ix && ix < width)) {
                                if (edgeAction == CLAMP_EDGES) {
                                    ix = x;
                                } else if (edgeAction == WRAP_EDGES) {
                                    ix = (x + width) % width;
                                } else {
                                    continue;
                                }
                            }
                            int rgb = inPixels[ioffset + ix];
                            a += f * ((rgb >> 24) & 0xff);
                            r += f * ((rgb >> 16) & 0xff);
                            g += f * ((rgb >> 8) & 0xff);
                            b += f * (rgb & 0xff);
                        }
                    }
                }
                int ia = alpha ? PixelUtils.clamp((int) (a + 0.5)) : 0xff;
                int ir = PixelUtils.clamp((int) (r + 0.5));
                int ig = PixelUtils.clamp((int) (g + 0.5));
                int ib = PixelUtils.clamp((int) (b + 0.5));
                outPixels[index++] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
            }
        }
    }

    /**
     * Convolve with a kernel consisting of one row.
     *
     * @param start
     * @param end
     * @param kernel     the kernel
     * @param inPixels   the input pixels
     * @param outPixels  the output pixels
     * @param width      the width
     * @param height     the height
     * @param alpha      include alpha channel
     * @param edgeAction what to do at the edges
     */
    public static void convolveH(int start, int end, Kernel kernel, int[] inPixels, int[] outPixels, int width, int height, boolean alpha, int edgeAction) {
        int index = start * width;
        float[] matrix = kernel.getKernelData(null);
        int cols = kernel.getWidth();
        int cols2 = cols / 2;

        for (int y = start; y < end; y++) {
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
                            } else if (edgeAction == WRAP_EDGES) {
                                ix = (x + width) % width;
                            }
                        } else if (ix >= width) {
                            if (edgeAction == CLAMP_EDGES) {
                                ix = width - 1;
                            } else if (edgeAction == WRAP_EDGES) {
                                ix = (x + width) % width;
                            }
                        }
                        int rgb = inPixels[ioffset + ix];
                        a += f * ((rgb >> 24) & 0xff);
                        r += f * ((rgb >> 16) & 0xff);
                        g += f * ((rgb >> 8) & 0xff);
                        b += f * (rgb & 0xff);
                    }
                }
                int ia = alpha ? PixelUtils.clamp((int) (a + 0.5)) : 0xff;
                int ir = PixelUtils.clamp((int) (r + 0.5));
                int ig = PixelUtils.clamp((int) (g + 0.5));
                int ib = PixelUtils.clamp((int) (b + 0.5));
                outPixels[index++] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
            }
        }
    }

    /**
     * Convolve with a kernel consisting of one column.
     *
     * @param start
     * @param end
     * @param kernel     the kernel
     * @param inPixels   the input pixels
     * @param outPixels  the output pixels
     * @param width      the width
     * @param height     the height
     * @param alpha      include alpha channel
     * @param edgeAction what to do at the edges
     */
    public static void convolveV(int start, int end, Kernel kernel, int[] inPixels, int[] outPixels, int width, int height, boolean alpha, int edgeAction) {
        int index = start * width;
        float[] matrix = kernel.getKernelData(null);
        int rows = kernel.getHeight();
        int rows2 = rows / 2;

        for (int y = start; y < end; y++) {
            for (int x = 0; x < width; x++) {
                float r = 0, g = 0, b = 0, a = 0;

                for (int row = -rows2; row <= rows2; row++) {
                    int iy = y + row;
                    int ioffset;
                    if (iy < 0) {
                        if (edgeAction == CLAMP_EDGES) {
                            ioffset = 0;
                        } else if (edgeAction == WRAP_EDGES) {
                            ioffset = ((y + height) % height) * width;
                        } else {
                            ioffset = iy * width;
                        }
                    } else if (iy >= height) {
                        if (edgeAction == CLAMP_EDGES) {
                            ioffset = (height - 1) * width;
                        } else if (edgeAction == WRAP_EDGES) {
                            ioffset = ((y + height) % height) * width;
                        } else {
                            ioffset = iy * width;
                        }
                    } else {
                        ioffset = iy * width;
                    }

                    float f = matrix[row + rows2];

                    if (f != 0) {
                        int rgb = inPixels[ioffset + x];
                        a += f * ((rgb >> 24) & 0xff);
                        r += f * ((rgb >> 16) & 0xff);
                        g += f * ((rgb >> 8) & 0xff);
                        b += f * (rgb & 0xff);
                    }
                }
                int ia = alpha ? PixelUtils.clamp((int) (a + 0.5)) : 0xff;
                int ir = PixelUtils.clamp((int) (r + 0.5));
                int ig = PixelUtils.clamp((int) (g + 0.5));
                int ib = PixelUtils.clamp((int) (b + 0.5));
                outPixels[index++] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
            }
        }
    }

    @Override
    public String toString() {
        return "Blur/Convolve...";
    }
}
