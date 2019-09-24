package com.adlitteram.panel;

import com.adlitteram.util.Message;
import com.adlitteram.util.NumUtils;
import com.jhlabs.filter.CropFilterFast;
import com.jhlabs.image.AbstractBufferedImageOp;
import cz.autel.dmi.HIGConstraints;
import cz.autel.dmi.HIGLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class CropPanelFilter extends AbstractPanelFilter implements ChangeListener {

    //
    private JPanel panel;
    private JSpinner xField;
    private JSpinner yField;
    private JSpinner wField;
    private JSpinner hField;

    @Override
    public String getName() {
        return Message.get("CropFilter");
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireFilterListeners();
    }

    @Override
    public void reset() {
        xField.setValue(Integer.valueOf(0));
        yField.setValue(Integer.valueOf(0));
        wField.setValue(Integer.valueOf(0));
        hField.setValue(Integer.valueOf(0));
    }

    @Override
    public boolean isParametrable() {
        return true;
    }

    @Override
    public void setRoiShape(Shape shape) {
        if (shape != null) {
            Rectangle r = shape.getBounds();
            xField.setValue(Integer.valueOf(r.x));
            yField.setValue(Integer.valueOf(r.x));
            wField.setValue(Integer.valueOf(r.width));
            hField.setValue(Integer.valueOf(r.height));
        }
    }

    @Override
    public JPanel getPanel() {
        if (panel == null) {

            Integer min = Integer.valueOf(0);
            Integer max = Integer.valueOf(999999);
            Integer step = Integer.valueOf(1);

            xField = new JSpinner(new SpinnerNumberModel(Integer.valueOf(0), min, max, step));
            xField.addChangeListener(this);

            yField = new JSpinner(new SpinnerNumberModel(Integer.valueOf(0), min, max, step));
            yField.addChangeListener(this);

            wField = new JSpinner(new SpinnerNumberModel(Integer.valueOf(1), min, max, step));
            wField.addChangeListener(this);

            hField = new JSpinner(new SpinnerNumberModel(Integer.valueOf(1), min, max, step));
            hField.addChangeListener(this);

            int w[] = {10, 0, 5, 0, 5, 0, 10};
            int h[] = {10, 0, 5, 0, 5, 0, 5, 0, 10};
            HIGLayout l = new HIGLayout(w, h);
            HIGConstraints c = new HIGConstraints();
            //l.setColumnWeight(2, 1);

            panel = new JPanel(l);
            panel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), getName()));

            panel.add(new JLabel(Message.get("X") + " :"), c.xy(2, 2, "r"));
            panel.add(xField, c.xy(4, 2));
            panel.add(new JLabel(Message.get("Pixels")), c.xy(6, 2, "l"));

            panel.add(new JLabel(Message.get("Y") + " :"), c.xy(2, 4, "r"));
            panel.add(yField, c.xy(4, 4));
            panel.add(new JLabel(Message.get("Pixels")), c.xy(6, 4, "l"));

            panel.add(new JLabel(Message.get("Width") + " :"), c.xy(2, 6, "r"));
            panel.add(wField, c.xy(4, 6));
            panel.add(new JLabel(Message.get("Pixels")), c.xy(6, 6, "l"));

            panel.add(new JLabel(Message.get("Height") + " :"), c.xy(2, 8, "r"));
            panel.add(hField, c.xy(4, 8));
            panel.add(new JLabel(Message.get("Pixels")), c.xy(6, 8, "l"));
        }
        return panel;
    }

    @Override
    public AbstractBufferedImageOp getFilter(float scale) {
        int x = NumUtils.intValue(xField.getValue(), 0);
        int y = NumUtils.intValue(yField.getValue(), 0);
        int w = NumUtils.intValue(wField.getValue(), 0);
        int h = NumUtils.intValue(hField.getValue(), 0);
        //if ( w == 0 || h == 0 ) return new IdentityFilter();
        return new CropFilterFast(x, y, w, h);
    }
}
