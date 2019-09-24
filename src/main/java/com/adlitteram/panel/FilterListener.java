package com.adlitteram.panel;

import com.jhlabs.image.AbstractBufferedImageOp;

public interface FilterListener {

    //
    public void setFilter(AbstractBufferedImageOp filter);

    public float getFilterScale();
}
