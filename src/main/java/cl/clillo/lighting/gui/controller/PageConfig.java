package cl.clillo.lighting.gui.controller;

import java.util.ArrayList;
import java.util.List;

public class PageConfig {

    private final List<ButtonGroup> bankAGroups;
    private final List<ButtonGroup> bankBGroups;

    public PageConfig(final List<ButtonGroup> buttonGroups) {
        this.bankAGroups = buttonGroups != null ? buttonGroups : new ArrayList<>();
        this.bankBGroups = new ArrayList<>();
    }

    public List<ButtonGroup> getButtonGroups() {
        return bankAGroups;
    }

    public List<ButtonGroup> getButtonGroups(final int bank) {
        return bank == cl.clillo.lighting.external.midi.MidiShiftBank.BANK_B ? bankBGroups : bankAGroups;
    }
}
