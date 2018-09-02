/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jhlabs.filter;

import com.jhlabs.image.AbstractBufferedImageOp;
import static com.jhlabs.image.AbstractBufferedImageOp.getImageType;
import static com.jhlabs.image.AbstractBufferedImageOp.getRGB0;
import static com.jhlabs.image.AbstractBufferedImageOp.setRGB0;
import com.jhlabs.utils.ThreadUtils;
import java.awt.image.BufferedImage;
import static java.util.concurrent.ForkJoinTask.invokeAll;
import java.util.concurrent.RecursiveAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlipFilterFast extends AbstractBufferedImageOp {

    private static final Logger logger = LoggerFactory.getLogger(FlipFilterFast.class);
    //
    public static final int FLIP_H = 1;
    /**
     * Flip the image vertically.
     */
    public static final int FLIP_V = 2;
    /**
     * Flip the image horizontally and vertically.
     */
    public static final int FLIP_HV = 3;
    /**
     * Rotate the image 90 degrees clockwise.
     */
    public static final int FLIP_90CW = 4;
    /**
     * Rotate the image 90 degrees counter-clockwise.
     */
    public static final int FLIP_90CCW = 5;
    /**
     * Rotate the image 180 degrees.
     */
    public static final int FLIP_180 = 6;
    //
    private int operation;

    public FlipFilterFast() {
        this(FLIP_HV);
    }

    public FlipFilterFast(int operation) {
        this.operation = operation;
    }

    /**
     * Set the filter operation.
     *
     * @param operation the filter operation
     * @see #getOperation
     */
    public void setOperation(int operation) {
        this.operation = operation;
    }

    /**
     * Get the filter operation.
     *
     * @return the filter operation
     * @see #setOperation
     */
    public int getOperation() {
        return operation;
    }

    @Override
    public boolean isCmykSupported() {
        return true;
    }

    @Override
    public boolean isGraySupported() {
        return true;
    }

    @Override
    public BufferedImage filterGRAY8(BufferedImage src, BufferedImage dst) {
        switch (operation) {
            case FLIP_90CW:
            case FLIP_90CCW:
                if (dst == null) {
                    dst = createCompatibleDestImage(src, src.getHeight(), src.getWidth());
                }
                int threshold = Math.max(ThreadUtils.THRESHOLD, (src.getWidth() * src.getHeight()) / (ThreadUtils.getAvailableProcessors() * 10));
                int end = (operation == FLIP_90CW) ? src.getHeight() : src.getWidth();
                ThreadUtils.forkJoin(new FlipAction_GRAY8(0, end, src, dst, threshold));
                return dst;

            case FLIP_180:
                if (dst == null) {
                    dst = createCompatibleDestImage(src);
                }
                return getFlip180_GRAY8(src, dst);

            case FLIP_H:
                if (dst == null) {
                    dst = createCompatibleDestImage(src);
                }
                return getFlipH_GRAY8(src, dst);

            case FLIP_V:
                if (dst == null) {
                    dst = createCompatibleDestImage(src);
                }
                return getFlipV_GRAY8(src, dst);

            case FLIP_HV:
                if (dst == null) {
                    dst = createCompatibleDestImage(src);
                }
                return getFlip180_GRAY8(src, dst);
        }
        return dst;
    }

    @Override
    public BufferedImage filterCMYK32(BufferedImage src, BufferedImage dst) {
        return filterRGB32(src, dst);
    }

    @Override
    public BufferedImage filterRGB32(BufferedImage src, BufferedImage dst) {

        switch (operation) {
            case FLIP_90CW:
            case FLIP_90CCW:
                if (dst == null) {
                    dst = createCompatibleDestImage(src, src.getHeight(), src.getWidth());
                }
                int threshold = Math.max(ThreadUtils.THRESHOLD, (src.getWidth() * src.getHeight()) / (ThreadUtils.getAvailableProcessors() * 10));
                int end = (operation == FLIP_90CW) ? src.getHeight() : src.getWidth();
                ThreadUtils.forkJoin(new FlipAction_RGB32(0, end, src, dst, threshold));
                return dst;

            case FLIP_180:
                if (dst == null) {
                    dst = createCompatibleDestImage(src);
                }
                return getFlip180_RGB32(src, dst);

            case FLIP_H:
                if (dst == null) {
                    dst = createCompatibleDestImage(src);
                }
                return getFlipH_RGB32(src, dst);

            case FLIP_V:
                if (dst == null) {
                    dst = createCompatibleDestImage(src);
                }
                return getFlipV_RGB32(src, dst);

            case FLIP_HV:
                if (dst == null) {
                    dst = createCompatibleDestImage(src);
                }
                return getFlip180_RGB32(src, dst);
        }
        return dst;
    }

    // GRAY8
    public static BufferedImage getFlip180_GRAY8(BufferedImage src, BufferedImage dst) {
        int width = src.getWidth();
        int height = src.getHeight();

        byte[] pixels1 = new byte[width];
        byte[] pixels2 = new byte[width];
        byte[] pixels3 = new byte[width];
        byte[] pixels4 = new byte[width];

        for (int y = 0; y < (height + 1) / 2; y++) {
            getGRAY(src, 0, y, width, 1, pixels1);
            getGRAY(src, 0, height - y - 1, width, 1, pixels2);
            for (int x = 0; x < width; x++) {
                pixels3[width - x - 1] = pixels1[x];
                pixels4[width - x - 1] = pixels2[x];
            }
            setGRAY(dst, 0, y, width, 1, pixels4);
            setGRAY(dst, 0, height - y - 1, width, 1, pixels3);
        }
        return dst;
    }

    public static BufferedImage getFlipH_GRAY8(BufferedImage src, BufferedImage dst) {
        int width = src.getWidth();
        int height = src.getHeight();
        byte[] pixels1 = new byte[width];
        byte[] pixels2 = new byte[width];

        for (int y = 0; y < height; y++) {
            getGRAY(src, 0, y, width, 1, pixels1);
            for (int x = 0; x < width; x++) {
                pixels2[width - x - 1] = pixels1[x];
            }
            setGRAY(dst, 0, y, width, 1, pixels2);
        }
        return dst;
    }

    public static BufferedImage getFlipV_GRAY8(BufferedImage src, BufferedImage dst) {
        int width = src.getWidth();
        int height = src.getHeight();

        byte[] pixels1 = new byte[width];
        byte[] pixels2 = new byte[width];

        for (int y = 0; y < (height + 1) / 2; y++) {
            getGRAY(src, 0, y, width, 1, pixels1);
            getGRAY(src, 0, height - y - 1, width, 1, pixels2);
            setGRAY(dst, 0, y, width, 1, pixels2);
            setGRAY(dst, 0, height - y - 1, width, 1, pixels1);
        }
        return dst;
    }

    public static BufferedImage getFlip90CW_GRAY8(int start, int end, BufferedImage src, BufferedImage dst) {
        int width = src.getWidth();
        int height = src.getHeight();
        byte[] pixels = new byte[width];

        for (int y = start; y < end; y++) {
            getGRAY(src, 0, y, width, 1, pixels);
            setGRAY(dst, height - y - 1, 0, 1, width, pixels);
        }
        return dst;
    }

    public static BufferedImage getFlip90CCW_GRAY8(int start, int end, BufferedImage src, BufferedImage dst) {
        int width = src.getWidth();
        int height = src.getHeight();
        byte[] pixels = new byte[height];

        for (int x = start; x < end; x++) {
            getGRAY(src, x, 0, 1, height, pixels);
            setGRAY(dst, 0, width - x - 1, height, 1, pixels);
        }
        return dst;
    }

    private class FlipAction_GRAY8 extends RecursiveAction {

        private int threshold;
        private int start;
        private int end;
        private BufferedImage src;
        private BufferedImage dst;

        public FlipAction_GRAY8(int start, int end, BufferedImage src, BufferedImage dst, int threshold) {
            this.start = start;
            this.end = end;
            this.src = src;
            this.dst = dst;
            this.threshold = threshold;
        }

        @Override
        public void compute() {
            int t = (end - start) * ((operation == FLIP_90CW) ? src.getWidth() : src.getHeight());
            if (t < threshold) {
                if (operation == FLIP_90CW) {
                    getFlip90CW_GRAY8(start, end, src, dst);
                }
                else if (operation == FLIP_90CCW) {
                    getFlip90CCW_GRAY8(start, end, src, dst);
                }
            }
            else {
                int split = (end - start) / 2;
                invokeAll(new FlipAction_GRAY8(start, start + split, src, dst, threshold),
                        new FlipAction_GRAY8(start + split, end, src, dst, threshold));
            }
        }
    }

    // RGB32
    public static BufferedImage getFlip180_RGB32(BufferedImage src, BufferedImage dst) {
        int width = src.getWidth();
        int height = src.getHeight();
        int srcType = getImageType(src);
        int dstType = getImageType(src);

        int[] pixels1 = new int[width];
        int[] pixels2 = new int[width];
        int[] pixels3 = new int[width];
        int[] pixels4 = new int[width];

        for (int y = 0; y < (height + 1) / 2; y++) {
            getRGB0(src, srcType, 0, y, width, 1, pixels1);
            getRGB0(src, srcType, 0, height - y - 1, width, 1, pixels2);
            for (int x = 0; x < width; x++) {
                pixels3[width - x - 1] = pixels1[x];
                pixels4[width - x - 1] = pixels2[x];
            }
            setRGB0(dst, dstType, 0, y, width, 1, pixels4);
            setRGB0(dst, dstType, 0, height - y - 1, width, 1, pixels3);
        }
        return dst;
    }

    public static BufferedImage getFlipV_RGB32(BufferedImage src, BufferedImage dst) {
        int width = src.getWidth();
        int height = src.getHeight();
        int srcType = getImageType(src);
        int dstType = getImageType(src);

        int[] pixels1 = new int[width];
        int[] pixels2 = new int[width];

        for (int y = 0; y < (height + 1) / 2; y++) {
            getRGB0(src, srcType, 0, y, width, 1, pixels1);
            getRGB0(src, srcType, 0, height - y - 1, width, 1, pixels2);
            setRGB0(dst, dstType, 0, y, width, 1, pixels2);
            setRGB0(dst, dstType, 0, height - y - 1, width, 1, pixels1);
        }
        return dst;
    }

    public static BufferedImage getFlipH_RGB32(BufferedImage src, BufferedImage dst) {

        int width = src.getWidth();
        int height = src.getHeight();
        int srcType = getImageType(src);
        int dstType = getImageType(src);

        int[] pixels1 = new int[width];
        int[] pixels2 = new int[width];

        for (int y = 0; y < height; y++) {
            getRGB0(src, srcType, 0, y, width, 1, pixels1);
            for (int x = 0; x < width; x++) {
                pixels2[width - x - 1] = pixels1[x];
            }
            setRGB0(dst, dstType, 0, y, width, 1, pixels2);
        }
        return dst;
    }

    public static BufferedImage getFlip90CW_RGB32(int start, int end, BufferedImage src, BufferedImage dst) {
        int width = src.getWidth();
        int height = src.getHeight();
        int srcType = getImageType(src);
        int dstType = getImageType(src);

        int[] pixels = new int[width];

        for (int y = start; y < end; y++) {
            getRGB0(src, srcType, 0, y, width, 1, pixels);
            setRGB0(dst, dstType, height - y - 1, 0, 1, width, pixels);
        }
        return dst;
    }

    public static BufferedImage getFlip90CCW_RGB32(int start, int end, BufferedImage src, BufferedImage dst) {
        int width = src.getWidth();
        int height = src.getHeight();
        int srcType = getImageType(src);
        int dstType = getImageType(src);

        int[] pixels = new int[height];

        for (int x = start; x < end; x++) {
            getRGB0(src, srcType, x, 0, 1, height, pixels);
            setRGB0(dst, dstType, 0, width - x - 1, height, 1, pixels);
        }
        return dst;
    }

    private class FlipAction_RGB32 extends RecursiveAction {

        private int threshold;
        private int start;
        private int end;
        private BufferedImage src;
        private BufferedImage dst;

        public FlipAction_RGB32(int start, int end, BufferedImage src, BufferedImage dst, int threshold) {
            this.start = start;
            this.end = end;
            this.src = src;
            this.dst = dst;
            this.threshold = threshold;
        }

        @Override
        public void compute() {
            int t = (end - start) * ((operation == FLIP_90CW) ? src.getWidth() : src.getHeight());
            if (t < threshold) {
                if (operation == FLIP_90CW) {
                    getFlip90CW_RGB32(start, end, src, dst);
                }
                else if (operation == FLIP_90CCW) {
                    getFlip90CCW_RGB32(start, end, src, dst);
                }
            }
            else {
                int split = (end - start) / 2;
                invokeAll(new FlipAction_RGB32(start, start + split, src, dst, threshold),
                        new FlipAction_RGB32(start + split, end, src, dst, threshold));
            }
        }
    }

    @Override
    public String toString() {
        return "Distort/Flip";
    }
}
