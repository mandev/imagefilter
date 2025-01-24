package com.adlitteram.panel;

import com.jhlabs.image.AbstractBufferedImageOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public abstract class AbstractPanelFilter implements PanelFilter {

    private final ArrayList<FilterListener> listeners = new ArrayList<>();

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

}
