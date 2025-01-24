package com.adlitteram.panel;

import com.adlitteram.jasmin.gui.widget.JSpinSlider;
import com.adlitteram.util.Message;
import com.jhlabs.filter.SepiaFilter;
import com.jhlabs.image.AbstractBufferedImageOp;
import cz.autel.dmi.HIGConstraints;
import cz.autel.dmi.HIGLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class SepiaPanelFilter extends AbstractPanelFilter implements ChangeListener {

    private JPanel panel;
    private JSpinSlider amountSlider;

    @Override
    public String getName() {
        return Message.get("SepiaFilter");
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireFilterListeners();
    }

    @Override
    public void reset() {
        amountSlider.setValue(30);
    }

    @Override
    public boolean isParametrable() {
        return true;
    }

    @Override
    public JPanel getPanel() {
        if (panel == null) {

            amountSlider = new JSpinSlider(30, 5, 100, 1, 5, 10);
            amountSlider.addChangeListener(this);

            int[] w = {10, 0, 10};
            int[] h = {10, 0, 0, 10};
            HIGLayout l = new HIGLayout(w, h);
            HIGConstraints c = new HIGConstraints();
            l.setColumnWeight(2, 1);

            panel = new JPanel(l);
            panel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), getName()));
            panel.add(new JLabel(Message.get("Amount") + " :"), c.xy(2, 2, "l"));
            panel.add(amountSlider, c.xy(2, 3));
        }
        return panel;
    }

    @Override
    public AbstractBufferedImageOp getFilter(float scale) {
        int amount = amountSlider.getValue();
        return new SepiaFilter(amount);
    }
}
