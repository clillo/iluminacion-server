package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.model.QLCScene;

import java.util.List;

public class ButtonGroup {

    private final int id;
    private final String name;
    private final List<QLCButton> buttonList;
    private QLCScene globalOff;
    private int finalOffReview;

    public ButtonGroup(final int id, final String name, final List<QLCButton> buttonList) {
        this.id = id;
        this.name = name;
        this.buttonList = buttonList;
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

    public void setGlobalOff(QLCScene globalOff) {
        this.globalOff = globalOff;
    }

    public int getId() {
        return id;
    }
}
