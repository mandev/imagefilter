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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.RecursiveAction;

/**
 * An abstract superclass for filters which distort images in some way. The
 * subclass only needs to override two methods to provide the mapping between
 * source and destination pixels.
 */
public abstract class TransformFilter extends AbstractBufferedImageOp {

    private static final Logger logger = LoggerFactory.getLogger(TransformFilter.class);
    //
    /**
     * Treat pixels off the edge as zero.
     */
    public final static int ZERO = 0;
    /**
     * Clamp pixels to the image edges.
     */
    public final static int CLAMP = 1;
    /**
     * Wrap pixels off the edge onto the oppsoite edge.
     */
    public final static int WRAP = 2;
    /**
     * Use nearest-neighbout interpolation.
     */
    public final static int NEAREST_NEIGHBOUR = 0;
    /**
     * Use bilinear interpolation.
     */
    public final static int BILINEAR = 1;
    /**
     * The action to take for pixels off the image edge.
     */
    protected int edgeAction = ZERO;
    /**
     * The type of interpolation to use.
     */
    protected int interpolation = BILINEAR;
    /**
     * The output image rectangle.
     */
    protected Rectangle transformedSpace;
    /**
     * The input image rectangle.
     */
    protected Rectangle originalSpace;

    /**
     * Set the action to perform for pixels off the edge of the image.
     *
     * @param edgeAction one of ZERO, CLAMP or WRAP
     * @see #getEdgeAction
     */
    public void setEdgeAction(int edgeAction) {
        this.edgeAction = edgeAction;
    }

    /**
     * Get the action to perform for pixels off the edge of the image.
     *
     * @return one of ZERO, CLAMP or WRAP
     * @see #setEdgeAction
     */
    public int getEdgeAction() {
        return edgeAction;
    }

    /**
     * Set the type of interpolation to perform.
     *
     * @param interpolation one of NEAREST_NEIGHBOUR or BILINEAR
     * @see #getInterpolation
     */
    public void setInterpolation(int interpolation) {
        this.interpolation = interpolation;
    }

    /**
     * Get the type of interpolation to perform.
     *
     * @return one of NEAREST_NEIGHBOUR or BILINEAR
     * @see #setInterpolation
     */
    public int getInterpolation() {
        return interpolation;
    }

    /**
     * Inverse transform a point. This method needs to be overriden by all
     * subclasses.
     *
     * @param x   the X position of the pixel in the output image
     * @param y   the Y position of the pixel in the output image
     * @param out the position of the pixel in the input image
     */
    protected abstract void transformInverse(int x, int y, float[] out);

    /**
     * Forward transform a rectangle. Used to determine the size of the output
     * image.
     *
     * @param rect the rectangle to transform
     */
    protected void transformSpace(Rectangle rect) {
    }

    @Override
    public boolean isCmykSupported() {
        return true;
    }

    @Override
    public boolean isGraySupported() {
        return true;
    }

    // GRAY8
    @Override
    public BufferedImage filterGRAY8(BufferedImage src, BufferedImage dst) {
        int width = src.getWidth();
        int height = src.getHeight();

        originalSpace = new Rectangle(0, 0, width, height);
        transformedSpace = new Rectangle(0, 0, width, height);
        transformSpace(transformedSpace);

        if (dst == null) {
            dst = createCompatibleDestImage(src, transformedSpace.width, transformedSpace.height);
        }

        int threshold = Math.max(ThreadUtils.THRESHOLD, (transformedSpace.width * transformedSpace.height) / (ThreadUtils.getAvailableProcessors() * 10));
        ThreadUtils.forkJoin(new TransformAction_GRAY8(0, transformedSpace.height, getGRAY(src), width, height, dst, threshold));
        return dst;
    }

    protected BufferedImage filterNearest_GRAY8(int start, int end, BufferedImage dst, int width, int height, byte[] inPixels) {
        int srcWidth = width;
        int srcHeight = height;
        int outWidth = transformedSpace.width;
        int outHeight = transformedSpace.height;
        int outX, outY, srcX, srcY;
        byte[] outPixels = new byte[outWidth];

        outX = transformedSpace.x;
        outY = transformedSpace.y;
        float[] out = new float[2];

        for (int y = start; y < end; y++) {
            for (int x = 0; x < outWidth; x++) {
                transformInverse(outX + x, outY + y, out);
                srcX = (int) out[0];
                srcY = (int) out[1];
                // int casting rounds towards zero, so we check out[0] < 0, not srcX < 0
                if (out[0] < 0 || srcX >= srcWidth || out[1] < 0 || srcY >= srcHeight) {
                    if (edgeAction == ZERO) {
                        outPixels[x] = 0;
                    } else if (edgeAction == WRAP) {
                        outPixels[x] = inPixels[(ImageMath.mod(srcY, srcHeight) * srcWidth) + ImageMath.mod(srcX, srcWidth)];
                    } else { // CLAMP
                        outPixels[x] = inPixels[(ImageMath.clamp(srcY, 0, srcHeight - 1) * srcWidth) + ImageMath.clamp(srcX, 0, srcWidth - 1)];
                    }
                } else {
                    outPixels[x] = inPixels[srcWidth * srcY + srcX];
                }
            }
            setGRAY(dst, 0, y, transformedSpace.width, 1, outPixels);
        }
        return dst;
    }

