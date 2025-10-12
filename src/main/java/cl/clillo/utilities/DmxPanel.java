package cl.clillo.utilities;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

public final class DmxPanel extends JPanel implements DmxModel.ModelListener {

    private final DmxModel model;
    private final UISettings settings;
    private final List<ChannelPanel> channelPanels = new ArrayList<>();

    public DmxPanel(DmxModel model, UISettings settings) {
        super(new WrapLayout(FlowLayout.LEFT, 12, 12));
        setBorder(new EmptyBorder(12,12,12,12));
        this.model = model;
        this.settings = settings;
        model.addListener(this);
        rebuild();
        settings.addPropertyChangeListener(evt -> {
            if ("scale".equals(evt.getPropertyName()) || "compact".equals(evt.getPropertyName())) {
                for (ChannelPanel cp : channelPanels) {
                    cp.applySettings(settings);
                }
                revalidate();
                repaint();
            }
        });

    }

    private void rebuild() {
        removeAll();
        channelPanels.clear();
        for (int i = 0; i < model.size(); i++) {
            ChannelPanel cp = new ChannelPanel(i, model, settings);
            channelPanels.add(cp);
            add(cp);
        }
        revalidate();
        repaint();
    }

    @Override public void valueChanged(int index, int newValue) {
        if (index >= 0 && index < channelPanels.size()) {
            channelPanels.get(index).setValueFromModel(newValue);
        }
    }

    public void allValuesChanged() {
        for (int i = 0; i < channelPanels.size(); i++) {
            channelPanels.get(i).setValueFromModel(model.get(i));
        }
    }

   public void structureChanged() {
        rebuild();
        settings.addPropertyChangeListener(evt -> {
            if ("scale".equals(evt.getPropertyName()) || "compact".equals(evt.getPropertyName())) {
                for (ChannelPanel cp : channelPanels) {
                    cp.applySettings(settings);
                }
                revalidate();
                repaint();
            }
        });
    }
}
