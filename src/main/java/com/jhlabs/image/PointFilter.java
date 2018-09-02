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

import static com.jhlabs.image.AbstractBufferedImageOp.getImageType;
import static com.jhlabs.image.AbstractBufferedImageOp.getRGB0;
import static com.jhlabs.image.AbstractBufferedImageOp.setRGB0;
import com.jhlabs.utils.ThreadUtils;
import java.awt.image.BufferedImage;
import java.util.concurrent.RecursiveAction;

/**
 * An abstract superclass for point filters. The interface is the same as the
 * old RGBImageFilter.
 */
public abstract class PointFilter extends AbstractBufferedImageOp {

    protected boolean canFilterIndexColorModel = false;

    //@Override
    public BufferedImage _filterRGB32(BufferedImage src, BufferedImage dst) {

        int width = src.getWidth();
        int height = src.getHeight();
        setDimensions(width, height);

        if (dst == null) {
            dst = createCompatibleDestImage(src);
        }

        int rgbType = getImageType(src);

        int[] pixels = new int[width];

        for (int y = 0; y < height; y++) {
            getRGB0(src, rgbType, 0, y, width, 1, pixels);
            for (int x = 0; x < width; x++) {
                pixels[x] = filterRGB(x, y, pixels[x]);
            }
            setRGB0(dst, rgbType, 0, y, width, 1, pixels);
        }
        return dst;
    }

    @Override
    public BufferedImage filterRGB32(BufferedImage src, BufferedImage dst) {

        int width = src.getWidth();
        int height = src.getHeight();
        setDimensions(width, height);
        int rgbType = getImageType(src);

        if (dst == null) {
            dst = createCompatibleDestImage(src);
        }
        int threshold = Math.max(ThreadUtils.THRESHOLD, (width * height) / (ThreadUtils.getAvailableProcessors() * 10));
        ThreadUtils.forkJoin(new PointAction(0, height, src, dst, rgbType, threshold));
        return dst;
    }

    private class PointAction extends RecursiveAction {

        private int threshold;
        private int start;
        private int end;
        private BufferedImage src;
        private BufferedImage dst;
        private int rgbType;

        private PointAction(int start, int end, BufferedImage src, BufferedImage dst, int rgbType, int threshold) {
            this.start = start;
            this.end = end;
            this.src = src;
            this.dst = dst;
            this.rgbType = rgbType;
            this.threshold = threshold;
        }

        @Override
        public void compute() {
            int width = src.getWidth();
            int t = (end - start) * width;
            if (t < threshold) {
                int[] pixels = new int[width];

                for (int y = start; y < end; y++) {
                    getRGB0(src, rgbType, 0, y, width, 1, pixels);
                    for (int x = 0; x < width; x++) {
                        pixels[x] = filterRGB(x, y, pixels[x]);
                    }
                    setRGB0(dst, rgbType, 0, y, width, 1, pixels);
                }
            }
            else {
                int split = (end - start) / 2;
                invokeAll(new PointAction(start, start + split, src, dst, rgbType, threshold),
                        new PointAction(start + split, end, src, dst, rgbType, threshold));
            }

        }
    }

    public void setDimensions(int width, int height) {
    }

    public abstract int filterRGB(int x, int y, int rgb);
}
