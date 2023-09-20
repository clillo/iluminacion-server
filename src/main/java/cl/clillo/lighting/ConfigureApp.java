package cl.clillo.lighting;

import cl.clillo.lighting.config.QLCFixtureBuilder;
import cl.clillo.lighting.dmx.ArtNet;
import cl.clillo.lighting.gui.EFXMConfigureJFrame;
import cl.clillo.lighting.model.QLCModel;
import cl.clillo.lighting.model.ShowCollection;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class ConfigureApp {

    private final EFXMConfigureJFrame efxmConfigureJFrame;
    private final Scheduler scheduler;

    public ConfigureApp() {
        //  ArtNet.setMode(ArtNet.ArtNetMode.DIRECT_ART_NET);
        //  ArtNet.setMode(ArtNet.ArtNetMode.HTTP_ART_NET);
        ArtNet.setMode(ArtNet.ArtNetMode.NON_ART_NET);

        final QLCModel qlcModelOriginal = new QLCModel();
        final QLCFixtureBuilder qlcModel = new QLCFixtureBuilder(qlcModelOriginal.getFixtureModelList());
        final ShowCollection showCollection = ShowCollection.getInstance();
        showCollection.addFromDirectory(qlcModel);

        efxmConfigureJFrame = new EFXMConfigureJFrame();
        scheduler = new Scheduler();
    }

    public void start(){
      //  final MidiHandler midiHandler = new MidiHandler(standAlone);
        scheduler.start();
        efxmConfigureJFrame.start();
    }

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        final ConfigureApp configureApp = new ConfigureApp();
        configureApp.start();
    }
}
