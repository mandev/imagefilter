package com.jhlabs.filter;

import com.jhlabs.image.AbstractBufferedImageOp;

/**
 * A filter which crops an image to a given rectangle.
 */
public class IdentityFilter extends AbstractBufferedImageOp {

    /**
     * Construct a Identity Filter.
     */
    public IdentityFilter() {
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
    public String toString() {
        return "Distort/Idenity";
    }
}
