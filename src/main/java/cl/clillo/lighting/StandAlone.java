package cl.clillo.lighting;

import cl.clillo.lighting.config.QLCFixtureBuilder;
import cl.clillo.lighting.dmx.ArtNet;
import cl.clillo.lighting.executor.QLCEfxExecutor;
import cl.clillo.lighting.fixture.qlc.QLCFixture;
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
import cl.clillo.lighting.utils.EffectEditPanel;
import cl.clillo.lighting.utils.FixtureRoboticPanel;
import cl.clillo.lighting.utils.EFXMConfigureApp;

import java.util.ArrayList;
import java.util.List;

public class StandAlone {

    public static void main(String[] args) {
        ArtNet.setMode(ArtNet.ArtNetMode.DIRECT_ART_NET);
      //  ArtNet.setMode(ArtNet.ArtNetMode.HTTP_ART_NET);
     //   ArtNet.setMode(ArtNet.ArtNetMode.NON_ART_NET);

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

        final QLCEfxCircle qlcEfxCircle = new QLCEfxCircle(1,null,null,null,null,null,null,null, new ArrayList<>()); // 13 -circle
        qlcEfxCircle.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(101)).build());
        qlcEfxCircle.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(102)).startOffset(36).reverse().build());

        qlcEfxCircle.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(201)).startOffset(72).build());
        qlcEfxCircle.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(202)).startOffset(108).reverse().build());
        qlcEfxCircle.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(203)).startOffset(144).build());
        qlcEfxCircle.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(204)).startOffset(180).reverse().build());

        qlcEfxCircle.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(301)).startOffset(216).build());
        qlcEfxCircle.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(302)).startOffset(252).reverse().build());
        qlcEfxCircle.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(303)).startOffset(288).build());
        qlcEfxCircle.getFixtureList().add(QLCEfxFixtureData.builder().fixture(qlcModel.getFixture(304)).startOffset(314).reverse().build());
        qlcEfxCircle.updateParameters(44352, 14417, 8520, 13190);

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

        final Show movingFade = Show.builder()
                .name("bouncing-auto")
                .executing(true)
                .firstTimeExecution(true)
                .stepList(List.of())
                .function(QLCScene.build(1, List.of(
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
                        QLCPoint.buildRoboticPoint(qlcModel.getFixture(304), QLCFixture.ChannelType.DIMMER, 128))))
                .build();

        showList.add(movingPositions);
        showList.add(movingFade);

        final EFXMConfigureApp p = EFXMConfigureApp.start(qlcFunction);
        final EffectEditPanel effectEditPanel = ((FixtureRoboticPanel)p.getContentPane()).getPnlMovingHead1();
        ((QLCEfxExecutor)movingPositions.getStepExecutor()).setRoboticNotifiable(effectEditPanel);

        Scheduler scheduler = new Scheduler(showList);
        scheduler.start();
    }
}
