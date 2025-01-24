package com.adlitteram.panel;

import com.adlitteram.jasmin.gui.widget.FloatJSpinSlider;
import com.adlitteram.util.Message;
import com.jhlabs.image.AbstractBufferedImageOp;
import com.jhlabs.image.MaskFilter;
import cz.autel.dmi.HIGConstraints;
import cz.autel.dmi.HIGLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class MaskPanelFilter extends AbstractPanelFilter implements ChangeListener {

    private JPanel panel;
    private FloatJSpinSlider alphaSlider;
    private FloatJSpinSlider redSlider;
    private FloatJSpinSlider greenSlider;
    private FloatJSpinSlider blueSlider;

    private static final float DEFAULT_ALPHA = 100f;
    private static final float DEFAULT_RED = 100f;
    private static final float DEFAULT_GREEN = 100f;
    private static final float DEFAULT_BLUE = 100f;

    @Override
    public String getName() {
        return Message.get("MaskXFilter");
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireFilterListeners();
    }

    @Override
    public void reset() {
        alphaSlider.setFloatValue(DEFAULT_ALPHA);
        redSlider.setFloatValue(DEFAULT_RED);
        greenSlider.setFloatValue(DEFAULT_GREEN);
        blueSlider.setFloatValue(DEFAULT_BLUE);
    }

    @Override
    public boolean isParametrable() {
        return true;
    }

    @Override
    public JPanel getPanel() {
        if (panel == null) {

            alphaSlider = new FloatJSpinSlider(DEFAULT_ALPHA, 0f, 100f, 1f, 1f, 10);
            alphaSlider.addChangeListener(this);

            redSlider = new FloatJSpinSlider(DEFAULT_RED, 0f, 100f, 1f, 1f, 10);
            redSlider.addChangeListener(this);

            greenSlider = new FloatJSpinSlider(DEFAULT_GREEN, 0f, 100f, 1f, 1f, 10);
            greenSlider.addChangeListener(this);

            blueSlider = new FloatJSpinSlider(DEFAULT_BLUE, 0f, 100f, 1f, 1f, 10);
            blueSlider.addChangeListener(this);

            int[] w = {10, 0, 10};
            int[] h = {10, 0, 0, 10, 0, 0, 10, 0, 0, 10, 0, 0, 10};
            HIGLayout l = new HIGLayout(w, h);
            HIGConstraints c = new HIGConstraints();
            l.setColumnWeight(2, 1);

            panel = new JPanel(l);
            panel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), getName()));
            panel.add(new JLabel(Message.get("Alpha") + " :"), c.xy(2, 2, "l"));
            panel.add(alphaSlider, c.xy(2, 3));
            panel.add(new JLabel(Message.get("Red") + " :"), c.xy(2, 5, "l"));
            panel.add(redSlider, c.xy(2, 6));
            panel.add(new JLabel(Message.get("Green") + " :"), c.xy(2, 8, "l"));
            panel.add(greenSlider, c.xy(2, 9));
            panel.add(new JLabel(Message.get("Blue") + " :"), c.xy(2, 11, "l"));
            panel.add(blueSlider, c.xy(2, 12));
        }

        return panel;
    }

    @Override
    public AbstractBufferedImageOp getFilter(float scale) {

        int alpha = Math.min(255, Math.round(alphaSlider.getFloatValue() / 100f * 255f));
        int red = Math.min(255, Math.round(redSlider.getFloatValue() / 100f * 255f));
        int green = Math.min(255, Math.round(greenSlider.getFloatValue() / 100f * 255f));
        int blue = Math.min(255, Math.round(blueSlider.getFloatValue() / 100f * 255f));

        int mask = alpha << 24 | red << 16 | green << 8 | blue;
        MaskFilter filter = new MaskFilter();
        filter.setMask(mask);
        return filter;
    }
}
