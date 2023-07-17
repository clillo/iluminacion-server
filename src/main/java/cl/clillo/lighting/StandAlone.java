package cl.clillo.lighting;

import cl.clillo.lighting.dmx.ArtNet;
import cl.clillo.lighting.executor.QLCEfxExecutor;
import cl.clillo.lighting.fixture.qlc.QLCFixture;
import cl.clillo.lighting.model.QLCDirection;
import cl.clillo.lighting.model.QLCEfx;
import cl.clillo.lighting.model.QLCEfxCircle;
import cl.clillo.lighting.model.QLCEfxFixtureData;
import cl.clillo.lighting.model.QLCEfxLine;
import cl.clillo.lighting.model.QLCEfxMultiLine;
import cl.clillo.lighting.model.QLCFunction;
import cl.clillo.lighting.model.QLCModel;
import cl.clillo.lighting.model.QLCPoint;
import cl.clillo.lighting.model.QLCRunOrder;
import cl.clillo.lighting.model.QLCSequence;
import cl.clillo.lighting.model.QLCStep;
import cl.clillo.lighting.model.RealPoint;
import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.utils.EffectEditPanel;
import cl.clillo.lighting.utils.FixtureRoboticPanel;
import cl.clillo.lighting.utils.EFXMConfigureApp;

import java.util.ArrayList;
import java.util.List;

public class StandAlone {

    public static void main(String[] args) {
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

        final QLCModel qlcModel = new QLCModel();

        final QLCEfxCircle qlcEfxCircle = qlcModel.getFunction(13); // 13 -circle
        qlcEfxCircle.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(19)).build());
        qlcEfxCircle.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(19)).startOffset(90).reverse().build());
        qlcEfxCircle.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(19)).startOffset(180).build());
        qlcEfxCircle.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(19)).startOffset(270).reverse().build());
        qlcEfxCircle.updateParameters(52672.0, 54977.4, 6880.0, 7490.0);

        final QLCEfxLine qlcEfxLine = qlcModel.getFunction(14);
        qlcEfxLine.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(19)).build());
        qlcEfxLine.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(19)).startOffset(0.25).reverse().build());
        qlcEfxLine.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(19)).startOffset(50).build());
        qlcEfxLine.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(19)).startOffset(0.75).reverse().build());
        qlcEfxLine.updateParameters(60416.0, 45001.0, 45248.0, 51336.0);

       final QLCEfxMultiLine qlcEfxMultiLine = new QLCEfxMultiLine(0, "type", "name", "path",
            QLCDirection.FORWARD, QLCRunOrder.LOOP, new ArrayList<>(), null, new ArrayList<>());
        qlcEfxMultiLine.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(19)).build());
        qlcEfxMultiLine.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(19)).startOffset(25).reverse().build());
        qlcEfxMultiLine.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(19)).startOffset(50).build());
        qlcEfxMultiLine.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(19)).startOffset(75).reverse().build());
       /* qlcEfxMultiLine.updateParameters(List.of(
                ,
                ,
                RealPoint.builder().x(52248.0).y(45220.0).build(),
                RealPoint.builder().x(58248.0).y(60220.0).build()));
*/

        qlcEfxMultiLine.updateParameters(
                RealPoint.builder().x(45248.0).y(45001.0).build(),
                RealPoint.builder().x(60416.0).y(60220.0).build());

        //final QLCEfx qlcFunction = qlcEfxCircle;
      //  final QLCEfx qlcFunction = qlcEfxLine;
        final QLCEfx qlcFunction = qlcEfxMultiLine;

        final Show dummy = Show.builder()
                .name("bouncing-auto")
                .executing(true)
                .firstTimeExecution(true)
                .stepList(List.of())
               // .function((QLCSequence)qlcSequence)
              //  .function(qlcModel.getFunction(61))
               // .function(qlcModel.getFunction(10))
                .function(qlcFunction)
                .build();

        showList.add(dummy);

        final EFXMConfigureApp p = EFXMConfigureApp.start(qlcFunction);
        final EffectEditPanel effectEditPanel = ((FixtureRoboticPanel)p.getContentPane()).getPnlMovingHead1();
        ((QLCEfxExecutor)dummy.getStepExecutor()).setRoboticNotifiable(effectEditPanel);

        Scheduler scheduler = new Scheduler(showList);
        scheduler.start();
    }
}
