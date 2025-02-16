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

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class ScratchFilter extends AbstractBufferedImageOp {

    private float density = 0.1f;
    private float angle;
    private float angleVariation = 1.0f;
    private float width = 0.5f;
    private float length = 0.5f;
    private int color = 0xffffffff;
    private int seed = 0;

    public ScratchFilter() {
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngleVariation(float angleVariation) {
        this.angleVariation = angleVariation;
    }

    public float getAngleVariation() {
        return angleVariation;
    }

    public void setDensity(float density) {
        this.density = density;
    }

    public float getDensity() {
        return density;
    }

    public void setLength(float length) {
        this.length = length;
    }

    public float getLength() {
        return length;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getWidth() {
        return width;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public int getSeed() {
        return seed;
    }

    public BufferedImage filterRGB32(BufferedImage src, BufferedImage dst) {
        if (dst == null) {
            dst = createCompatibleDestImage(src, null);
        }

        int width = src.getWidth();
        int height = src.getHeight();
        int numScratches = (int) (density * width * height / 100);
        ArrayList lines = new ArrayList();
        {
            float l = length * width;
            Random random = new Random(seed);
            Graphics2D g = dst.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(color));
            g.setStroke(new BasicStroke(this.width));
            for (int i = 0; i < numScratches; i++) {
                float x = width * random.nextFloat();
                float y = height * random.nextFloat();
                float a = angle + ImageMath.TWO_PI * (angleVariation * (random.nextFloat() - 0.5f));
                float s = (float) Math.sin(a) * l;
                float c = (float) Math.cos(a) * l;
                float x1 = x - c;
                float y1 = y - s;
                float x2 = x + c;
                float y2 = y + s;
                g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
                lines.add(new Line2D.Float(x1, y1, x2, y2));
            }
            g.dispose();
        }

        if (false) {
//		int[] inPixels = getPixelsRGB( src, 0, 0, width, height, null );
            int[] inPixels = new int[width * height];
            int index = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    float sx = x, sy = y;
                    for (int i = 0; i < numScratches; i++) {
                        Line2D.Float l = (Line2D.Float) lines.get(i);
                        float dot = (l.x2 - l.x1) * (sx - l.x1) + (l.y2 - l.y1) * (sy - l.y1);
                        if (dot > 0) {
                            inPixels[index] |= (1 << i);
                        }
                    }
                    index++;
                }
            }

            Colormap colormap = new LinearColormap();
            index = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    float f = (float) (inPixels[index] & 0x7fffffff) / 0x7fffffff;
                    inPixels[index] = colormap.getColor(f);
                    index++;
                }
            }
            setRGB(dst, 0, 0, width, height, inPixels);
        }
        return dst;
    }

    public String toString() {
        return "Render/Scratches...";
    }
}
