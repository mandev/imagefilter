
package com.adlitteram.panel;

import com.adlitteram.jasmin.gui.widget.FloatJSpinSlider;
import com.adlitteram.jasmin.gui.widget.JSpinSlider;
import com.adlitteram.util.Message;

import com.jhlabs.image.AbstractBufferedImageOp;
import com.jhlabs.image.CrystallizeFilter;
import cz.autel.dmi.HIGConstraints;
import cz.autel.dmi.HIGLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class CrystallizePanelFilter extends AbstractPanelFilter implements ChangeListener {

    private JPanel panel;
    private FloatJSpinSlider scaleSlider;
    private JSpinSlider angleSlider;
    private JSpinSlider randomSlider;
    private JSpinSlider edgeWidthSlider;

    @Override
    public String getName() {
        return Message.get("Crystallize");
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireFilterListeners();
    }

    @Override
    public void reset() {
        scaleSlider.setFloatValue(32);
        angleSlider.setValue(0);
        randomSlider.setValue(0);
        edgeWidthSlider.setValue(40);
    }

    @Override
    public boolean isParametrable() {
        return true;
    }

    @Override
    public JPanel getPanel() {
        if (panel == null) {

            scaleSlider = new FloatJSpinSlider(32, 1, 100, 1, 50, 10);
            scaleSlider.addChangeListener(this);

            angleSlider = new JSpinSlider(0, 0, 360, 1, 90, 10);
            angleSlider.addChangeListener(this);

            randomSlider = new JSpinSlider(0, 0, 100, 1, 50, 10);
            randomSlider.addChangeListener(this);

            edgeWidthSlider = new JSpinSlider(40, 0, 100, 1, 50, 10);
            edgeWidthSlider.addChangeListener(this);

            int w[] = {10, 0, 10};
            int h[] = {10, 0, 0, 10, 0, 0, 10, 0, 0, 10, 0, 0, 10, 0, 0, 10};
            HIGLayout l = new HIGLayout(w, h);
            HIGConstraints c = new HIGConstraints();
            l.setColumnWeight(2, 1);

            panel = new JPanel(l);
            panel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), getName()));
            panel.add(new JLabel(Message.get("Size") + " :"), c.xy(2, 2, "l"));
            panel.add(scaleSlider, c.xy(2, 3));
//            panel.add(new JLabel(Message.get("Angle") + " :"), c.xy(2, 5, "l"));
//            panel.add(angleSlider, c.xy(2, 6));
            panel.add(new JLabel(Message.get("Randomness") + " :"), c.xy(2, 8, "l"));
            panel.add(randomSlider, c.xy(2, 9));
            panel.add(new JLabel(Message.get("EdgeWidth") + " :"), c.xy(2, 11, "l"));
            panel.add(edgeWidthSlider, c.xy(2, 12));
        }
        return panel;
    }

    @Override
    public AbstractBufferedImageOp getFilter(float scale) {
        float size = scaleSlider.getFloatValue() * scale;
        float angle = (float) (angleSlider.getValue() * Math.PI / 180.0d);
        float random = (float) randomSlider.getValue() / 100;
        float width = (float) edgeWidthSlider.getValue() / 100f;

        CrystallizeFilter filter = new CrystallizeFilter();
        filter.setScale(size);
        filter.setAngle(angle);
        filter.setRandomness(random);
        filter.setEdgeThickness(width);
        return filter;
    }
}
