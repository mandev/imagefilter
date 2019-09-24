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
package com.jhlabs.filter;

import com.jhlabs.image.ImageMath;
import com.jhlabs.image.TransformFilter;
import java.awt.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A filter which rotates an image. These days this is easier done with Java2D,
 * but this filter remains.
 */
public class StraightenFilter extends TransformFilter {

    private static final Logger logger = LoggerFactory.getLogger(StraightenFilter.class);
    //

    private float angle;
    private float cos;
    private float sin;
    private float scale;
    private boolean keepSize = true;

    /**
     * Construct a RotateFilter.
     */
    public StraightenFilter() {
        this(ImageMath.PI);
    }

    /**
     * Construct a RotateFilter.
     *
     * @param angle the angle to rotate
     */
    public StraightenFilter(float angle) {
        this(angle, true);
    }

    /**
     * Construct a RotateFilter.
     *
     * @param angle the angle to rotate
     * @param resize true if the output image should be resized
     */
    public StraightenFilter(float angle, boolean keepSize) {
        setAngle(angle);
        this.keepSize = keepSize;
    }

    /**
     * Specifies the angle of rotation.
     *
     * @param angle the angle of rotation.
     * @angle
     * @see #getAngle
     */
    public void setAngle(float angle) {
        this.angle = angle;
        cos = (float) Math.cos(this.angle);
        sin = (float) Math.sin(this.angle);
    }

    /**
     * Returns the angle of rotation.
     *
     * @return the angle of rotation.
     * @see #setAngle
     */
    public float getAngle() {
        return angle;
    }

    @Override
    protected void transformSpace(Rectangle rect) {

        float x, y, width, height;
        float s = getScale(rect.width, rect.height);

        if (keepSize) {
            scale = s;
            x = rect.x / s;
            y = rect.y / s;
            width = rect.width / s;
            height = rect.height / s;
        }
        else {
            scale = 1f;
            x = rect.x;
            y = rect.y;
            width = rect.width;
            height = rect.height;
        }

        float[] out = {0f, 0f};
        float minx = Float.MAX_VALUE;
        float miny = Float.MAX_VALUE;
        float maxx = Float.MIN_VALUE;
        float maxy = Float.MIN_VALUE;

        transform(x, y, out);
        minx = Math.min(minx, out[0]);
        miny = Math.min(miny, out[1]);
        maxx = Math.max(maxx, out[0]);
        maxy = Math.max(maxy, out[1]);

        transform(x + width, y, out);
        minx = Math.min(minx, out[0]);
        miny = Math.min(miny, out[1]);
        maxx = Math.max(maxx, out[0]);
        maxy = Math.max(maxy, out[1]);

        transform(x, y + height, out);
        minx = Math.min(minx, out[0]);
        miny = Math.min(miny, out[1]);
        maxx = Math.max(maxx, out[0]);
        maxy = Math.max(maxy, out[1]);

        transform(x + width, y + height, out);
        minx = Math.min(minx, out[0]);
        miny = Math.min(miny, out[1]);
        maxx = Math.max(maxx, out[0]);
        maxy = Math.max(maxy, out[1]);

        float ww = width * s;
        float hh = height * s;
        rect.x = Math.round(minx + (maxx - minx - ww) / 2f);
        rect.y = Math.round(miny + (maxy - miny - hh) / 2f);
        rect.width = Math.round(ww);
        rect.height = Math.round(hh);
    }

    // Igor Krivokon
    // TODO : deal with angle > +/- 90°
    private float getScale(int width, int height) {
        double a = Math.abs(angle);
        float c = (float) Math.cos(a);
        float s = (float) Math.sin(a);
        float ha = (float) (height * height) / (width * s + height * c);
        float hb = (float) (height * width) / (width * c + height * s);
        return Math.min(ha, hb) / (float) height;
    }

    private void transform(float x, float y, float[] out) {
        out[0] = x * cos + y * sin;
        out[1] = y * cos - x * sin;
    }

    @Override
    protected void transformInverse(int x, int y, float[] out) {
        out[0] = (x * cos - y * sin) * scale;
        out[1] = (y * cos + x * sin) * scale;
    }

    @Override
    public String toString() {
        return "Straighten " + (int) (angle * 180 / Math.PI);
    }
}
