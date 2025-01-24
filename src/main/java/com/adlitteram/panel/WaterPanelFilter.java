package com.adlitteram.panel;

import com.adlitteram.jasmin.gui.widget.FloatJSpinSlider;
import com.adlitteram.jasmin.utils.GuiUtils;
import com.adlitteram.util.Message;
import com.adlitteram.util.NumUtils;
import com.jhlabs.filter.StraightenFilter;
import com.jhlabs.image.AbstractBufferedImageOp;
import com.jhlabs.image.TransformFilter;
import com.jhlabs.image.WaterFilter;
import cz.autel.dmi.HIGConstraints;
import cz.autel.dmi.HIGLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class WaterPanelFilter extends AbstractPanelFilter implements ChangeListener, ActionListener {


    private static final String[] INTERPOLATION_ARRAY = {Message.get("Nearest"), Message.get("Bilinear")};
    private static final float DEFAULT_WAVELENGTH = 50f;
    private static final float DEFAULT_AMPLITUDE = 1f;
    private static final float DEFAULT_PHASE = 0f;
    private static final float DEFAULT_CENTREX = 0.5f;
    private static final float DEFAULT_CENTREY = 0.5f;
    private static final float DEFAULT_RADIUS = 0f;

    private JPanel panel;
    private FloatJSpinSlider wavelengthSlider;
    private FloatJSpinSlider amplitudeSlider;
    private FloatJSpinSlider phaseSlider;
    private FloatJSpinSlider centreXSlider;
    private FloatJSpinSlider centreYSlider;
    private FloatJSpinSlider radiusSlider;
    private JComboBox<String> interpolationCombo;

    @Override
    public String getName() {
        return Message.get("WaterFilter");
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireFilterListeners();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        fireFilterListeners();
    }

    @Override
    public void reset() {
        wavelengthSlider.setFloatValue(DEFAULT_WAVELENGTH);
        amplitudeSlider.setFloatValue(DEFAULT_AMPLITUDE);
        phaseSlider.setFloatValue(DEFAULT_PHASE);
        centreXSlider.setFloatValue(DEFAULT_CENTREX);
        centreYSlider.setFloatValue(DEFAULT_CENTREY);
        radiusSlider.setFloatValue(DEFAULT_RADIUS);
        interpolationCombo.setSelectedIndex(1);
    }

    @Override
    public boolean isParametrable() {
        return true;
    }

    @Override
    public JPanel getPanel() {
        if (panel == null) {

            wavelengthSlider = new FloatJSpinSlider(DEFAULT_WAVELENGTH, 1f, 500f, 1f);
            wavelengthSlider.addChangeListener(this);

            amplitudeSlider = new FloatJSpinSlider(DEFAULT_AMPLITUDE, 0f, 20f, 0.1f);
            amplitudeSlider.addChangeListener(this);

            phaseSlider = new FloatJSpinSlider(DEFAULT_PHASE, -180f, 180f, 1f);
            phaseSlider.addChangeListener(this);

            centreXSlider = new FloatJSpinSlider(DEFAULT_CENTREX, 0f, 1f, 0.1f);
            centreXSlider.addChangeListener(this);

            centreYSlider = new FloatJSpinSlider(DEFAULT_CENTREY, 0f, 1f, 0.1f);
            centreYSlider.addChangeListener(this);

            radiusSlider = new FloatJSpinSlider(DEFAULT_RADIUS, 0f, 10000f, 10f, 10f, 5);
            radiusSlider.addChangeListener(this);

            interpolationCombo = new JComboBox<>(INTERPOLATION_ARRAY);
            interpolationCombo.setSelectedIndex(1);
            interpolationCombo.addActionListener(this);

            int[] w = {10, 0, 10};
            int[] h = {10, 0, 0, 10, 0, 0, 10, 0, 0, 10, 0, 0, 10, 0, 0, 10, 0, 0, 10, 0, 10};
            HIGLayout l = new HIGLayout(w, h);
            HIGConstraints c = new HIGConstraints();
            l.setColumnWeight(2, 1);

            panel = new JPanel(l);
            panel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), getName()));
            panel.add(new JLabel(Message.get("WaveLength") + " :"), c.xy(2, 2, "l"));
            panel.add(wavelengthSlider, c.xy(2, 3));
            panel.add(new JLabel(Message.get("Amplitude") + " :"), c.xy(2, 5, "l"));
            panel.add(amplitudeSlider, c.xy(2, 6));
            panel.add(new JLabel(Message.get("Phase") + " :"), c.xy(2, 8, "l"));
            panel.add(phaseSlider, c.xy(2, 9));
            panel.add(new JLabel(Message.get("CentreX") + " :"), c.xy(2, 11, "l"));
            panel.add(centreXSlider, c.xy(2, 12));
            panel.add(new JLabel(Message.get("CentreY") + " :"), c.xy(2, 14, "l"));
            panel.add(centreYSlider, c.xy(2, 15));
            panel.add(new JLabel(Message.get("Radius") + " :"), c.xy(2, 17, "l"));
            panel.add(radiusSlider, c.xy(2, 18));
            panel.add(GuiUtils.addLabel(interpolationCombo, Message.get("Interpolation")), c.xy(2, 20, "l"));
        }

        return panel;
    }

    @Override
    public AbstractBufferedImageOp getFilter(float scale) {
        float wavelength = wavelengthSlider.getFloatValue() * scale;
        float amplitude = amplitudeSlider.getFloatValue();
        float phase = NumUtils.toRadian(phaseSlider.getFloatValue());
        float centreX = centreXSlider.getFloatValue();
        float centreY = centreYSlider.getFloatValue();
        float radius = radiusSlider.getFloatValue() * scale;
        int interpolation = (interpolationCombo.getSelectedIndex() == 0) ? TransformFilter.NEAREST_NEIGHBOUR : TransformFilter.BILINEAR;

        WaterFilter filter = new WaterFilter();
        filter.setWavelength(wavelength);
        filter.setAmplitude(amplitude);
        filter.setPhase(phase);
        filter.setCentreX(centreX);
        filter.setCentreY(centreY);
        filter.setRadius(radius);
        filter.setInterpolation(interpolation);
        return filter;
    }
}
