package com.adlitteram.panel;

import com.adlitteram.util.Message;
import cz.autel.dmi.HIGConstraints;
import cz.autel.dmi.HIGLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class InternalCropPanelFilter extends IdentityPanelFilter {

    //
    private JPanel panel;
    private JTextField xField;
    private JTextField yField;
    private JTextField wField;
    private JTextField hField;

    @Override
    public String getName() {
        return Message.get("CropFilter");
    }

    @Override
    public void setRoiShape(Shape shape) {
        if (shape != null) {
            Rectangle r = shape.getBounds();
            xField.setText(String.valueOf(r.x));
            yField.setText(String.valueOf(r.x));
            wField.setText(String.valueOf(r.width));
            hField.setText(String.valueOf(r.height));
        }
    }

    @Override
    public JPanel getPanel() {
        if (panel == null) {

            xField = new JTextField(10);
            xField.setEditable(false);

            yField = new JTextField(10);
            yField.setEditable(false);

            wField = new JTextField(10);
            wField.setEditable(false);

            hField = new JTextField(10);
            hField.setEditable(false);

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

}
