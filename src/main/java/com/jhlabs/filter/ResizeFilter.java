/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jhlabs.filter;

import com.adlitteram.jasmin.image.icc.IccUtils;
import com.jhlabs.image.AbstractBufferedImageOp;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

public class ResizeFilter extends AbstractBufferedImageOp {

    public static final int NEIGHBOR = 0;
    public static final int BILINEAR = 1;
    public static final int BICUBIC = 2;
    public static final int MULTISTEP = 3;

    private static final Object[] RENDERING_HINTS = {
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR,
            RenderingHints.VALUE_INTERPOLATION_BICUBIC,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR
    };
    private final int type;
    private final int dstWidth;
    private final int dstHeight;

    /**
     * Construct a ResizeFilter.
     */
    public ResizeFilter() {
        this(32, 32, BILINEAR);
    }

    /**
     * Construct a ResizeFilter.
     *
     * @param width  the width to scale to
     * @param height the height to scale to
     * @param type
     */
    public ResizeFilter(int width, int height, int type) {
        this.dstWidth = width;
        this.dstHeight = height;
        this.type = type;
    }

    @Override
    public boolean isGraySupported() {
        return true;
    }

    @Override
    public BufferedImage filterGRAY8(BufferedImage src, BufferedImage dst) {
        if (IccUtils.isCS_sGRAY(src)) {
            if (dst == null || IccUtils.isCS_sGRAY(dst)) {
                dst = scaleImage(src, dst);
            } else {
                ColorSpace cs = dst.getColorModel().getColorSpace();
                dst = IccUtils.applyColorSpace(dst, IccUtils.CS_GRAY_COLORSPACE);
                dst = scaleImage(src, dst);
                dst = IccUtils.applyColorSpace(dst, cs);
            }
        } else {
            ColorSpace cs1 = src.getColorModel().getColorSpace();
            src = IccUtils.applyColorSpace(src, IccUtils.CS_GRAY_COLORSPACE);

            if (dst == null) {
                dst = scaleImage(src, dst);
                dst = IccUtils.applyColorSpace(dst, cs1);
            } else if (IccUtils.isCS_sGRAY(dst)) {
                dst = scaleImage(src, dst);
            } else {
                ColorSpace cs2 = dst.getColorModel().getColorSpace();
                dst = IccUtils.applyColorSpace(dst, IccUtils.CS_GRAY_COLORSPACE);
                dst = scaleImage(src, dst);
                dst = IccUtils.applyColorSpace(dst, cs2);
            }

            src = IccUtils.applyColorSpace(src, cs1);
        }

        return dst;
    }

    @Override
    public BufferedImage filterRGB32(BufferedImage src, BufferedImage dst) {

        if (IccUtils.isCS_sRGB(src)) {
            if (dst == null || IccUtils.isCS_sRGB(dst)) {
                dst = scaleImage(src, dst);
            } else {
                ColorSpace cs = dst.getColorModel().getColorSpace();
                dst = IccUtils.applyColorSpace(dst, IccUtils.CS_sRGB_COLORSPACE);
                dst = scaleImage(src, dst);
                dst = IccUtils.applyColorSpace(dst, cs);
            }
        } else {
            ColorSpace cs1 = src.getColorModel().getColorSpace();
            src = IccUtils.applyColorSpace(src, IccUtils.CS_sRGB_COLORSPACE);

            if (dst == null) {
                dst = scaleImage(src, dst);
                dst = IccUtils.applyColorSpace(dst, cs1);
            } else if (IccUtils.isCS_sRGB(dst)) {
                dst = scaleImage(src, dst);
            } else {
                ColorSpace cs2 = dst.getColorModel().getColorSpace();
                dst = IccUtils.applyColorSpace(dst, IccUtils.CS_sRGB_COLORSPACE);
                dst = scaleImage(src, dst);
                dst = IccUtils.applyColorSpace(dst, cs2);
            }

            src = IccUtils.applyColorSpace(src, cs1);
        }

        return dst;
    }

    private BufferedImage scaleImage(BufferedImage src, BufferedImage dst) {
        if (dst == null) {
            dst = createCompatibleDestImage(src, dstWidth, dstHeight);
        }
        return (type == MULTISTEP) ? getProgressiveScaledImage(src, dst) : getScaledImage(src, dst);
    }

    private BufferedImage getScaledImage(BufferedImage src, BufferedImage dst) {
        Graphics2D g2 = dst.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RENDERING_HINTS[type]);
        g2.setComposite(AlphaComposite.Src);
        g2.drawImage(src, 0, 0, dst.getWidth(), dst.getHeight(), null);
        g2.dispose();
        return dst;
    }

    private BufferedImage getProgressiveScaledImage(BufferedImage src, BufferedImage dst) {
        if (dst.getWidth() < src.getWidth() && dst.getHeight() < src.getHeight()) {
            return getAverageDownImage(src, dst);
        }
        return getAverageImage(src, dst);
    }

    private BufferedImage getAverageImage(BufferedImage src, BufferedImage dst) {
        Graphics2D g = dst.createGraphics();
        g.drawImage(src.getScaledInstance(dst.getWidth(), dst.getHeight(), Image.SCALE_AREA_AVERAGING), 0, 0, null);
        g.dispose();
        return dst;
    }

    private BufferedImage getAverageDownImage(BufferedImage src, BufferedImage dst) {

        BufferedImage retImg = src;
        int prevW = retImg.getWidth();
        int prevH = retImg.getHeight();

        BufferedImage tmpImg = null;
        Graphics2D g2 = null;

        int w = src.getWidth();
        int h = src.getHeight();

        do {
            if (w > dst.getWidth()) {
                w /= 2;
                if (w < dst.getWidth()) {
                    w = dst.getWidth();
                }
            }

            if (h > dst.getHeight()) {
                h /= 2;
                if (h < dst.getHeight()) {
                    h = dst.getHeight();
                }
            }

            if (tmpImg == null) {
                // Use a single scratch buffer for all iterations
                ColorModel dstCM = dst.getColorModel();
                tmpImg = new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(w, h), dstCM.isAlphaPremultiplied(), null);
                g2 = tmpImg.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RENDERING_HINTS[MULTISTEP]);
            }

            g2.drawImage(retImg, 0, 0, w, h, 0, 0, prevW, prevH, null);
            prevW = w;
            prevH = h;
            retImg = tmpImg;
        }
        while (w != dst.getWidth() || h != dst.getHeight());

        g2.dispose();

        // If we used a scratch buffer that is larger than our
        // target size, create an image of the right size and copy
        // the results into it
        if (dst.getWidth() != retImg.getWidth() || dst.getHeight() != retImg.getHeight()) {
            g2 = dst.createGraphics();
            g2.drawImage(retImg, 0, 0, null);
            g2.dispose();
        } else {
            dst = retImg;
        }

        return dst;
    }

    @Override
    public String toString() {
        return "Distort/Resize";
    }
}
