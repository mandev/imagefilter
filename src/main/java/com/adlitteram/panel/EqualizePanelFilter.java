package com.adlitteram.panel;

import com.adlitteram.util.Message;
import com.jhlabs.image.AbstractBufferedImageOp;
import com.jhlabs.image.EqualizeFilter;
import cz.autel.dmi.HIGConstraints;
import cz.autel.dmi.HIGLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class EqualizePanelFilter extends AbstractPanelFilter implements ChangeListener {

    private JPanel panel;

    @Override
    public String getName() {
        return Message.get("EqualizeFilter");
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireFilterListeners();
    }

    @Override
    public JPanel getPanel() {
        if (panel == null) {

            int[] w = {10, 0, 10};
            int[] h = {10, 0, 15};
            HIGLayout l = new HIGLayout(w, h);
            HIGConstraints c = new HIGConstraints();
            l.setColumnWeight(2, 1);

            panel = new JPanel(l);
            panel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), getName()));
            panel.add(new JLabel(Message.get("NoParameters")), c.xy(2, 2, ""));
        }
        return panel;
    }

    @Override
    public AbstractBufferedImageOp getFilter(float scale) {
        return new EqualizeFilter();
    }
}
