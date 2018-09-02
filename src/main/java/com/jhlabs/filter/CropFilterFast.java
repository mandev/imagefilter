
package com.jhlabs.filter;

import com.jhlabs.image.AbstractBufferedImageOp;
import java.awt.image.BufferedImage;

/**
 * A filter which crops an image to a given rectangle.
 */
public class CropFilterFast extends AbstractBufferedImageOp {
    private int x;
    private int y;
    private int width;
    private int height;

    /**
     * Construct a CropFilter.
     */
    public CropFilterFast() {
        this(0, 0, 32, 32);
    }

    /**
     * Construct a CropFilter.
     * @param x the left edge of the crop rectangle
     * @param y the top edge of the crop rectangle
     * @param width the width of the crop rectangle
     * @param height the height of the crop rectangle
     */
    public CropFilterFast(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Set the left edge of the crop rectangle.
     * @param x the left edge of the crop rectangle
     * @see #getX
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Get the left edge of the crop rectangle.
     * @return the left edge of the crop rectangle
     * @see #setX
     */
    public int getX() {
        return x;
    }

    /**
     * Set the top edge of the crop rectangle.
     * @param y the top edge of the crop rectangle
     * @see #getY
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Get the top edge of the crop rectangle.
     * @return the top edge of the crop rectangle
     * @see #setY
     */
    public int getY() {
        return y;
    }

    /**
     * Set the width of the crop rectangle.
     * @param width the width of the crop rectangle
     * @see #getWidth
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Get the width of the crop rectangle.
     * @return the width of the crop rectangle
     * @see #setWidth
     */
    public int getWidth() {
        return width;
    }

    /**
     * Set the height of the crop rectangle.
     * @param height the height of the crop rectangle
     * @see #getHeight
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Get the height of the crop rectangle.
     * @return the height of the crop rectangle
     * @see #setHeight
     */
    public int getHeight() {
        return height;
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
        if (x >= src.getWidth()) x = 0;
        if (y >= src.getHeight()) y = 0;
        int w = Math.min(width, src.getWidth() - x);
        int h = Math.min(height, src.getHeight() - y);
        if (w <= 0) w = src.getWidth() - x;
        if (h <= 0) h = src.getHeight() - y;

        if (dst == null) {
            dst = createCompatibleDestImage(src, w, h);
        }

        byte[] pixels = new byte[w];
        for (int i = 0; i < h; i++) {
            getGRAY(src, x, y + i, w, 1, pixels);
            setGRAY(dst, 0, i, w, 1, pixels);
        }
        return dst;
    }

    @Override
    public BufferedImage filterCMYK32(BufferedImage src, BufferedImage dst) {
        return filterRGB32(src, dst);
    }

    @Override
    public BufferedImage filterRGB32(BufferedImage src, BufferedImage dst) {
        if (x >= src.getWidth()) x = 0;
        if (y >= src.getHeight()) y = 0;
        int w = Math.min(width, src.getWidth() - x);
        int h = Math.min(height, src.getHeight() - y);
        if (w <= 0) w = src.getWidth() - x;
        if (h <= 0) h = src.getHeight() - y;

        int rgbType = getImageType(src);

        if (dst == null) {
            dst = createCompatibleDestImage(src, w, h);
        }

        int[] pixels = new int[w];
        for (int i = 0; i < h; i++) {
            getRGB0(src, rgbType, x, y + i, w, 1, pixels);
            setRGB0(dst, rgbType, 0, i, w, 1, pixels);
        }
        return dst;
    }

    @Override
    public String toString() {
        return "Distort/Crop";
    }
}
