package com.adlitteram.panel;

import com.jhlabs.image.AbstractBufferedImageOp;

import javax.swing.*;
import java.awt.*;

public interface PanelFilter {

    String getName();

    JPanel getPanel();

    void reset();

    void setRoiShape(Shape shape);

    boolean isParametrable();

    AbstractBufferedImageOp getFilter(float scale);

    void addFilterListener(FilterListener listener);

    void removeFilterListener(FilterListener listener);
}
