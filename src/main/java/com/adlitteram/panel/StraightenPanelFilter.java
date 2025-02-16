package com.adlitteram.panel;

import com.adlitteram.jasmin.gui.widget.FloatJSpinSlider;
import com.adlitteram.jasmin.utils.GuiUtils;
import com.adlitteram.util.Message;
import com.adlitteram.util.NumUtils;
import com.jhlabs.filter.StraightenFilter;
import com.jhlabs.image.AbstractBufferedImageOp;
import com.jhlabs.image.TransformFilter;
import cz.autel.dmi.HIGConstraints;
import cz.autel.dmi.HIGLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StraightenPanelFilter extends AbstractPanelFilter implements ChangeListener, ActionListener {

    private static final String[] INTERPOLATION_ARRAY = {Message.get("Nearest"), Message.get("Bilinear")};
    private static final int DEFAULT_ANGLE = 0;

    private JPanel panel;
    private FloatJSpinSlider angleSlider;
    private JCheckBox keepSizeCheck;
    private JComboBox<String> interpolationCombo;

    @Override
    public String getName() {
        return Message.get("StraightenFilter");
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
        angleSlider.setFloatValue(DEFAULT_ANGLE);
        keepSizeCheck.setSelected(true);
        interpolationCombo.setSelectedIndex(1);
    }

    @Override
    public boolean isParametrable() {
        return true;
    }

    @Override
    public JPanel getPanel() {
        if (panel == null) {
            angleSlider = new FloatJSpinSlider(DEFAULT_ANGLE, -15f, 15f, 0.1f, 1f, 10);
            angleSlider.addChangeListener(this);

            keepSizeCheck = new JCheckBox(Message.get("KeepOriginalSize"), true);
            keepSizeCheck.addActionListener(this);

            interpolationCombo = new JComboBox<>(INTERPOLATION_ARRAY);
            interpolationCombo.setSelectedIndex(1);
            interpolationCombo.addActionListener(this);

            int[] w = {10, 0, 10};
            int[] h = {10, 0, 0, 10, 0, 10, 0, 10};
            HIGLayout l = new HIGLayout(w, h);
            HIGConstraints c = new HIGConstraints();
            l.setColumnWeight(2, 1);

            panel = new JPanel(l);
            panel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), getName()));
            panel.add(new JLabel(Message.get("Angle") + " :"), c.xy(2, 2, "l"));
            panel.add(angleSlider, c.xy(2, 3));
            panel.add(GuiUtils.addLabel(interpolationCombo, Message.get("Interpolation")), c.xy(2, 7, "l"));
        }

        return panel;
    }

    @Override
    public AbstractBufferedImageOp getFilter(float scale) {
        float angle = NumUtils.toRadian(angleSlider.getFloatValue());
        int interpolation = (interpolationCombo.getSelectedIndex() == 0) ? TransformFilter.NEAREST_NEIGHBOUR : TransformFilter.BILINEAR;
        boolean keepSize = keepSizeCheck.isSelected();

        StraightenFilter filter = new StraightenFilter(angle, keepSize);
        filter.setInterpolation(interpolation);
        return filter;
    }
}