    protected BufferedImage filterBilinear_GRAY8(int start, int end, BufferedImage dst, int width, int height, byte[] inPixels) {
        int srcWidth = width;
        int srcHeight = height;
        int srcWidth1 = width - 1;
        int srcHeight1 = height - 1;
        int outWidth = transformedSpace.width;
        int outHeight = transformedSpace.height;
        int outX, outY;
        byte[] outPixels = new byte[outWidth];

        outX = transformedSpace.x;
        outY = transformedSpace.y;
        float[] out = new float[2];

        for (int y = start; y < end; y++) {
            for (int x = 0; x < outWidth; x++) {
                transformInverse(outX + x, outY + y, out);
                int srcX = (int) Math.floor(out[0]);
                int srcY = (int) Math.floor(out[1]);
                float xWeight = out[0] - srcX;
                float yWeight = out[1] - srcY;

                if (srcX >= 0 && srcX < srcWidth1 && srcY >= 0 && srcY < srcHeight1) {
                    // Easy case, all corners are in the image
                    int i = srcWidth * srcY + srcX;
                    outPixels[x] = ImageMath.bilinearInterpolateGray(
                            xWeight, yWeight,
                            inPixels[i], inPixels[i + 1],
                            inPixels[i + srcWidth], inPixels[i + srcWidth + 1]);
                } else {
                    // Some of the corners are off the image
                    outPixels[x] = ImageMath.bilinearInterpolateGray(xWeight, yWeight,
                            getPixel_GRAY8(inPixels, srcX, srcY, srcWidth, srcHeight),
                            getPixel_GRAY8(inPixels, srcX + 1, srcY, srcWidth, srcHeight),
                            getPixel_GRAY8(inPixels, srcX, srcY + 1, srcWidth, srcHeight),
                            getPixel_GRAY8(inPixels, srcX + 1, srcY + 1, srcWidth, srcHeight));
                }
            }
            setGRAY(dst, 0, y, transformedSpace.width, 1, outPixels);
        }

        return dst;
    }

