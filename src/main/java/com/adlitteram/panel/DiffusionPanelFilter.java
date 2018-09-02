
package com.adlitteram.panel;

import com.adlitteram.util.Message;
import com.adlitteram.util.NumUtils;
import com.jhlabs.image.AbstractBufferedImageOp;
import com.jhlabs.image.DiffusionFilter;
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

public class DiffusionPanelFilter extends AbstractPanelFilter implements ActionListener {
    //
    private JPanel panel;
    private JComboBox ditherCombo;
    private JCheckBox serpentineCheck;
    private JCheckBox colorCheck;
    private static final String[] ditherArray = {"2", "4", "5", "6", "8", "16", "64", "256"};

    @Override
    public String getName() {
        return Message.get("DiffusionFilter");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        fireFilterListeners();
    }

    @Override
    public void reset() {
        ditherCombo.setSelectedItem("8");
        serpentineCheck.setSelected(true);
        colorCheck.setSelected(true);
    }

    @Override
    public boolean isParametrable() {
        return true;
    }

    @Override
    public JPanel getPanel() {
        if (panel == null) {

            ditherCombo = new JComboBox(ditherArray);
            ditherCombo.setSelectedItem("8");
            ditherCombo.addActionListener(this);

            serpentineCheck = new JCheckBox(Message.get("Serpentine"), true);
            serpentineCheck.addActionListener(this);

            colorCheck = new JCheckBox(Message.get("ColorDither"), true);
            colorCheck.addActionListener(this);

            int w[] = {10, 0, 5, 0, 10};
            int h[] = {10, 0, 10, 0, 5, 0, 10};
            HIGLayout l = new HIGLayout(w, h);
            HIGConstraints c = new HIGConstraints();
            l.setColumnWeight(2, 1);
            l.setColumnWeight(4, 1);

            panel = new JPanel(l);
            panel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), getName()));
            panel.add(new JLabel(Message.get("Levels") + " :"), c.xy(2, 2, "r"));
            panel.add(ditherCombo, c.xy(4, 2, "l"));
            panel.add(serpentineCheck, c.xy(4, 4, "l"));
            panel.add(colorCheck, c.xy(4, 6, "l"));
        }
        return panel;
    }

    @Override
    public AbstractBufferedImageOp getFilter(float scale) {
        int level = NumUtils.intValue(ditherCombo.getSelectedItem());
        boolean serpentine = serpentineCheck.isSelected();
        boolean color = colorCheck.isSelected();

        DiffusionFilter filter = new DiffusionFilter();
        filter.setLevels(level);
        filter.setColorDither(color);
        filter.setSerpentine(serpentine);
        return filter;
    }
}
