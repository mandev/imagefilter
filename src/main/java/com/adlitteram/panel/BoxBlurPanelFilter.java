package com.adlitteram.panel;

import com.adlitteram.jasmin.gui.widget.JSpinSlider;
import com.adlitteram.util.Message;
import com.jhlabs.image.AbstractBufferedImageOp;
import com.jhlabs.image.BoxBlurFilter;
import cz.autel.dmi.HIGConstraints;
import cz.autel.dmi.HIGLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class BoxBlurPanelFilter extends AbstractPanelFilter implements ChangeListener {

    private JPanel panel;
    private JSpinSlider hRadiusSlider;
    private JSpinSlider vRadiusSlider;
    private JSpinSlider iterateSlider;

    @Override
    public String getName() {
        return Message.get("BoxBlurFilter");
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireFilterListeners();
    }

    @Override
    public void reset() {
        hRadiusSlider.setValue(3);
        vRadiusSlider.setValue(3);
        iterateSlider.setValue(1);
    }

    @Override
    public boolean isParametrable() {
        return true;
    }

    @Override
    public JPanel getPanel() {
        if (panel == null) {

            hRadiusSlider = new JSpinSlider(3, 0, 30, 1, 5, 10);
            hRadiusSlider.addChangeListener(this);

            vRadiusSlider = new JSpinSlider(3, 0, 30, 1, 5, 10);
            vRadiusSlider.addChangeListener(this);

            iterateSlider = new JSpinSlider(1, 0, 10, 1, 1, 10);
            iterateSlider.getSlider().setSnapToTicks(true);
            iterateSlider.addChangeListener(this);

            int w[] = {10, 0, 10};
            int h[] = {10, 0, 0, 10, 0, 0, 10, 0, 0, 10};
            HIGLayout l = new HIGLayout(w, h);
            HIGConstraints c = new HIGConstraints();
            l.setColumnWeight(2, 1);

            panel = new JPanel(l);
            panel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), getName()));
            panel.add(new JLabel(Message.get("HorizontalRadius") + " :"), c.xy(2, 2, "l"));
            panel.add(hRadiusSlider, c.xy(2, 3));
            panel.add(new JLabel(Message.get("VerticalRadius") + " :"), c.xy(2, 5, "l"));
            panel.add(vRadiusSlider, c.xy(2, 6));
            panel.add(new JLabel(Message.get("Iteration") + " :"), c.xy(2, 8, "l"));
            panel.add(iterateSlider, c.xy(2, 9));
        }
        return panel;
    }

    @Override
    public AbstractBufferedImageOp getFilter(float scale) {
        float hradius = (float) hRadiusSlider.getValue() * scale;
        float vradius = (float) vRadiusSlider.getValue() * scale;
        int iteration = iterateSlider.getValue();

        return new BoxBlurFilter(hradius, vradius, iteration);
    }
}