    protected byte getPixel_GRAY8(byte[] pixels, int x, int y, int width, int height) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            if (edgeAction == ZERO) {
                return 0;
            } else if (edgeAction == WRAP) {
                return pixels[(ImageMath.mod(y, height) * width) + ImageMath.mod(x, width)];
            } else {
                return pixels[(ImageMath.clamp(y, 0, height - 1) * width) + ImageMath.clamp(x, 0, width - 1)];
            }
        }
        return pixels[y * width + x];
    }

    private class TransformAction_GRAY8 extends RecursiveAction {

        private final int threshold;
        private final int start;
        private final int end;
        private final byte[] srcPixels;
        private final BufferedImage dst;
        private final int width;
        private final int height;

        private TransformAction_GRAY8(int start, int end, byte[] srcPixels, int width, int height, BufferedImage dst, int threshold) {
            this.start = start;
            this.end = end;
            this.srcPixels = srcPixels;
            this.width = width;
            this.height = height;
            this.dst = dst;
            this.threshold = threshold;
        }

        @Override
        public void compute() {
            int t = (end - start) * dst.getWidth();
            if (t < threshold) {
                if (interpolation == NEAREST_NEIGHBOUR) {
                    filterNearest_GRAY8(start, end, dst, width, height, srcPixels);
                } else {
                    filterBilinear_GRAY8(start, end, dst, width, height, srcPixels);
                }
            } else {
                int split = (end - start) / 2;
                invokeAll(new TransformAction_GRAY8(start, start + split, srcPixels, width, height, dst, threshold),
                        new TransformAction_GRAY8(start + split, end, srcPixels, width, height, dst, threshold));
            }
        }
    }

    // RGB32
    @Override
    public BufferedImage filterRGB32(BufferedImage src, BufferedImage dst) {
        int width = src.getWidth();
        int height = src.getHeight();

        originalSpace = new Rectangle(0, 0, width, height);
        transformedSpace = new Rectangle(0, 0, width, height);
        transformSpace(transformedSpace);

        if (dst == null) {
            dst = createCompatibleDestImage(src, transformedSpace.width, transformedSpace.height);
        }
        int threshold = Math.max(ThreadUtils.THRESHOLD, (transformedSpace.width * transformedSpace.height) / (ThreadUtils.getAvailableProcessors() * 10));
        ThreadUtils.forkJoin(new TransformAction_RGB32(0, transformedSpace.height, getRGB(src), width, height, dst, threshold));
        return dst;
    }

    protected BufferedImage filterBilinear_RGB32(int start, int end, BufferedImage dst, int width, int height, int[] inPixels) {

        int srcWidth = width;
        int srcHeight = height;
        int srcWidth1 = width - 1;
        int srcHeight1 = height - 1;
        int outWidth = transformedSpace.width;
        int outHeight = transformedSpace.height;
        int outX, outY;
        int dstType = getImageType(dst);
        int[] outPixels = new int[outWidth];

        outX = transformedSpace.x;
        outY = transformedSpace.y;
        float[] out = new float[2];

        //for (int y = 0; y < outHeight; y++) {
        for (int y = start; y < end; y++) {
            for (int x = 0; x < outWidth; x++) {
                transformInverse(outX + x, outY + y, out);
                int srcX = (int) Math.floor(out[0]);
                int srcY = (int) Math.floor(out[1]);
                float xWeight = out[0] - srcX;
                float yWeight = out[1] - srcY;

                if (srcX >= 0 && srcX < srcWidth1 && srcY >= 0 && srcY < srcHeight1) {
                    // Easy case, all corners are in the image
                    int i = srcWidth * srcY + srcX;
                    outPixels[x] = ImageMath.bilinearInterpolate(
                            xWeight, yWeight,
                            inPixels[i], inPixels[i + 1],
                            inPixels[i + srcWidth], inPixels[i + srcWidth + 1]);
                } else {
                    // Some of the corners are off the image
                    outPixels[x] = ImageMath.bilinearInterpolate(xWeight, yWeight,
                            getPixel_RGB32(inPixels, srcX, srcY, srcWidth, srcHeight),
                            getPixel_RGB32(inPixels, srcX + 1, srcY, srcWidth, srcHeight),
                            getPixel_RGB32(inPixels, srcX, srcY + 1, srcWidth, srcHeight),
                            getPixel_RGB32(inPixels, srcX + 1, srcY + 1, srcWidth, srcHeight));
                }
            }
            setRGB0(dst, dstType, 0, y, transformedSpace.width, 1, outPixels);
        }

        return dst;
    }

    protected BufferedImage filterNearest_RGB32(int start, int end, BufferedImage dst, int width, int height, int[] inPixels) {
        int srcWidth = width;
        int srcHeight = height;
        int outWidth = transformedSpace.width;
        int outHeight = transformedSpace.height;
        int outX, outY, srcX, srcY;
        int dstType = getImageType(dst);
        int[] outPixels = new int[outWidth];

        outX = transformedSpace.x;
        outY = transformedSpace.y;
        float[] out = new float[2];

        //for (int y = 0; y < outHeight; y++) {
        for (int y = start; y < end; y++) {
            for (int x = 0; x < outWidth; x++) {
                transformInverse(outX + x, outY + y, out);
                srcX = (int) out[0];
                srcY = (int) out[1];
                // int casting rounds towards zero, so we check out[0] < 0, not srcX < 0
                if (out[0] < 0 || srcX >= srcWidth || out[1] < 0 || srcY >= srcHeight) {
                    if (edgeAction == ZERO) {
                        outPixels[x] = 0;
                    } else if (edgeAction == WRAP) {
                        outPixels[x] = inPixels[(ImageMath.mod(srcY, srcHeight) * srcWidth) + ImageMath.mod(srcX, srcWidth)];
                    } else { // CLAMP
                        outPixels[x] = inPixels[(ImageMath.clamp(srcY, 0, srcHeight - 1) * srcWidth) + ImageMath.clamp(srcX, 0, srcWidth - 1)];
                    }
                } else {
                    outPixels[x] = inPixels[srcWidth * srcY + srcX];
                }
            }
            setRGB0(dst, dstType, 0, y, transformedSpace.width, 1, outPixels);
        }
        return dst;
    }

    protected int getPixel_RGB32(int[] pixels, int x, int y, int width, int height) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            if (edgeAction == ZERO) {
                return 0;
            } else if (edgeAction == WRAP) {
                return pixels[(ImageMath.mod(y, height) * width) + ImageMath.mod(x, width)];
            } else { // CLAMP
                return pixels[(ImageMath.clamp(y, 0, height - 1) * width) + ImageMath.clamp(x, 0, width - 1)];
            }
        }
        return pixels[y * width + x];
    }

    private class TransformAction_RGB32 extends RecursiveAction {

        private final int threshold;
        private final int start;
        private final int end;
        private final int[] srcPixels;
        private final BufferedImage dst;
        private final int width;
        private final int height;

        private TransformAction_RGB32(int start, int end, int[] srcPixels, int width, int height, BufferedImage dst, int threshold) {
            this.start = start;
            this.end = end;
            this.srcPixels = srcPixels;
            this.width = width;
            this.height = height;
            this.dst = dst;
            this.threshold = threshold;
        }

        @Override
        public void compute() {
            int t = (end - start) * dst.getWidth();
            if (t < threshold) {
                if (interpolation == NEAREST_NEIGHBOUR) {
                    filterNearest_RGB32(start, end, dst, width, height, srcPixels);
                } else {
                    filterBilinear_RGB32(start, end, dst, width, height, srcPixels);
                }
            } else {
                int split = (end - start) / 2;
                invokeAll(new TransformAction_RGB32(start, start + split, srcPixels, width, height, dst, threshold),
                        new TransformAction_RGB32(start + split, end, srcPixels, width, height, dst, threshold));
            }
        }
    }

    // CMYK32
    @Override
    public BufferedImage filterCMYK32(BufferedImage src, BufferedImage dst) {
        return filterRGB32(src, dst);
    }
}
