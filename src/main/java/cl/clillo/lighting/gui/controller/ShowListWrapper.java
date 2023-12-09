package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.model.Show;

public class ShowListWrapper {

    private final Show show;

    public ShowListWrapper(final Show show) {
        this.show = show;
    }

    public Show getShow() {
        return show;
    }

    @Override
    public String toString() {
        return show.getId()+"\t"+show.getFunction().getName();
    }
}
