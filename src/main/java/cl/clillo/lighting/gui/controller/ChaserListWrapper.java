package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.model.ChaserStep;

public class ChaserListWrapper {

    private final ChaserStep chaserStep;

    public ChaserListWrapper(final ChaserStep chaserStep) {
        this.chaserStep = chaserStep;
    }

    public ChaserStep getChaserStep() {
        return chaserStep;
    }

    @Override
    public String toString() {
        return chaserStep.getId()+"\t"+ chaserStep.getShow().getName()+"\t"+ (chaserStep.getHold()/1000.0)+ " sec.";
    }
}
