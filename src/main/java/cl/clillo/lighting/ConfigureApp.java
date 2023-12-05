package cl.clillo.lighting;

import cl.clillo.lighting.external.dmx.ArtNet;
import cl.clillo.lighting.gui.movements.EFXMConfigureJFrame;
import cl.clillo.lighting.model.ShowCollection;

public class ConfigureApp {

    private final EFXMConfigureJFrame efxmConfigureJFrame;

    public ConfigureApp() {
        //  ArtNet.setMode(ArtNet.ArtNetMode.DIRECT_ART_NET);
       //   ArtNet.setMode(ArtNet.ArtNetMode.HTTP_ART_NET);
        ArtNet.setMode(ArtNet.ArtNetMode.NON_ART_NET);

        ShowCollection.getInstance();

        efxmConfigureJFrame = new EFXMConfigureJFrame();

    }

    public void start(){
        efxmConfigureJFrame.start();
    }

    public static void main(String[] args) {
        final ConfigureApp configureApp = new ConfigureApp();
        configureApp.start();
    }
}
