package cl.clillo.lighting;

import cl.clillo.lighting.external.dmx.ArtNet;
import cl.clillo.lighting.gui.movements.FixtureGroupSelectionFrame;
import cl.clillo.lighting.model.ShowCollection;

public class ConfigureApp {

    private final FixtureGroupSelectionFrame groupSelectionFrame;

    public ConfigureApp() {
        //  ArtNet.setMode(ArtNet.ArtNetMode.DIRECT_ART_NET);
       //   ArtNet.setMode(ArtNet.ArtNetMode.HTTP_ART_NET);
        ArtNet.setMode(ArtNet.ArtNetMode.NON_ART_NET);

        ShowCollection.getInstance();

        groupSelectionFrame = new FixtureGroupSelectionFrame();

    }

    public void start(){
        groupSelectionFrame.start();
    }

    public static void main(String[] args) {
        final ConfigureApp configureApp = new ConfigureApp();
        configureApp.start();
    }
}
