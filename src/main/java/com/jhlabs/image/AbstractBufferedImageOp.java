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

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A convenience class which implements those methods of BufferedImageOp which
 * are rarely changed.
 */
public abstract class AbstractBufferedImageOp implements BufferedImageOp, Cloneable {

    private static final Logger logger = LoggerFactory.getLogger(AbstractBufferedImageOp.class);

    public static final int RGB32_TYPE = 1; // RGB (24 bits) or ARGB (32 bits)
    public static final int GRAY8_TYPE = 2; // GRAY (8 bits)
    public static final int GRAY_2X8_TYPE = 3; // AGRAY (2x8 bits)
    public static final int RGB_3X8_TYPE = 4; // RGB (3x8 bits)
    public static final int RGB_4X8_TYPE = 5; // ARGB (4x8 bits)
    public static final int CMYK_4X8_TYPE = 6; // CMYK (4x8 bits)
    public static final int UNKNOWN_TYPE = 10; // Unknown

    @Override
    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        int imageType = getImageType(src);
        //logger.info("ImageType: " + imageType) ;

        if (dst != null && getImageType(dst) != imageType) {
            imageType = UNKNOWN_TYPE;
        }

        if ((imageType == GRAY8_TYPE || imageType == GRAY_2X8_TYPE) && isGraySupported()) {
            return filterGRAY8(src, dst);
        }

        if (imageType == CMYK_4X8_TYPE && isCmykSupported()) {
            return filterCMYK32(src, dst);
        }

