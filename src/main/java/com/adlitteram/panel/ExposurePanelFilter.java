package com.adlitteram.panel;

import com.adlitteram.jasmin.gui.widget.FloatJSpinSlider;
import com.adlitteram.util.Message;
import com.jhlabs.image.AbstractBufferedImageOp;
import com.jhlabs.image.ExposureFilter;
import cz.autel.dmi.HIGConstraints;
import cz.autel.dmi.HIGLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExposurePanelFilter extends AbstractPanelFilter implements ChangeListener {

    //
    private static final Logger logger = LoggerFactory.getLogger(ExposurePanelFilter.class);
    //
    private JPanel panel;
    private FloatJSpinSlider exposureSlider;
    private float defaultExposure = 1.4f;

    @Override
    public String getName() {
        return Message.get("ExposureFilter");
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireFilterListeners();
    }

    @Override
    public void reset() {
        exposureSlider.setFloatValue(defaultExposure);
    }

    @Override
    public boolean isParametrable() {
        return true;
    }

    @Override
    public JPanel getPanel() {
        if (panel == null) {

            exposureSlider = new FloatJSpinSlider(defaultExposure, 0f, 5f, 0.01f, 0.1f, 10);
            exposureSlider.addChangeListener(this);

            int w[] = {10, 0, 10};
            int h[] = {10, 0, 0, 10};
            HIGLayout l = new HIGLayout(w, h);
            HIGConstraints c = new HIGConstraints();
            l.setColumnWeight(2, 1);

            panel = new JPanel(l);
            panel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), getName()));
            panel.add(new JLabel(Message.get("Exposure") + " :"), c.xy(2, 2, "l"));
            panel.add(exposureSlider, c.xy(2, 3));
        }

        return panel;
    }

    @Override
    public AbstractBufferedImageOp getFilter(float scale) {

        float exposure = exposureSlider.getFloatValue();

        ExposureFilter filter = new ExposureFilter();
        filter.setExposure(exposure);
        return filter;
    }
}
