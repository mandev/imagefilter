package com.adlitteram.panel;

import com.adlitteram.jasmin.gui.widget.FloatJSpinSlider;
import com.adlitteram.jasmin.gui.widget.JSpinSlider;
import com.adlitteram.util.Message;

import com.jhlabs.image.AbstractBufferedImageOp;
import com.jhlabs.image.UnsharpFilter;
import cz.autel.dmi.HIGConstraints;
import cz.autel.dmi.HIGLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class UnsharpMaskPanelFilter extends AbstractPanelFilter implements ChangeListener {

    //
    private JPanel panel;
    private FloatJSpinSlider radiusSlider;
    private JSpinSlider amountSlider;
    private JSpinSlider thresholdSlider;

    @Override
    public String getName() {
        return Message.get("UnsharpMaskFilter");
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireFilterListeners();
    }

    @Override
    public void reset() {
        radiusSlider.setFloatValue(2f);
        amountSlider.setValue(50);
        thresholdSlider.setValue(2);
    }

    @Override
    public boolean isParametrable() {
        return true;
    }

    @Override
    public JPanel getPanel() {
        if (panel == null) {

            radiusSlider = new FloatJSpinSlider(2f, 0f, 10f, .5f, 5f, 10);
            radiusSlider.addChangeListener(this);

            amountSlider = new JSpinSlider(50, 0, 100, 1, 25, 10);
            amountSlider.addChangeListener(this);

            thresholdSlider = new JSpinSlider(2, 0, 255, 1, 255, 10);
            thresholdSlider.addChangeListener(this);

            int w[] = {10, 0, 10};
            int h[] = {10, 0, 0, 10, 0, 0, 10, 0, 0, 10};
            HIGLayout l = new HIGLayout(w, h);
            HIGConstraints c = new HIGConstraints();
            l.setColumnWeight(2, 1);

            panel = new JPanel(l);
            panel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), getName()));
            panel.add(new JLabel(Message.get("Radius") + " :"), c.xy(2, 2, "l"));
            panel.add(radiusSlider, c.xy(2, 3));
            panel.add(new JLabel(Message.get("Amount") + " :"), c.xy(2, 5));
            panel.add(amountSlider, c.xy(2, 6));
            panel.add(new JLabel(Message.get("Threshold") + " :"), c.xy(2, 8));
            panel.add(thresholdSlider, c.xy(2, 9));
        }
        return panel;
    }

    @Override
    public AbstractBufferedImageOp getFilter(float scale) {
        float radius = radiusSlider.getFloatValue() * scale;
        float amount = (float) amountSlider.getValue() / 100f;
        int threshold = thresholdSlider.getValue();

        UnsharpFilter filter = new UnsharpFilter();
        filter.setRadius(radius);
        filter.setAmount(amount);
        filter.setThreshold(threshold);
        return filter;
    }
}
