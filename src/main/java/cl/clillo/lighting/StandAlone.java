package cl.clillo.lighting;

import cl.clillo.lighting.config.QLCFixtureBuilder;
import cl.clillo.lighting.dmx.ArtNet;
import cl.clillo.lighting.midi.KeyData;
import cl.clillo.lighting.midi.MidiEvent;
import cl.clillo.lighting.model.QLCFunction;
import cl.clillo.lighting.model.QLCModel;
import cl.clillo.lighting.model.QLCPoint;
import cl.clillo.lighting.model.QLCSequence;
import cl.clillo.lighting.model.QLCStep;
import cl.clillo.lighting.model.Show;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StandAlone implements MidiEvent {

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
      //  ArtNet.setMode(ArtNet.ArtNetMode.DIRECT_ART_NET);
      //  ArtNet.setMode(ArtNet.ArtNetMode.HTTP_ART_NET);
        ArtNet.setMode(ArtNet.ArtNetMode.NON_ART_NET);

        final List<Show> showList = new ArrayList<>();
        final QLCStep step1 = QLCStep.builder().id(1)
                .pointList(List.of(QLCPoint.builder().dmxChannel(419).data(255).build(),
                        QLCPoint.builder().dmxChannel(421).data(255).build()))
                .hold(500)
                .build();

        final QLCStep step2 = QLCStep.builder().id(2)
                .pointList(List.of(QLCPoint.builder().dmxChannel(419).data(255).build(),
                        QLCPoint.builder().dmxChannel(421).data(0).build()))
                .hold(500)
                .build();

        final QLCFunction qlcSequence = QLCSequence.builder()
                .id(1)
                .addStepList(step1)
                .addStepList(step2)
                .type("Sequence")
                .build();

        final QLCModel qlcModelOriginal = new QLCModel();
        final QLCFixtureBuilder qlcModel = new QLCFixtureBuilder(qlcModelOriginal.getFixtureModelList());

     //   final QLCEfxCircle qlcEfxCircle = QLCEfxCircle.read(qlcModel, "QLCEfxCircle.circle1.xml");
        //qlcEfxCircle.writeToConfigFile();

    }

    @Override
    public void onKeyPress(final KeyData keyData) {
        System.out.println("Key press: "+keyData);
    }

    @Override
    public void onKeyRelease(final KeyData keyData) {
        System.out.println("Key release: "+keyData);
    }

    @Override
    public void onSlide(KeyData keyData) {
        System.out.println("Slide: "+keyData);
    }
}
