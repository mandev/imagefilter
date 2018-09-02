/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.adlitteram.panel;

import com.adlitteram.jasmin.gui.widget.FloatJSpinSlider;
import com.adlitteram.util.Message;
import com.jhlabs.image.AbstractBufferedImageOp;
import com.jhlabs.image.ThresholdFilter;
import cz.autel.dmi.HIGConstraints;
import cz.autel.dmi.HIGLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ThresholdPanelFilter extends AbstractPanelFilter implements ChangeListener {
    //
    private JPanel panel;
    private FloatJSpinSlider lowerThresholdSlider;
    private FloatJSpinSlider upperThresholdSlider;
    //
    private float defaultLowerThreshold = 25f;
    private float defaultUpperThreshold = 75f;

    @Override
    public String getName() {
        return Message.get("ThresholdFilter");
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireFilterListeners();
    }

    @Override
    public void reset() {
        lowerThresholdSlider.setFloatValue(defaultLowerThreshold);
        upperThresholdSlider.setFloatValue(defaultUpperThreshold);
    }

    @Override
    public boolean isParametrable() {
        return true;
    }

    @Override
    public JPanel getPanel() {
        if (panel == null) {

            lowerThresholdSlider = new FloatJSpinSlider(defaultLowerThreshold, 0f, 100f, 1f, 1f, 10);
            lowerThresholdSlider.addChangeListener(this);

            upperThresholdSlider = new FloatJSpinSlider(defaultUpperThreshold, 0f, 100f, 1f, 1f, 10);
            upperThresholdSlider.addChangeListener(this);

            int w[] = {10, 0, 10};
            int h[] = {10, 0, 0, 10, 0, 0, 10, 0, 0, 10};
            HIGLayout l = new HIGLayout(w, h);
            HIGConstraints c = new HIGConstraints();
            l.setColumnWeight(2, 1);

            panel = new JPanel(l);
            panel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), getName()));
            panel.add(new JLabel(Message.get("LowerThreshold") + " :"), c.xy(2, 2, "l"));
            panel.add(lowerThresholdSlider, c.xy(2, 3));
            panel.add(new JLabel(Message.get("UpperThreshold") + " :"), c.xy(2, 5, "l"));
            panel.add(upperThresholdSlider, c.xy(2, 6));
        }

        return panel;
    }

    @Override
    public AbstractBufferedImageOp getFilter(float scale) {

        int low = Math.min(255, Math.round(lowerThresholdSlider.getFloatValue() / 100f * 255f));
        int up = Math.min(255, Math.round(upperThresholdSlider.getFloatValue() / 100f * 255f));

        ThresholdFilter filter = new ThresholdFilter();
        filter.setLowerThreshold(low);
        filter.setUpperThreshold(up);
        return filter;
    }
}
