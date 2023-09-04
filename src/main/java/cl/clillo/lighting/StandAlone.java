package cl.clillo.lighting;

import cl.clillo.lighting.config.QLCFixtureBuilder;
import cl.clillo.lighting.dmx.ArtNet;
import cl.clillo.lighting.fixture.qlc.QLCFixture;
import cl.clillo.lighting.midi.KeyData;
import cl.clillo.lighting.midi.MidiEvent;
import cl.clillo.lighting.model.QLCDirection;
import cl.clillo.lighting.model.QLCEfx;
import cl.clillo.lighting.model.QLCEfxCircle;
import cl.clillo.lighting.model.QLCEfxFixtureData;
import cl.clillo.lighting.model.QLCEfxLine;
import cl.clillo.lighting.model.QLCEfxMultiLine;
import cl.clillo.lighting.model.QLCEfxSpline;
import cl.clillo.lighting.model.QLCFunction;
import cl.clillo.lighting.model.QLCModel;
import cl.clillo.lighting.model.QLCPoint;
import cl.clillo.lighting.model.QLCRunOrder;
import cl.clillo.lighting.model.QLCScene;
import cl.clillo.lighting.model.QLCSequence;
import cl.clillo.lighting.model.QLCStep;
import cl.clillo.lighting.model.RealPoint;
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

        final QLCEfxCircle qlcEfxCircle = QLCEfxCircle.read(qlcModel, "QLCEfxCircle.circle1.xml");
        //qlcEfxCircle.writeToConfigFile();

        final QLCEfxLine qlcEfxLine = new QLCEfxLine(1,null,null,null,null,null,null,null,new ArrayList<>());
        qlcEfxLine.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(201)).build());
 //       qlcEfxLine.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(202)).startOffset(0.25).reverse().build());
   //     qlcEfxLine.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(203)).startOffset(50).build());
     //   qlcEfxLine.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(204)).startOffset(0.75).reverse().build());
        qlcEfxLine.updateParameters(60416.0, 45001.0, 45248.0, 51336.0);

       final QLCEfxMultiLine qlcEfxMultiLine = new QLCEfxMultiLine(0, "type", "name", "path",
            QLCDirection.FORWARD, QLCRunOrder.LOOP, new ArrayList<>(), null, new ArrayList<>());
        qlcEfxMultiLine.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(201)).build());
     //   qlcEfxMultiLine.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(202)).startOffset(25).reverse().build());
      //  qlcEfxMultiLine.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(203)).startOffset(50).build());
      //  qlcEfxMultiLine.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(204)).startOffset(75).reverse().build());

        qlcEfxMultiLine.updateParameters(
                RealPoint.builder().x(45248.0).y(45001.0).build(),
                RealPoint.builder().x(60416.0).y(60220.0).build());

        final QLCEfxSpline qlcEfxSpline = new QLCEfxSpline(0, "type", "name", "path",
                QLCDirection.FORWARD, QLCRunOrder.LOOP, new ArrayList<>(), null, new ArrayList<>());
        qlcEfxSpline.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(201)).build());
      //  qlcEfxSpline.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(19)).startOffset(25).reverse().build());
     //   qlcEfxSpline.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(19)).startOffset(50).build());
     //   qlcEfxSpline.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(19)).startOffset(75).reverse().build());
        qlcEfxSpline.updateParameters(List.of(
                RealPoint.builder().x(56128.0).y(56360.0).build(),
                RealPoint.builder().x(49472.0).y(60730.0).build(),
                RealPoint.builder().x(44096.0).y(55487.0).build(),
                RealPoint.builder().x(55296.0).y(43326.0).build(),
                RealPoint.builder().x(48448.0).y(39612.0).build(),
                RealPoint.builder().x(43264.0).y(42962.0).build(),
                RealPoint.builder().x(53760.0).y(52137.0).build()
                ));
        final QLCEfx qlcFunction = qlcEfxCircle;
    //  final QLCEfx qlcFunction = qlcEfxLine;
      //  final QLCEfx qlcFunction = qlcEfxMultiLine;
    //    final QLCEfx qlcFunction = qlcEfxSpline;

        final Show movingPositions = Show.builder()
                .name("bouncing-auto")
                .executing(true)
                .firstTimeExecution(true)
                .stepList(List.of())
               // .function((QLCSequence)qlcSequence)
              //  .function(qlcModel.getFunction(61))
               // .function(qlcModel.getFunction(10))
                .function(qlcFunction)
                .build();


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
