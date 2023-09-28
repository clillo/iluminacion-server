package cl.clillo.lighting;

import cl.clillo.lighting.dmx.ArtNet;
import cl.clillo.lighting.gui.controller.ControllerJFrame;

public class StandAlone {

    public static void main(String[] args) {
      //  ArtNet.setMode(ArtNet.ArtNetMode.DIRECT_ART_NET);
      //  ArtNet.setMode(ArtNet.ArtNetMode.HTTP_ART_NET);
        ArtNet.setMode(ArtNet.ArtNetMode.NON_ART_NET);

        final ControllerJFrame controllerJFrame = new ControllerJFrame();
        controllerJFrame.start();

    }
}
