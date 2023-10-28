package cl.clillo.lighting;

import cl.clillo.lighting.gui.virtualdj.VirtualDJFrame;

public class ConfigureVDJ {

    private final VirtualDJFrame virtualDJFrame;

    public ConfigureVDJ() {
        virtualDJFrame = new VirtualDJFrame();

    }

    public void start(){
        virtualDJFrame.start();
    }

    public static void main(String[] args) {
        final ConfigureVDJ configureApp = new ConfigureVDJ();
        configureApp.start();
    }
}
