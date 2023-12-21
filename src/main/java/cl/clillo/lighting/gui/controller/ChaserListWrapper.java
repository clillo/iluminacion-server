package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.model.QLCChaserStep;

public class ChaserListWrapper {

    private final QLCChaserStep chaserStep;

    public ChaserListWrapper(final QLCChaserStep chaserStep) {
        this.chaserStep = chaserStep;
    }

    public QLCChaserStep getChaserStep() {
        return chaserStep;
    }

    @Override
    public String toString() {
        return chaserStep.getId()+"\t"+ chaserStep.getShow().getName()+"\t"+ (chaserStep.getHold()/1000.0)+ " sec.";
    }
}
