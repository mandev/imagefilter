package com.adlitteram.panel;

import com.adlitteram.jasmin.gui.widget.FloatJSpinSlider;
import com.adlitteram.jasmin.gui.widget.JSpinSlider;
import com.adlitteram.util.Message;
import com.jhlabs.image.AbstractBufferedImageOp;
import com.jhlabs.image.ChromeFilter;
import cz.autel.dmi.HIGConstraints;
import cz.autel.dmi.HIGLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ChromePanelFilter extends AbstractPanelFilter implements ChangeListener {

    //
    private JPanel panel;
    private FloatJSpinSlider softnessSlider;
    private FloatJSpinSlider heightSlider;
    private JSpinSlider amountSlider;
    private FloatJSpinSlider exposureSlider;

    @Override
    public String getName() {
        return Message.get("ChromeFilter");
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireFilterListeners();
    }

    @Override
    public void reset() {
        softnessSlider.setFloatValue(5f);
        heightSlider.setFloatValue(2f);
        amountSlider.setValue(50);
        exposureSlider.setFloatValue(1.5f);
    }

    @Override
    public boolean isParametrable() {
        return true;
    }

    @Override
    public JPanel getPanel() {
        if (panel == null) {

            softnessSlider = new FloatJSpinSlider(5f, 0f, 50f, 1f, 10f, 10);
            softnessSlider.addChangeListener(this);

            heightSlider = new FloatJSpinSlider(2f, 0f, 5f, .5f, 1f, 10);
            heightSlider.addChangeListener(this);

            amountSlider = new JSpinSlider(50, 0, 100, 1, 25, 10);
            amountSlider.addChangeListener(this);

            exposureSlider = new FloatJSpinSlider(1.5f, 0f, 5f, 0.5f, 1f, 10);
            exposureSlider.addChangeListener(this);

            int w[] = {10, 0, 10};
            int h[] = {10, 0, 0, 10, 0, 0, 10, 0, 0, 10, 0, 0, 10};
            HIGLayout l = new HIGLayout(w, h);
            HIGConstraints c = new HIGConstraints();
            l.setColumnWeight(2, 1);

            panel = new JPanel(l);
            panel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), getName()));
            panel.add(new JLabel(Message.get("Softness") + " :"), c.xy(2, 2, "l"));
            panel.add(softnessSlider, c.xy(2, 3));
            panel.add(new JLabel(Message.get("Height") + " :"), c.xy(2, 5, "l"));
            panel.add(heightSlider, c.xy(2, 6));
            panel.add(new JLabel(Message.get("Amount") + " :"), c.xy(2, 8));
            panel.add(amountSlider, c.xy(2, 9));
            panel.add(new JLabel(Message.get("Exposure") + " :"), c.xy(2, 11));
            panel.add(exposureSlider, c.xy(2, 12));
        }
        return panel;
    }

    public AbstractBufferedImageOp getFilter(float scale) {
        float softness = softnessSlider.getFloatValue() * scale;
        float height = heightSlider.getFloatValue() * scale;
        float amount = (float) amountSlider.getValue() / 100f;
        float exposure = exposureSlider.getFloatValue();

        ChromeFilter filter = new ChromeFilter();
        filter.setBumpSoftness(softness);
        filter.setBumpHeight(height);
        filter.setAmount(amount);
        filter.setExposure(exposure);
        return filter;
    }
}
