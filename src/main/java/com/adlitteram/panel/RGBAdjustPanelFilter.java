package com.adlitteram.panel;

import com.adlitteram.jasmin.gui.widget.FloatJSpinSlider;
import com.adlitteram.util.Message;
import com.jhlabs.image.AbstractBufferedImageOp;
import com.jhlabs.image.RGBAdjustFilter;
import cz.autel.dmi.HIGConstraints;
import cz.autel.dmi.HIGLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class RGBAdjustPanelFilter extends AbstractPanelFilter implements ChangeListener {

    private JPanel panel;
    private FloatJSpinSlider redSlider;
    private FloatJSpinSlider greenSlider;
    private FloatJSpinSlider blueSlider;

    @Override
    public String getName() {
        return Message.get("RGBAdjustFilter");
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireFilterListeners();
    }

    @Override
    public void reset() {
        redSlider.setFloatValue(0);
        greenSlider.setFloatValue(0);
        blueSlider.setFloatValue(0);
    }

    @Override
    public boolean isParametrable() {
        return true;
    }

    @Override
    public JPanel getPanel() {
        if (panel == null) {

            redSlider = new FloatJSpinSlider(0f, -1f, 1f, .1f, .1f, 10);
            redSlider.addChangeListener(this);

            greenSlider = new FloatJSpinSlider(0f, -1f, 1f, .1f, .1f, 10);
            greenSlider.addChangeListener(this);

            blueSlider = new FloatJSpinSlider(0f, -1f, 1f, .1f, .1f, 10);
            blueSlider.addChangeListener(this);

            int w[] = {10, 0, 10};
            int h[] = {10, 0, 0, 10, 0, 0, 10, 0, 0, 10};
            HIGLayout l = new HIGLayout(w, h);
            HIGConstraints c = new HIGConstraints();
            l.setColumnWeight(2, 1);

            panel = new JPanel(l);
            panel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), getName()));
            panel.add(new JLabel(Message.get("Red") + " :"), c.xy(2, 2, "l"));
            panel.add(redSlider, c.xy(2, 3));
            panel.add(new JLabel(Message.get("Green") + " :"), c.xy(2, 5, "l"));
            panel.add(greenSlider, c.xy(2, 6));
            panel.add(new JLabel(Message.get("Blue") + " :"), c.xy(2, 8, "l"));
            panel.add(blueSlider, c.xy(2, 9));
        }
        return panel;
    }

    @Override
    public AbstractBufferedImageOp getFilter(float scale) {
        float red = redSlider.getFloatValue();
        float green = greenSlider.getFloatValue();
        float blue = blueSlider.getFloatValue();

        RGBAdjustFilter filter = new RGBAdjustFilter();
        filter.setRFactor(red);
        filter.setGFactor(green);
        filter.setBFactor(blue);
        return filter;
    }
}
