package cl.clillo.lighting;

import cl.clillo.lighting.config.QLCFixtureBuilder;
import cl.clillo.lighting.dmx.ArtNet;
import cl.clillo.lighting.fixture.qlc.QLCFixture;
import cl.clillo.lighting.gui.EFXMConfigureJFrame;
import cl.clillo.lighting.model.QLCEfxCircle;
import cl.clillo.lighting.model.QLCEfxLine;
import cl.clillo.lighting.model.QLCEfxMultiLine;
import cl.clillo.lighting.model.QLCEfxSpline;
import cl.clillo.lighting.model.QLCModel;
import cl.clillo.lighting.model.QLCPoint;
import cl.clillo.lighting.model.QLCScene;
import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.model.ShowCollection;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigureApp {

    private final EFXMConfigureJFrame efxmConfigureJFrame;
    private final Scheduler scheduler;

    public ConfigureApp() throws ParserConfigurationException, IOException, SAXException {
        //  ArtNet.setMode(ArtNet.ArtNetMode.DIRECT_ART_NET);
        //  ArtNet.setMode(ArtNet.ArtNetMode.HTTP_ART_NET);
        ArtNet.setMode(ArtNet.ArtNetMode.NON_ART_NET);

        final QLCModel qlcModelOriginal = new QLCModel();
        final QLCFixtureBuilder qlcModel = new QLCFixtureBuilder(qlcModelOriginal.getFixtureModelList());
        final ShowCollection showCollection = ShowCollection.getInstance();
        showCollection.addFromDirectory(qlcModel);

        showCollection.addQLCScene (QLCScene.build(1, List.of(
                        QLCPoint.buildRoboticPoint(qlcModel.getFixture(101), QLCFixture.ChannelType.DIMMER, 255),
                        QLCPoint.buildRoboticPoint(qlcModel.getFixture(102), QLCFixture.ChannelType.DIMMER, 255),
                        //    QLCPoint.buildRoboticPoint(qlcModel.getFixture(101), QLCFixture.ChannelType.STROBE, 255),
                        //     QLCPoint.buildRoboticPoint(qlcModel.getFixture(102), QLCFixture.ChannelType.STROBE, 255),
                        QLCPoint.buildRoboticPoint(qlcModel.getFixture(201), QLCFixture.ChannelType.DIMMER, 128),
                        QLCPoint.buildRoboticPoint(qlcModel.getFixture(202), QLCFixture.ChannelType.DIMMER, 128),
                        QLCPoint.buildRoboticPoint(qlcModel.getFixture(203), QLCFixture.ChannelType.DIMMER, 128),
                        QLCPoint.buildRoboticPoint(qlcModel.getFixture(204), QLCFixture.ChannelType.DIMMER, 128),
                        QLCPoint.buildRoboticPoint(qlcModel.getFixture(301), QLCFixture.ChannelType.DIMMER, 128),
                        QLCPoint.buildRoboticPoint(qlcModel.getFixture(302), QLCFixture.ChannelType.DIMMER, 128),
                        QLCPoint.buildRoboticPoint(qlcModel.getFixture(303), QLCFixture.ChannelType.DIMMER, 128),
                        QLCPoint.buildRoboticPoint(qlcModel.getFixture(304), QLCFixture.ChannelType.DIMMER, 128))));

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
