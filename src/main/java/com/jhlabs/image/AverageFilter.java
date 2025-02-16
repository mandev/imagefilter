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

/**
 * A filter which averages the 3x3 neighbourhood of each pixel, providing a
 * simple blur.
 */
public class AverageFilter extends ConvolveFilter {

    /**
     * The convolution kernal for the averaging.
     */
    protected static float[] theMatrix = {0.1f, 0.1f, 0.1f, 0.1f, 0.2f, 0.1f, 0.1f, 0.1f, 0.1f};

    public AverageFilter() {
        super(theMatrix);
    }

    @Override
    public String toString() {
        return "Blur/Average Blur";
    }
}
