package com.adlitteram.panel;

import com.adlitteram.util.Message;
import com.jhlabs.filter.FlipFilterFast;
import com.jhlabs.filter.IdentityFilter;
import com.jhlabs.image.AbstractBufferedImageOp;
import cz.autel.dmi.HIGConstraints;
import cz.autel.dmi.HIGLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class FlipPanelFilter extends AbstractPanelFilter implements ChangeListener, ActionListener {

    //
    private JPanel panel;
    private JCheckBox horCheck;
    private JCheckBox verCheck;

    @Override
    public String getName() {
        return Message.get("FlipFilter");
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
        horCheck.setSelected(false);
        verCheck.setSelected(false);
    }

    @Override
    public boolean isParametrable() {
        return true;
    }

    @Override
    public JPanel getPanel() {
        if (panel == null) {

            horCheck = new JCheckBox(Message.get("Horizontal"), false);
            horCheck.addActionListener(this);

            verCheck = new JCheckBox(Message.get("Vertical"), false);
            verCheck.addActionListener(this);

            int w[] = {10, 0, 10};
            int h[] = {10, 0, 5, 0, 10};
            HIGLayout l = new HIGLayout(w, h);
            HIGConstraints c = new HIGConstraints();
            l.setColumnWeight(2, 1);

            panel = new JPanel(l);
            panel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), getName()));
            panel.add(horCheck, c.xy(2, 2));
            panel.add(verCheck, c.xy(2, 4));
        }

        return panel;
    }

    @Override
    public AbstractBufferedImageOp getFilter(float scale) {
        boolean h = horCheck.isSelected();
        boolean v = verCheck.isSelected();

        if (h && v) {
            return new FlipFilterFast(FlipFilterFast.FLIP_HV);
        }
        if (h) {
            return new FlipFilterFast(FlipFilterFast.FLIP_H);
        }
        if (v) {
            return new FlipFilterFast(FlipFilterFast.FLIP_V);
        }
        return new IdentityFilter();
    }
}
