
package com.adlitteram.panel;

import com.adlitteram.jasmin.gui.widget.JSpinSlider;
import com.adlitteram.util.Message;
import com.jhlabs.image.AbstractBufferedImageOp;
import com.jhlabs.image.SharpenFilter;
import cz.autel.dmi.HIGConstraints;
import cz.autel.dmi.HIGLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SharpenPanelFilter extends AbstractPanelFilter implements ChangeListener {
    //
    private JPanel panel;
    private JSpinSlider iterationSlider;

    @Override
    public String getName() {
        return Message.get("SharpenFilter");
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireFilterListeners();
    }

    @Override
    public void reset() {
        iterationSlider.setValue(0);
    }

    @Override
    public boolean isParametrable() {
        return true;
    }

    @Override
    public JPanel getPanel() {
        if (panel == null) {

            iterationSlider = new JSpinSlider(0, 0, 10, 1, 10, 10);
            iterationSlider.addChangeListener(this);

            int w[] = {10, 0, 10};
            int h[] = {10, 0, 0, 10};
            HIGLayout l = new HIGLayout(w, h);
            HIGConstraints c = new HIGConstraints();
            l.setColumnWeight(2, 1);

            panel = new JPanel(l);
            panel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), getName()));
            panel.add(new JLabel(Message.get("Iteration") + " :"), c.xy(2, 2, "l"));
            panel.add(iterationSlider, c.xy(2, 3));
        }
        return panel;
    }

    @Override
    public AbstractBufferedImageOp getFilter(float scale) {
        int iteration = iterationSlider.getValue();

        SharpenFilter filter = new SharpenFilter();
        filter.setIteration(iteration);
        return filter;
    }
}
