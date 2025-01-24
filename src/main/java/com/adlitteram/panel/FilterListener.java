package com.adlitteram.panel;

import com.jhlabs.image.AbstractBufferedImageOp;

public interface FilterListener {

    void setFilter(AbstractBufferedImageOp filter);

    float getFilterScale();
}
