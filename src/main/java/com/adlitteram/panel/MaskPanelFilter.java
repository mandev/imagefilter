package com.adlitteram.panel;

import com.adlitteram.jasmin.gui.widget.FloatJSpinSlider;
import com.adlitteram.util.Message;
import com.jhlabs.image.AbstractBufferedImageOp;
import com.jhlabs.image.MaskFilter;
import cz.autel.dmi.HIGConstraints;
import cz.autel.dmi.HIGLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MaskPanelFilter extends AbstractPanelFilter implements ChangeListener {

    //
    private JPanel panel;
    private FloatJSpinSlider alphaSlider;
    private FloatJSpinSlider redSlider;
    private FloatJSpinSlider greenSlider;
    private FloatJSpinSlider blueSlider;
    //
    private float defaultAlpha = 100f;
    private float defaultRed = 100f;
    private float defaultGreen = 100f;
    private float defaultBlue = 100f;

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
        alphaSlider.setFloatValue(defaultAlpha);
        redSlider.setFloatValue(defaultRed);
        greenSlider.setFloatValue(defaultGreen);
        blueSlider.setFloatValue(defaultBlue);
    }

    @Override
    public boolean isParametrable() {
        return true;
    }

    @Override
    public JPanel getPanel() {
        if (panel == null) {

            alphaSlider = new FloatJSpinSlider(defaultAlpha, 0f, 100f, 1f, 1f, 10);
            alphaSlider.addChangeListener(this);

            redSlider = new FloatJSpinSlider(defaultRed, 0f, 100f, 1f, 1f, 10);
            redSlider.addChangeListener(this);

            greenSlider = new FloatJSpinSlider(defaultGreen, 0f, 100f, 1f, 1f, 10);
            greenSlider.addChangeListener(this);

            blueSlider = new FloatJSpinSlider(defaultBlue, 0f, 100f, 1f, 1f, 10);
            blueSlider.addChangeListener(this);

            int w[] = {10, 0, 10};
            int h[] = {10, 0, 0, 10, 0, 0, 10, 0, 0, 10, 0, 0, 10};
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
