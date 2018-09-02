
package com.adlitteram.panel;

import com.jhlabs.image.AbstractBufferedImageOp;
import java.awt.Shape;
import javax.swing.JPanel;

public interface PanelFilter {
    //
    public String getName();

    public JPanel getPanel();

    public void reset();

    public void setRoiShape(Shape shape);

    public boolean isParametrable();

    public AbstractBufferedImageOp getFilter(float scale);

    public void addFilterListener(FilterListener listener);

    public void removeFilterListener(FilterListener listener);
}
