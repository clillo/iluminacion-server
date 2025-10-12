package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.model.QLCScene;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class ButtonGroup {

    @Getter
    private final int id;
    @Getter
    private final List<QLCButton> buttonList;
    @Setter
    @Getter
    private QLCScene globalOff;
    private int finalOffReview;

    public ButtonGroup(final int id, final String name, final List<QLCButton> buttonList) {
        this.id = id;
        this.buttonList = buttonList;
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

}
