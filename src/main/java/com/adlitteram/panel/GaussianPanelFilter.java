package com.adlitteram.panel;

import com.adlitteram.jasmin.gui.widget.JSpinSlider;
import com.adlitteram.util.Message;

import com.jhlabs.image.AbstractBufferedImageOp;
import com.jhlabs.image.GaussianFilter;
import cz.autel.dmi.HIGConstraints;
import cz.autel.dmi.HIGLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class GaussianPanelFilter extends AbstractPanelFilter implements ChangeListener {

    private JPanel panel;
    private JSpinSlider radiusSlider;

    @Override
    public String getName() {
        return Message.get("GaussianFilter");
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireFilterListeners();
    }

    @Override
    public void reset() {
        radiusSlider.setValue(1);
    }

    @Override
    public boolean isParametrable() {
        return true;
    }

    @Override
    public JPanel getPanel() {
        if (panel == null) {

            radiusSlider = new JSpinSlider(2, 1, 30, 1, 5, 10);
            radiusSlider.addChangeListener(this);

            int w[] = {10, 0, 10};
            int h[] = {10, 0, 0, 10};
            HIGLayout l = new HIGLayout(w, h);
            HIGConstraints c = new HIGConstraints();
            l.setColumnWeight(2, 1);

            panel = new JPanel(l);
            panel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), getName()));
            panel.add(new JLabel(Message.get("Radius") + " :"), c.xy(2, 2, "l"));
            panel.add(radiusSlider, c.xy(2, 3));
        }
        return panel;
    }

    @Override
    public AbstractBufferedImageOp getFilter(float scale) {
        float radius = (float) radiusSlider.getValue() * scale;
        return new GaussianFilter(radius);
    }
}
