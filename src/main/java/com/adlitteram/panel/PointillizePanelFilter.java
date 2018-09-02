
package com.adlitteram.panel;

import com.adlitteram.jasmin.color.ColorPalette;
import com.adlitteram.jasmin.color.NamedColor;
import com.adlitteram.jasmin.gui.combo.ColorCombo;
import com.adlitteram.jasmin.gui.widget.FloatJSpinSlider;
import com.adlitteram.jasmin.gui.widget.JSpinSlider;
import com.adlitteram.util.Message;
import com.jhlabs.image.AbstractBufferedImageOp;
import com.jhlabs.image.PointillizeFilter;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class PointillizePanelFilter extends AbstractPanelFilter implements ChangeListener, ActionListener {
    //
    private static final String[] gridTypeArray = {
        Message.get("Random"), Message.get("Square"), Message.get("Hexagon"),
        Message.get("Octogon"), Message.get("Triangle")
    };
    //
    private JPanel panel;
    private FloatJSpinSlider sizeSlider;
    private JSpinSlider angleSlider;
    private JSpinSlider randomSlider;
    private JSpinSlider dotSizeSlider;
    private JSpinSlider fuzzinessSlider;
    private JComboBox gridTypeCombo;
    private JCheckBox fadeEdgesCheck;
    private ColorCombo edgeColorCombo;
    //
    private final float defaultSize = 25f;
    private final int defaultAngle = 0;
    private final int defaultRandomness = 0;
    private final int defaultDotSize = 100;
    private final int defaultFuzziness = 0;
    private final int defaultGridType = 2;
    private final boolean defaultFadeEdges = false;
    private final NamedColor defaultEdgeColor = NamedColor.BLACK;
    //
    private final ColorPalette colorPalette;

    public PointillizePanelFilter(ColorPalette colorPalette) {
        this.colorPalette = colorPalette;
    }

    @Override
    public String getName() {
        return Message.get("PointillizeFilter");
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireFilterListeners();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        dotSizeSlider.setEnabled(!fadeEdgesCheck.isSelected());
        fuzzinessSlider.setEnabled(!fadeEdgesCheck.isSelected());
        edgeColorCombo.setEnabled(!fadeEdgesCheck.isSelected());
        randomSlider.setEnabled(gridTypeCombo.getSelectedIndex() != PointillizeFilter.RANDOM);
        fireFilterListeners();
    }

    @Override
    public boolean isParametrable() {
        return true;
    }

    @Override
    public void reset() {
        sizeSlider.setFloatValue(defaultSize);
        angleSlider.setValue(defaultAngle);
        randomSlider.setValue(defaultRandomness);
        dotSizeSlider.setValue(defaultDotSize);
        fuzzinessSlider.setValue(defaultFuzziness);
        gridTypeCombo.setSelectedIndex(defaultGridType);
        fadeEdgesCheck.setSelected(defaultFadeEdges);
        edgeColorCombo.setSelectedItem(defaultEdgeColor);
    }

    @Override
    public JPanel getPanel() {
        if (panel == null) {

            gridTypeCombo = new JComboBox(gridTypeArray);
            gridTypeCombo.setSelectedIndex(defaultGridType);
            gridTypeCombo.addActionListener(this);

            edgeColorCombo = new ColorCombo(colorPalette);
            edgeColorCombo.setSelectedItem(defaultEdgeColor);
            edgeColorCombo.addActionListener(this);

            fadeEdgesCheck = new JCheckBox(Message.get("FadeEgdes"), defaultFadeEdges);
            fadeEdgesCheck.addActionListener(this);

            int w0[] = {0, 0, 5, 0, 20, 0, 5, 0, 0};
            int h0[] = {5, 0, 15, 0, 0};
            HIGLayout l0 = new HIGLayout(w0, h0);
            HIGConstraints c0 = new HIGConstraints();
            l0.setColumnWeight(5, 1);

            JPanel p0 = new JPanel(l0);
            p0.add(new JLabel(Message.get("EdgeColor")), c0.xy(2, 2, "r"));
            p0.add(edgeColorCombo, c0.xy(4, 2));
            p0.add(fadeEdgesCheck, c0.xy(6, 2, "l"));
            p0.add(new JLabel(Message.get("GridType")), c0.xy(2, 4, "r"));
            p0.add(gridTypeCombo, c0.xy(4, 4));

            sizeSlider = new FloatJSpinSlider(defaultSize, 0f, 100f, 1f, 1f, 10);
            sizeSlider.addChangeListener(this);

            angleSlider = new JSpinSlider(defaultAngle, 0, 360, 1, 90, 10);
            angleSlider.addChangeListener(this);

            randomSlider = new JSpinSlider(defaultRandomness, 0, 100, 1, 50, 10);
            randomSlider.setEnabled(defaultGridType != PointillizeFilter.RANDOM);
            randomSlider.addChangeListener(this);

            dotSizeSlider = new JSpinSlider(defaultDotSize, 0, 100, 1, 1, 10);
            dotSizeSlider.setEnabled(!defaultFadeEdges);
            dotSizeSlider.addChangeListener(this);

            fuzzinessSlider = new JSpinSlider(defaultFuzziness, 0, 100, 1, 1, 10);
            fuzzinessSlider.setEnabled(!defaultFadeEdges);
            fuzzinessSlider.addChangeListener(this);

            int w[] = {10, 0, 10};
            int h[] = {10, 0, 0, 10, 0, 0, 10, 0, 0, 10, 0, 0, 10, 0, 0, 10, 0, 0, 10};
            HIGLayout l = new HIGLayout(w, h);
            HIGConstraints c = new HIGConstraints();
            l.setColumnWeight(2, 1);

            panel = new JPanel(l);
            panel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), getName()));
            panel.add(new JLabel(Message.get("Size") + " :"), c.xy(2, 2, "l"));
            panel.add(sizeSlider, c.xy(2, 3));
