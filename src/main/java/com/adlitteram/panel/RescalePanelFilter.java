package com.adlitteram.panel;

import com.adlitteram.jasmin.gui.widget.FloatJSpinSlider;
import com.adlitteram.util.Message;
import com.jhlabs.image.AbstractBufferedImageOp;
import com.jhlabs.image.RescaleFilter;
import cz.autel.dmi.HIGConstraints;
import cz.autel.dmi.HIGLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class RescalePanelFilter extends AbstractPanelFilter implements ChangeListener {

    //
    private JPanel panel;
    private FloatJSpinSlider rescaleSlider;
    //
    private float defaultScale = 1f;

    @Override
    public String getName() {
        return Message.get("RescaleFilter");
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireFilterListeners();
    }

    @Override
    public void reset() {
        rescaleSlider.setFloatValue(defaultScale);
    }

    @Override
    public boolean isParametrable() {
        return true;
    }

    @Override
    public JPanel getPanel() {
        if (panel == null) {

            rescaleSlider = new FloatJSpinSlider(defaultScale, 0f, 5f, 0.1f, 0.1f, 10);
            rescaleSlider.addChangeListener(this);

            int w[] = {10, 0, 10};
            int h[] = {10, 0, 0, 10};
            HIGLayout l = new HIGLayout(w, h);
            HIGConstraints c = new HIGConstraints();
            l.setColumnWeight(2, 1);

            panel = new JPanel(l);
            panel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), getName()));
            panel.add(new JLabel(Message.get("Scale") + " :"), c.xy(2, 2, "l"));
            panel.add(rescaleSlider, c.xy(2, 3));
        }

        return panel;
    }

    @Override
    public AbstractBufferedImageOp getFilter(float scale) {

        float rescale = rescaleSlider.getFloatValue();

        RescaleFilter filter = new RescaleFilter();
        filter.setScale(rescale);
        return filter;
    }
}
