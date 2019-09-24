package com.adlitteram.panel;

import com.adlitteram.jasmin.gui.widget.FloatJSpinSlider;
import com.adlitteram.util.Message;
import com.jhlabs.filter.PerlinContrastFilter;

import com.jhlabs.image.AbstractBufferedImageOp;
import cz.autel.dmi.HIGConstraints;
import cz.autel.dmi.HIGLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class PerlinContrastPanelFilter extends AbstractPanelFilter implements ChangeListener {

    //
    private JPanel panel;
    private FloatJSpinSlider contrastSlider;

    @Override
    public String getName() {
        return Message.get("PerlinContrastFilter");
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireFilterListeners();
    }

    @Override
    public void reset() {
        contrastSlider.setFloatValue(.5f);
    }

    @Override
    public boolean isParametrable() {
        return true;
    }

    @Override
    public JPanel getPanel() {
        if (panel == null) {

            contrastSlider = new FloatJSpinSlider(.5f, 0f, 1f, .01f, .5f, 10);
            contrastSlider.addChangeListener(this);

            int w[] = {10, 0, 10};
            int h[] = {10, 0, 0, 10};
            HIGLayout l = new HIGLayout(w, h);
            HIGConstraints c = new HIGConstraints();
            l.setColumnWeight(2, 1);

            panel = new JPanel(l);
            panel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), getName()));
            panel.add(new JLabel(Message.get("Contrast") + " :"), c.xy(2, 2, "l"));
            panel.add(contrastSlider, c.xy(2, 3));
        }
        return panel;
    }

    @Override
    public AbstractBufferedImageOp getFilter(float scale) {
        float c = contrastSlider.getFloatValue();
        return new PerlinContrastFilter(c);
    }
}
