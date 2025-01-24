package com.adlitteram.panel;

import com.adlitteram.jasmin.gui.widget.FloatJSpinSlider;
import com.adlitteram.util.Message;
import com.jhlabs.filter.GammaContrastFilter;
import com.jhlabs.image.AbstractBufferedImageOp;
import cz.autel.dmi.HIGConstraints;
import cz.autel.dmi.HIGLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class ContrastPanelFilter extends AbstractPanelFilter implements ChangeListener {

    private JPanel panel;
    private FloatJSpinSlider brigthnessSlider;
    private FloatJSpinSlider contrastSlider;
    private FloatJSpinSlider gammaSlider;

    private static final float DEFAULT_BRIGTHNESS = 1f;
    private static final float DEFAULT_CONTRAST = 1f;
    private static final float DEFAULT_GAMMA = 1f;

    @Override
    public String getName() {
        return Message.get("ContrastFilter");
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireFilterListeners();
    }

    @Override
    public void reset() {
        brigthnessSlider.setFloatValue(DEFAULT_BRIGTHNESS);
        contrastSlider.setFloatValue(DEFAULT_CONTRAST);
        gammaSlider.setFloatValue(DEFAULT_GAMMA);
    }

    @Override
    public boolean isParametrable() {
        return true;
    }

    @Override
    public JPanel getPanel() {
        if (panel == null) {

            brigthnessSlider = new FloatJSpinSlider(DEFAULT_BRIGTHNESS, 0f, 2f, 0.01f, 0.5f, 10);
            brigthnessSlider.addChangeListener(this);

            contrastSlider = new FloatJSpinSlider(DEFAULT_CONTRAST, 0f, 2f, 0.01f, 0.5f, 10);
            contrastSlider.addChangeListener(this);

            gammaSlider = new FloatJSpinSlider(DEFAULT_GAMMA, 0f, 3f, 0.01f, 0.5f, 10);
            gammaSlider.addChangeListener(this);

            int[] w = {10, 0, 10};
            int[] h = {10, 0, 0, 10, 0, 0, 10, 0, 0, 10};
            HIGLayout l = new HIGLayout(w, h);
            HIGConstraints c = new HIGConstraints();
            l.setColumnWeight(2, 1);

            panel = new JPanel(l);
            panel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), getName()));
            panel.add(new JLabel(Message.get("Brightness") + " :"), c.xy(2, 2, "l"));
            panel.add(brigthnessSlider, c.xy(2, 3));
            panel.add(new JLabel(Message.get("Contrast") + " :"), c.xy(2, 5, "l"));
            panel.add(contrastSlider, c.xy(2, 6));
            panel.add(new JLabel(Message.get("Gamma") + " :"), c.xy(2, 8, "l"));
            panel.add(gammaSlider, c.xy(2, 9));
        }

        return panel;
    }

    @Override
    public AbstractBufferedImageOp getFilter(float scale) {

        float bright = brigthnessSlider.getFloatValue();
        float contrast = contrastSlider.getFloatValue();
        float gamma = gammaSlider.getFloatValue();

        GammaContrastFilter filter = new GammaContrastFilter();
        filter.setBrightness(bright);
        filter.setContrast(contrast);
        filter.setGamma(gamma);

        return filter;
    }
}
