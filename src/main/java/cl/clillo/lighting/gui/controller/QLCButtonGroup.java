package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.model.QLCScene;

import java.util.List;

public class QLCButtonGroup {

    private final int id;
    private final int idPanel;
    private final String name;
    private final List<QLCButton> buttonList;
    private final QLCScene globalOff;
    private int finalOffReview;

    public QLCButtonGroup(final int idPanel, final int id, final String name, final List<QLCButton> buttonList, final QLCScene globalOff) {
        this.idPanel = idPanel;
        this.id = id;
        this.name = name;
        this.buttonList = buttonList;
        this.globalOff = globalOff;
    }

    public List<QLCButton> getButtonList() {
        return buttonList;
    }

    public void addFinalOffReview() {
        this.finalOffReview++;
    }

    public int minusFinalOffReview() {
        return finalOffReview--;
    }

    public boolean isFinalOffReview(){
        return finalOffReview==0;
    }

    public QLCScene getGlobalOff() {
        return globalOff;
    }
}
