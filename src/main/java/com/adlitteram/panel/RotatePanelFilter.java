
package com.adlitteram.panel;

import com.adlitteram.jasmin.gui.widget.FloatJSpinSlider;
import com.adlitteram.jasmin.utils.GuiUtils;
import com.adlitteram.util.Message;
import com.jhlabs.filter.StraightenFilter;
import com.jhlabs.image.AbstractBufferedImageOp;
import com.jhlabs.image.RotateFilter;
import cz.autel.dmi.HIGConstraints;
import cz.autel.dmi.HIGLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RotatePanelFilter extends AbstractPanelFilter implements ChangeListener, ActionListener {
    private static final Logger logger = LoggerFactory.getLogger(RotatePanelFilter.class);
    //
    private static final double DEGREE_TO_RADIAN = Math.PI / 180d;
    private static final String[] INTERPOLATION_ARRAY = {Message.get("Nearest"), Message.get("Bilinear")};
    private static final int DEFAULT_ANGLE = 0;
    //
    private JPanel panel;
    private FloatJSpinSlider angleSlider;
    private JCheckBox resizeCheck;
    private JComboBox interpolationCombo;

    @Override
    public String getName() {
        return Message.get("RotateFilter");
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
        resizeCheck.setSelected(true);
        interpolationCombo.setSelectedIndex(1);
    }

    @Override
    public boolean isParametrable() {
        return true;
    }

    @Override
    public JPanel getPanel() {
        if (panel == null) {

            angleSlider = new FloatJSpinSlider(DEFAULT_ANGLE, -180f, 180f, 1f, 1f, 10);
            angleSlider.addChangeListener(this);

            resizeCheck = new JCheckBox(Message.get("ReSize"), true);
            resizeCheck.addActionListener(this);

            interpolationCombo = new JComboBox(INTERPOLATION_ARRAY);
            interpolationCombo.setSelectedIndex(1);
            interpolationCombo.addActionListener(this);

            int w[] = {10, 0, 10};
            int h[] = {10, 0, 0, 10, 0, 10, 0, 10};
            HIGLayout l = new HIGLayout(w, h);
            HIGConstraints c = new HIGConstraints();
            l.setColumnWeight(2, 1);

            panel = new JPanel(l);
            panel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), getName()));
            panel.add(new JLabel(Message.get("Angle") + " :"), c.xy(2, 2, "l"));
            panel.add(angleSlider, c.xy(2, 3));
            panel.add(resizeCheck, c.xy(2, 5, "l"));
            panel.add(GuiUtils.addLabel(Message.get("Interpolation") + " :", interpolationCombo), c.xy(2, 7, "l"));
        }

        return panel;
    }

    @Override
    public AbstractBufferedImageOp getFilter(float scale) {
        float angle = (float) (angleSlider.getFloatValue() * DEGREE_TO_RADIAN);
        int interpolation = (interpolationCombo.getSelectedIndex() == 0) ? StraightenFilter.NEAREST_NEIGHBOUR : StraightenFilter.BILINEAR;
        boolean resize = resizeCheck.isSelected();

        RotateFilter filter = new RotateFilter(angle, resize);
        filter.setInterpolation(interpolation);
        return filter;
    }
}
