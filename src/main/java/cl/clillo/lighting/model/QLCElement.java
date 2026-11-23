package cl.clillo.lighting.model;

import lombok.Getter;
import lombok.Setter;


public class QLCElement {

    @Getter
    protected final int id;
    @Getter
    protected final String type;
    @Getter
    protected final String name;
    @Getter
    protected final String path;
    @Setter
    @Getter
    protected boolean blackout;
    @Setter
    @Getter
    private boolean totalBlackout;
    private boolean initEventTrigger;

    public QLCElement(int id, String type, String name, String path) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.path = path;
        this.initEventTrigger = false;
    }

    protected int[] getDimmerChannels(){
        return new int[0];
    }

    public boolean isInitEventTrigger() {
        return initEventTrigger;
    }

    public void setInitEventTrigger(boolean initEventTrigger) {
        this.initEventTrigger = initEventTrigger;
    }
}
