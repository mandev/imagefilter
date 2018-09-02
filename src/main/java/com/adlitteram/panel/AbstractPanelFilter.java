package com.adlitteram.panel;

import com.jhlabs.image.AbstractBufferedImageOp;
import java.awt.Shape;
import java.util.ArrayList;
import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPanelFilter implements PanelFilter {
    //

    private static final Logger logger = LoggerFactory.getLogger(AbstractPanelFilter.class);
    //
    private ArrayList<FilterListener> listeners = new ArrayList<>();

    @Override
    public void addFilterListener(FilterListener listener) {
        for (FilterListener l : listeners) {
            if (l == listener) {
                return;
            }
        }
        listeners.add(listener);
    }

    @Override
    public void removeFilterListener(FilterListener listener) {
        listeners.remove(listener);
    }

    protected void fireFilterListeners() {
        for (FilterListener l : listeners) {
            l.setFilter(getFilter(l.getFilterScale()));
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public void reset() {
        // Do nothing
    }

    @Override
    public void setRoiShape(Shape shape) {
        // Do nothing
    }

    @Override
    public boolean isParametrable() {
        return false;
    }

    @Override
    abstract public String getName();

    @Override
    abstract public JPanel getPanel();

    @Override
    abstract public AbstractBufferedImageOp getFilter(float scale);
}
