package com.adlitteram.panel;

import com.adlitteram.jasmin.gui.widget.FloatJSpinSlider;
import com.adlitteram.util.Message;
import com.jhlabs.image.AbstractBufferedImageOp;
import com.jhlabs.image.OpacityFilter;
import cz.autel.dmi.HIGConstraints;
import cz.autel.dmi.HIGLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class OpacityPanelFilter extends AbstractPanelFilter implements ChangeListener {

    private JPanel panel;
    private FloatJSpinSlider opacitySlider;
    private static final float DEFAULT_OPACITY = 100f;

    @Override
    public String getName() {
        return Message.get("OpacityFilter");
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireFilterListeners();
    }

    @Override
    public void reset() {
        opacitySlider.setFloatValue(DEFAULT_OPACITY);
    }

    @Override
    public boolean isParametrable() {
        return true;
    }

    @Override
    public JPanel getPanel() {
        if (panel == null) {

            opacitySlider = new FloatJSpinSlider(DEFAULT_OPACITY, 0f, 100f, 1f, 1f, 10);
            opacitySlider.addChangeListener(this);

            int[] w = {10, 0, 10};
            int[] h = {10, 0, 0, 10, 0, 0, 10, 0, 0, 10};
            HIGLayout l = new HIGLayout(w, h);
            HIGConstraints c = new HIGConstraints();
            l.setColumnWeight(2, 1);

            panel = new JPanel(l);
            panel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), getName()));
            panel.add(new JLabel(Message.get("Opacity") + " :"), c.xy(2, 2, "l"));
            panel.add(opacitySlider, c.xy(2, 3));
        }

        return panel;
    }

    @Override
    public AbstractBufferedImageOp getFilter(float scale) {

        int opacity = Math.min(255, Math.round(opacitySlider.getFloatValue() / 100f * 255f));

        OpacityFilter filter = new OpacityFilter();
        filter.setOpacity(opacity);
        return filter;
    }
}