//            panel.add(new JLabel(Message.get("Angle") + " :"), c.xy(2, 5, "l"));
//            panel.add(angleSlider, c.xy(2, 6));
            panel.add(new JLabel(Message.get("Randomness") + " :"), c.xy(2, 8, "l"));
            panel.add(randomSlider, c.xy(2, 9));
            panel.add(new JLabel(Message.get("DotSize") + " :"), c.xy(2, 11, "l"));
            panel.add(dotSizeSlider, c.xy(2, 12));
            panel.add(new JLabel(Message.get("Fuzziness") + " :"), c.xy(2, 14, "l"));
            panel.add(fuzzinessSlider, c.xy(2, 15));

            panel.add(p0, c.xy(2, 17));
        }

        return panel;
    }

    @Override
    public AbstractBufferedImageOp getFilter(float scale) {

        float size = sizeSlider.getFloatValue() * scale;
        float angle = (float) (angleSlider.getValue() * Math.PI / 180.0d);
        float dotSize = (float) (dotSizeSlider.getValue() / 100f);
        float randomness = (float) (randomSlider.getValue() / 100f);
        float fuzziness = (float) (fuzzinessSlider.getValue() / 100f);
        boolean fadeEdges = fadeEdgesCheck.isSelected();
        int gridType = gridTypeCombo.getSelectedIndex();
        int edgeColor = ((NamedColor) edgeColorCombo.getSelectedItem()).getRGB();

        PointillizeFilter filter = new PointillizeFilter();
        filter.setScale(size);
        filter.setAngle(angle);
        filter.setRandomness(randomness);
        filter.setEdgeThickness(dotSize);
        filter.setFuzziness(fuzziness);
        filter.setFadeEdges(fadeEdges);
        filter.setGridType(gridType);
        filter.setEdgeColor(edgeColor);
        return filter;
    }
}