        return filterRGB32(src, dst);
    }

    public BufferedImage filterRGB32(BufferedImage src, BufferedImage dst) {
        logger.info("Using default filterRGB32: do nothing");
        return src;
    }

    public BufferedImage filterCMYK32(BufferedImage src, BufferedImage dst) {
        logger.info("Using default filterCMYK32: do nothing");
        return src;
    }

    public BufferedImage filterGRAY8(BufferedImage src, BufferedImage dst) {
        logger.info("Using default filterGRAY8: do nothing");
        return src;
    }

    // Indicates if the filter natively supports Gray (8bits)
    public boolean isGraySupported() {
        return false;
    }

    // Indicates if the filter natively supports CMYK (32 bits)
    public boolean isCmykSupported() {
        return false;
    }

    public BufferedImage createCompatibleDestImage(BufferedImage src) {
        ColorModel dstCM = src.getColorModel();
        return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(src.getWidth(), src.getHeight()), dstCM.isAlphaPremultiplied(), null);
    }

    public BufferedImage createCompatibleDestImage(BufferedImage src, int width, int height) {
        ColorModel dstCM = src.getColorModel();
        return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(width, height), dstCM.isAlphaPremultiplied(), null);
    }

    @Override
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel dstCM) {
        if (dstCM == null) {
            dstCM = src.getColorModel();
        }
        return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(src.getWidth(), src.getHeight()), dstCM.isAlphaPremultiplied(), null);
    }

    public BufferedImage createCompatibleDestImage(BufferedImage src, int width, int height, ColorModel dstCM) {
        if (dstCM == null) {
            dstCM = src.getColorModel();
        }
        return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(width, height), dstCM.isAlphaPremultiplied(), null);
    }

    public static BufferedImage createBufferedImage(ColorModel colorModel, int width, int height, int[] pixels) {
        // RGB
        if (colorModel instanceof DirectColorModel) {
            DataBuffer intBuffer = new DataBufferInt(pixels, pixels.length);
            SampleModel sampleModel = colorModel.createCompatibleSampleModel(width, height);
            WritableRaster raster = Raster.createWritableRaster(sampleModel, intBuffer, null);
            return new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);
        }

        // GRAY
        if (colorModel instanceof ComponentColorModel && colorModel.getTransferType() == DataBuffer.TYPE_BYTE && colorModel.getNumComponents() == 1) {
            byte byteArray[] = new byte[width * height];
            for (int j = 0, i = 0; i < pixels.length; i++) {
                int rgb = pixels[i];
                byteArray[j++] = (byte) ((float) (((rgb >> 16) & 0xff) + ((rgb >> 8) & 0xff) + (rgb & 0xff)) / 3f + .49f);
            }

            DataBuffer byteBuffer = new DataBufferByte(byteArray, width * height * 1);
            SampleModel sampleModel = colorModel.createCompatibleSampleModel(width, height);
            WritableRaster raster = Raster.createWritableRaster(sampleModel, byteBuffer, null);
            return new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);
        }

        WritableRaster raster = colorModel.createCompatibleWritableRaster(width, height);
        BufferedImage bi = new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);
        bi.setRGB(0, 0, width, height, pixels, 0, width);
        return bi;
    }

    @Override
    public Rectangle2D getBounds2D(BufferedImage src) {
        return new Rectangle(0, 0, src.getWidth(), src.getHeight());
    }

    @Override
    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        if (dstPt == null) {
            return new Point2D.Double(srcPt.getX(), srcPt.getY());
        }
        dstPt.setLocation(srcPt.getX(), srcPt.getY());
        return dstPt;
    }

    @Override
    public RenderingHints getRenderingHints() {
        return null;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        StringBuilder toStringBuilder = new StringBuilder();
        toStringBuilder.append(super.toString());
        toStringBuilder.append("\n");
        return toStringBuilder.toString();
    }

    public static int getImageType(BufferedImage image) {
        ColorModel colorModel = image.getColorModel();

        if (colorModel instanceof DirectColorModel) {
            return RGB32_TYPE;
        }

        if (colorModel instanceof ComponentColorModel && colorModel.getTransferType() == DataBuffer.TYPE_BYTE) {
            int numComponents = colorModel.getNumComponents();
            int colorSpaceType = colorModel.getColorSpace().getType();
            if (numComponents == 1 && colorSpaceType == ColorSpace.TYPE_GRAY) {
                return GRAY8_TYPE;
            }
            if (numComponents == 2 && colorSpaceType == ColorSpace.TYPE_GRAY) {
                return GRAY_2X8_TYPE;
            }
            if (numComponents == 3 && colorSpaceType == ColorSpace.TYPE_RGB) {
                return RGB_3X8_TYPE;
            }
            if (numComponents == 4) {
                if (colorSpaceType == ColorSpace.TYPE_RGB) {
                    return RGB_4X8_TYPE;
                }
                if (colorSpaceType == ColorSpace.TYPE_CMYK) {
                    return CMYK_4X8_TYPE;
                }
            }
        }
        // Unknown
        return UNKNOWN_TYPE;
    }

    // BufferedImage is Gray (8 bits) - Return internal array
    public static byte[] getGRAY(BufferedImage image) {
        return ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
    }

    // BufferedImage is Gray (8 bits) - Return internal array
    public static byte[] getGRAY(BufferedImage image, int sx, int sy, int width, int height, byte[] pixels) {
        if (pixels == null) {
            pixels = new byte[width * height];
        }
        image.getRaster().getDataElements(sx, sy, width, height, pixels);
        return pixels;
    }

    // BufferedImage is Gray (8 bits) 
    public static void setGRAY(BufferedImage image, int sx, int sy, int width, int height, byte[] pixels) {
        if (pixels == null) {
            pixels = new byte[width * height];
        }
        image.getRaster().setDataElements(sx, sy, width, height, pixels);
    }

    // RGB (32 bits)
    public static int[] getRGB(BufferedImage image) {
        int imageType = getImageType(image);
        if (imageType == RGB32_TYPE) {
            return ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        }
        return getRGB(image, 0, 0, image.getWidth(), image.getHeight(), null);
    }

    // Return a RGB (32 bits) array    
    public static int[] getRGB(BufferedImage image, int sx, int sy, int width, int height, int[] pixels) {
        if (pixels == null) {
            pixels = new int[width * height];
        }
        return getRGB0(image, getImageType(image), sx, sy, width, height, pixels);
    }

    // Return a RGB (32 bits) array    
    public static int[] getRGB0(BufferedImage image, int imageType, int sx, int sy, int width, int height, int[] pixels) {
        switch (imageType) {
            case RGB32_TYPE:
                return getRGB1(image, sx, sy, width, height, pixels);
            case GRAY8_TYPE:
                return getRGB2(image, sx, sy, width, height, pixels);
            case RGB_4X8_TYPE:
            case CMYK_4X8_TYPE:
                return getRGB3(image, sx, sy, width, height, pixels);
            case RGB_3X8_TYPE:
                return getRGB4(image, sx, sy, width, height, pixels);
        }
        return getRGB9(image, sx, sy, width, height, pixels);
    }

    // BufferedImage is RGB or ARGB (32 bits)
    public static int[] getRGB1(BufferedImage image, int sx, int sy, int width, int height, int[] pixels) {
        if (pixels == null) {
            pixels = new int[width * height];
        }
        image.getRaster().getDataElements(sx, sy, width, height, pixels);
        return pixels;
    }

    // BufferedImage is GRAY (8 bits => 32 bits)
    public static int[] getRGB2(BufferedImage image, int sx, int sy, int width, int height, int[] pixels) {
        if (pixels == null) {
            pixels = new int[width * height];
        }
        Raster raster = image.getRaster();
        byte[] row = new byte[width];

        for (int i = 0, y = 0; y < height; y++) {
            raster.getDataElements(sx, sy + y, width, 1, row);
            for (int x = 0; x < width; x++) {
                int b = (row[x] & 0xff);
                pixels[i++] = 0xff << 24 | b << 16 | b << 8 | b;
            }
        }
        return pixels;
    }

    // BufferedImage is ARGB or CMYK (4x8 bits => 32 bits)
    public static int[] getRGB3(BufferedImage image, int sx, int sy, int width, int height, int[] pixels) {
        if (pixels == null) {
            pixels = new int[width * height];
        }
        Raster raster = image.getRaster();
        byte[] row = new byte[4 * width];

        for (int i = 0, y = 0; y < height; y++) {
            raster.getDataElements(sx, sy + y, width, 1, row);
            for (int j = 0, x = 0; x < width; x++) {
                pixels[i++] = (row[j] & 0xff) << 24 | (row[j + 1] & 0xff) << 16 | (row[j + 2] & 0xff) << 8 | row[j + 3] & 0xff;
                j += 4;
            }
        }
        return pixels;
    }

    // BufferedImage is RGB (3x8 bits => 32 bits)
    public static int[] getRGB4(BufferedImage image, int sx, int sy, int width, int height, int[] pixels) {
        if (pixels == null) {
            pixels = new int[width * height];
        }
        Raster raster = image.getRaster();
        byte[] row = new byte[3 * width];

        for (int i = 0, y = 0; y < height; y++) {
            raster.getDataElements(sx, sy + y, width, 1, row);
            for (int j = 0, x = 0; x < width; x++) {
                pixels[i++] = (row[j] & 0xff) << 16 | (row[j + 1] & 0xff) << 8 | row[j + 2] & 0xff;
                j += 3;
            }
        }
        return pixels;
    }

    // Unknown
    public static int[] getRGB9(BufferedImage image, int sx, int sy, int width, int height, int[] pixels) {
        return image.getRGB(sx, sy, width, height, pixels, 0, width);
    }

    /**
     * A convenience method for setting ARGB pixels in an image. This tries to
     * avoid the performance penalty of BufferedImage.setRGB unmanaging the
     * image.
     *
     * @param image a BufferedImage object
     * @param sx the left edge of the pixel block
     * @param sy the right edge of the pixel block
     * @param width the width of the pixel arry
     * @param height the height of the pixel arry
     * @param pixels the array of pixels to set
     * @see #getPixelsRGB
     */
    public static void setRGB(BufferedImage image, int sx, int sy, int width, int height, int[] pixels) {
        if (pixels == null) {
            pixels = new int[height * width];
        }
        setRGB0(image, getImageType(image), sx, sy, width, height, pixels);
    }

    public static void setRGB0(BufferedImage image, int imageType, int sx, int sy, int width, int height, int[] pixels) {
        switch (imageType) {
            case RGB32_TYPE:
                setRGB1(image, sx, sy, width, height, pixels);
                return;
            case GRAY8_TYPE:
                setRGB2(image, sx, sy, width, height, pixels);
                return;
            case RGB_3X8_TYPE:
                setRGB4(image, sx, sy, width, height, pixels);
                return;
            case RGB_4X8_TYPE:
                setRGB3(image, sx, sy, width, height, pixels);
                return;
            case CMYK_4X8_TYPE:
                setRGB3(image, sx, sy, width, height, pixels);
                return;
        }
        setRGB9(image, sx, sy, width, height, pixels);
    }

    // RGB (24 bits) or ARGB (32 bits)
    public static void setRGB1(BufferedImage image, int sx, int sy, int width, int height, int[] pixels) {
        image.getRaster().setDataElements(sx, sy, width, height, pixels);
    }

    // GRAY (8 bits)
    public static void setRGB2(BufferedImage image, int sx, int sy, int width, int height, int[] pixels) {
        WritableRaster raster = image.getRaster();
        final float INV3 = 1f / 3f;
        byte[] row = new byte[1 * width];

        for (int i = 0, y = 0; y < height; y++) {
            for (int j = 0, x = 0; x < width; x++) {
                int rgb = pixels[i++];
                row[j++] = (byte) ((float) (((rgb >> 16) & 0xff) + ((rgb >> 8) & 0xff) + (rgb & 0xff)) * INV3 + .5f);
            }
            raster.setDataElements(sx, sy + y, width, 1, row);
        }
    }

    // ARGB or CMYK (4x8 bits)
    public static void setRGB3(BufferedImage image, int sx, int sy, int width, int height, int[] pixels) {
        WritableRaster raster = image.getRaster();
        byte[] row = new byte[4 * width];

        for (int i = 0, y = 0; y < height; y++) {
            for (int j = 0, x = 0; x < width; x++) {
                int rgb = pixels[i++];
                row[j++] = (byte) ((rgb >> 24) & 0xff);
                row[j++] = (byte) ((rgb >> 16) & 0xff);
                row[j++] = (byte) ((rgb >> 8) & 0xff);
                row[j++] = (byte) (rgb & 0xff);
            }
            raster.setDataElements(sx, sy + y, width, 1, row);
        }
    }

    // RGB (3x8 bits)
    public static void setRGB4(BufferedImage image, int sx, int sy, int width, int height, int[] pixels) {
        WritableRaster raster = image.getRaster();
        byte[] row = new byte[3 * width];

        for (int i = 0, y = 0; y < height; y++) {
            for (int j = 0, x = 0; x < width; x++) {
                int rgb = pixels[i++];
                row[j++] = (byte) ((rgb >> 16) & 0xff);
                row[j++] = (byte) ((rgb >> 8) & 0xff);
                row[j++] = (byte) (rgb & 0xff);
            }
            raster.setDataElements(sx, sy + y, width, 1, row);
        }
    }

    // Unknown
    public static void setRGB9(BufferedImage image, int sx, int sy, int width, int height, int[] pixels) {
        image.setRGB(sx, sy, width, height, pixels, 0, width);
    }

}
//    public BufferedImage createBufferedImage(ColorModel colorModel, int width, int height, int[] pixels) {
//
//        // RGB
//        if (colorModel instanceof DirectColorModel) {
//            System.err.println("createBufferedImage - DirectColorModel");
//            DataBuffer intBuffer = new DataBufferInt(pixels, pixels.length);
//            SampleModel sampleModel = colorModel.createCompatibleSampleModel(width, height);
//            WritableRaster raster = Raster.createWritableRaster(sampleModel, intBuffer, null);
//            return new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);
//        }
//
//        // GRAY
//        if (colorModel instanceof ComponentColorModel && colorModel.getTransferType() == DataBuffer.TYPE_BYTE && colorModel.getNumComponents() == 1) {
//            byte byteArray[] = new byte[width * height];
//            for (int j = 0, i = 0; i < pixels.length; i++) {
//                int rgb = pixels[i];
//                byteArray[j++] = (byte) ((float) (((rgb >> 16) & 0xff) + ((rgb >> 8) & 0xff) + (rgb & 0xff)) / 3f + .49f);
//            }
//
//            DataBuffer byteBuffer = new DataBufferByte(byteArray, width * height * 1);
//            SampleModel sampleModel = colorModel.createCompatibleSampleModel(width, height);
//            WritableRaster raster = Raster.createWritableRaster(sampleModel, byteBuffer, null);
//            return new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);
//        }
//
//        // OTHERS
//        System.err.println("createBufferedImage - other");
//        BufferedImage bi = new BufferedImage(colorModel, colorModel.createCompatibleWritableRaster(width, height), colorModel.isAlphaPremultiplied(), null);
//        bi.setRGB(0, 0, width, height, pixels, 0, width);
//        return bi;
//    }
//     public int[] getRGB4(WritableRaster raster, int startX, int startY, int w, int h, int[] rgbArray) {
//        if (rgbArray == null) rgbArray = new int[h * w];
//        byte[] data = new byte[4];
//
//        for (int i = 0, y = startY; y < startY + h; y++) {
//            for (int x = startX; x < startX + w; x++) {
//                raster.getDataElements(x, y, data);
//                rgbArray[i++] = (data[3] & 0xff) << 24 | (data[2] & 0xff) << 16 | (data[1] & 0xff) << 8 | data[0] & 0xff;
//            }
//        }
//        return rgbArray;
//    }
//    public void setRGB4(WritableRaster raster, int startX, int startY, int w, int h, int[] rgbArray) {
//        byte[] data = new byte[4];
//
//        for (int i = 0, y = startY; y < startY + h; y++) {
//            for (int x = startX; x < startX + w; x++) {
//                int rgb = rgbArray[i++];
//                data[0] = (byte) (rgb & 0xff);
//                data[1] = (byte) ((rgb >> 8) & 0xff);
//                data[2] = (byte) ((rgb >> 16) & 0xff);
//                data[3] = (byte) ((rgb >> 24) & 0xff);
//                raster.setDataElements(x, y, data);
//            }
//        }
//    }

