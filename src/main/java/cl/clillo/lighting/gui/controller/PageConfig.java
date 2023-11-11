package cl.clillo.lighting.gui.controller;

import java.util.List;

public class PageConfig {

    private final List<ButtonGroup> buttonGroups;

    public PageConfig(final List<ButtonGroup> buttonGroups) {
        this.buttonGroups = buttonGroups;
    }

    public List<ButtonGroup> getButtonGroups() {
        return buttonGroups;
    }
}
