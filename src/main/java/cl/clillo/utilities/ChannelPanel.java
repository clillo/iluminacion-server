package cl.clillo.utilities;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

public final class ChannelPanel extends JPanel {
    private final int index;
    private final DmxModel model;
    private final JSlider slider;
    private final JSpinner spinner;
    private final JLabel title;
    private final JProgressBar bar;
    private UISettings settings;

    public ChannelPanel(int index, DmxModel model, UISettings settings) {
        this.index = index;
        this.model = model;
        this.settings = settings;

        setLayout(new BorderLayout(6,6));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDDDDDD)),
                new EmptyBorder(6,6,6,6)
        ));

        title = new JLabel("CH " + (index + 1), SwingConstants.CENTER);
        add(title, BorderLayout.NORTH);

        slider = new JSlider(SwingConstants.VERTICAL, 0, 255, model.get(index));
        slider.setMajorTickSpacing(64);
        slider.setMinorTickSpacing(8);
        slider.setPaintTicks(true);
        add(slider, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        spinner = new JSpinner(new SpinnerNumberModel(model.get(index), 0, 255, 1));
        // compactar spinner
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.NumberEditor ne) {
            ne.getTextField().setColumns(3);
        }
        spinner.setPreferredSize(new Dimension(50, 22));

        bar = new JProgressBar(0,255);
        bar.setValue(model.get(index));
        bottom.add(new JLabel("Val:"));
        bottom.add(spinner);
        bottom.add(bar);
        add(bottom, BorderLayout.SOUTH);

        // Sync slider -> model
        slider.addChangeListener(e -> {
            int v = slider.getValue();
            if (!spinner.getValue().equals(v)) spinner.setValue(v);
            bar.setValue(v);
            if (!slider.getValueIsAdjusting()) {
                model.set(index, v);
            }
        });
        // Sync spinner -> model
        spinner.addChangeListener(e -> {
            int v = (Integer) spinner.getValue();
            if (slider.getValue() != v) slider.setValue(v);
            bar.setValue(v);
            model.set(index, v);
        });

        // Mouse wheel para ajustes finos (Shift = pasos de 10)
        addMouseWheelListener(e -> {
            int delta = -e.getWheelRotation();
            int v = Math.max(0, Math.min(255, slider.getValue() + (e.isShiftDown() ? delta*10 : delta)));
            slider.setValue(v);
        });

        // aplicar configuración inicial
        applySettings(this.settings);
        setTitleFromModel();
    }

    void setValueFromModel(int v) {
        if (slider.getValue() != v) slider.setValue(v);
        if (!((Integer)spinner.getValue()).equals(v)) spinner.setValue(v);
    }

    /** Aplica zoom y modo compacto. */
    void applySettings(UISettings settings) {
        this.settings = settings;
        double s = settings.getScale();
        boolean compact = settings.isCompact();

        int pad = (int) Math.round((compact ? 6 : 8) * s);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDDDDDD)),
                new EmptyBorder(pad, pad, pad, pad)
        ));

        float titlePt = (float) ((compact ? 11f : 12f) * s);
        title.setFont(title.getFont().deriveFont(Font.BOLD, titlePt));

        int sliderW = (int) Math.round((compact ? 36 : 48) * s);
        int sliderH = (int) Math.round((compact ? 160 : 200) * s);
        slider.setPreferredSize(new Dimension(sliderW, sliderH));

        // Spinner sizing
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.NumberEditor ne) {
            ne.getTextField().setColumns(compact ? 3 : 4);
            ne.getTextField().setFont(ne.getTextField().getFont().deriveFont((float) (ne.getTextField().getFont().getSize2D() * s)));
        }
        spinner.setPreferredSize(new Dimension(
                (int)Math.round((compact ? 50 : 60) * s),
                (int)Math.round((compact ? 22 : 24) * s)
        ));

        int barW = (int) Math.round((compact ? 60 : 80) * s);
        int barH = (int) Math.round((compact ? 10 : 12) * s);
        bar.setPreferredSize(new Dimension(barW, barH));

        revalidate();
        repaint();
    }

    void setTitleFromModel() {
        String desc = safe(model.getDescription(index));
        title.setText("<html><div style='text-align:center'>CH " + (index + 1)
                + "<br><span style='font-size:smaller'>" + desc + "</span></div></html>");
    }

    private static String safe(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }
}
