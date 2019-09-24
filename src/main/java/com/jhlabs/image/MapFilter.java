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

import com.jhlabs.math.Function2D;

public class MapFilter extends TransformFilter {

    private Function2D xMapFunction;
    private Function2D yMapFunction;

    public MapFilter() {
    }

    public void setXMapFunction(Function2D xMapFunction) {
        this.xMapFunction = xMapFunction;
    }

    public Function2D getXMapFunction() {
        return xMapFunction;
    }

    public void setYMapFunction(Function2D yMapFunction) {
        this.yMapFunction = yMapFunction;
    }

    public Function2D getYMapFunction() {
        return yMapFunction;
    }

    protected void transformInverse(int x, int y, float[] out) {
        out[0] = xMapFunction.evaluate(x, y) * transformedSpace.width;
        out[1] = yMapFunction.evaluate(x, y) * transformedSpace.height;
    }

    @Override
    public String toString() {
        return "Distort/Map Coordinates...";
    }
}
