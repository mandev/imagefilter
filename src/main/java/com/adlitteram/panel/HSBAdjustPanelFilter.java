
package com.adlitteram.panel;

import com.adlitteram.jasmin.gui.widget.FloatJSpinSlider;
import com.adlitteram.util.Message;
import com.jhlabs.image.AbstractBufferedImageOp;
import com.jhlabs.image.HSBAdjustFilter;
import cz.autel.dmi.HIGConstraints;
import cz.autel.dmi.HIGLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class HSBAdjustPanelFilter extends AbstractPanelFilter implements ChangeListener {
    //
    private JPanel panel;
    private FloatJSpinSlider hueSlider;
    private FloatJSpinSlider saturationSlider;
    private FloatJSpinSlider brigthnessSlider;

    @Override
    public String getName() {
        return Message.get("HSBAdjustFilter");
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireFilterListeners();
    }

    @Override
    public void reset() {
        hueSlider.setFloatValue(0);
        saturationSlider.setFloatValue(0);
        brigthnessSlider.setFloatValue(0);
    }

    @Override
    public boolean isParametrable() {
        return true;
    }

    @Override
    public JPanel getPanel() {
        if (panel == null) {

            hueSlider = new FloatJSpinSlider(0f, -1f, 1f, .01f, .01f, 10);
            hueSlider.addChangeListener(this);

            saturationSlider = new FloatJSpinSlider(0f, -1f, 1f, .01f, .01f, 10);
            saturationSlider.addChangeListener(this);

            brigthnessSlider = new FloatJSpinSlider(0f, -1f, 1f, .01f, .01f, 10);
            brigthnessSlider.addChangeListener(this);

            int w[] = {10, 0, 10};
            int h[] = {10, 0, 0, 10, 0, 0, 10, 0, 0, 10};
            HIGLayout l = new HIGLayout(w, h);
            HIGConstraints c = new HIGConstraints();
            l.setColumnWeight(2, 1);

            panel = new JPanel(l);
            panel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), getName()));
            panel.add(new JLabel(Message.get("Hue") + " :"), c.xy(2, 2, "l"));
            panel.add(hueSlider, c.xy(2, 3));
            panel.add(new JLabel(Message.get("Saturation") + " :"), c.xy(2, 5, "l"));
            panel.add(saturationSlider, c.xy(2, 6));
            panel.add(new JLabel(Message.get("Brightness") + " :"), c.xy(2, 8, "l"));
            panel.add(brigthnessSlider, c.xy(2, 9));
        }
        return panel;
    }

    @Override
    public AbstractBufferedImageOp getFilter(float scale) {
        float h = hueSlider.getFloatValue();
        float s = saturationSlider.getFloatValue();
        float b = brigthnessSlider.getFloatValue();

        HSBAdjustFilter filter = new HSBAdjustFilter();
        filter.setHFactor(h);
        filter.setSFactor(s);
        filter.setBFactor(b);
        return filter;
    }
}
