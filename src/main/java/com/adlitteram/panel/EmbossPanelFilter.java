
package com.adlitteram.panel;

import com.adlitteram.jasmin.gui.widget.JSpinSlider;
import com.adlitteram.util.Message;
import com.jhlabs.image.AbstractBufferedImageOp;
import com.jhlabs.image.EmbossFilter;
import cz.autel.dmi.HIGConstraints;
import cz.autel.dmi.HIGLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class EmbossPanelFilter extends AbstractPanelFilter implements ChangeListener {
    private JPanel panel;
    private JSpinSlider azimuthSlider;
    private JSpinSlider elevationSlider;
    private JSpinSlider bumpHeightSlider;

    @Override
    public String getName() {
        return Message.get("EmbossFilter");
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireFilterListeners();
    }

    @Override
    public void reset() {
        azimuthSlider.setValue(135);
        elevationSlider.setValue(30);
        bumpHeightSlider.setValue(1);
    }

    @Override
    public boolean isParametrable() {
        return true;
    }

    @Override
    public JPanel getPanel() {
        if (panel == null) {

            azimuthSlider = new JSpinSlider(135, 0, 360, 1, 90, 10);
            azimuthSlider.addChangeListener(this);

            elevationSlider = new JSpinSlider(30, 0, 90, 1, 30, 10);
            elevationSlider.addChangeListener(this);

            bumpHeightSlider = new JSpinSlider(1, 0, 20, 1, 5, 10);
            bumpHeightSlider.addChangeListener(this);

            int w[] = {10, 0, 10};
            int h[] = {10, 0, 0, 10, 0, 0, 10, 0, 0, 10};
            HIGLayout l = new HIGLayout(w, h);
            HIGConstraints c = new HIGConstraints();
            l.setColumnWeight(2, 1);

            panel = new JPanel(l);
            panel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), getName()));
            panel.add(new JLabel(Message.get("Azimuth") + " :"), c.xy(2, 2, "l"));
            panel.add(azimuthSlider, c.xy(2, 3));
            panel.add(new JLabel(Message.get("Elevation") + " :"), c.xy(2, 5));
            panel.add(elevationSlider, c.xy(2, 6));
            panel.add(new JLabel(Message.get("BumpHeight") + " :"), c.xy(2, 8));
            panel.add(bumpHeightSlider, c.xy(2, 9));
        }
        return panel;
    }

    @Override
    public AbstractBufferedImageOp getFilter(float scale) {
        float azimuth = (float) (azimuthSlider.getValue() * Math.PI / 180.0d);
        float elevation = (float) (elevationSlider.getValue() * Math.PI / 180.0d);
        float bumpHeight = bumpHeightSlider.getValue() * scale;

        EmbossFilter filter = new EmbossFilter();
        filter.setAzimuth(azimuth);
        filter.setElevation(elevation);
        filter.setBumpHeight(bumpHeight);
        filter.setEmboss(false);
        return filter;
    }
}
