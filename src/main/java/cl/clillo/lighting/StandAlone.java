package cl.clillo.lighting;

import cl.clillo.lighting.dmx.ArtNet;
import cl.clillo.lighting.executor.QLCEfxExecutor;
import cl.clillo.lighting.model.QLCDirection;
import cl.clillo.lighting.model.QLCEfxCircle;
import cl.clillo.lighting.model.QLCEfxLine;
import cl.clillo.lighting.model.QLCEfxMultiLine;
import cl.clillo.lighting.model.QLCFunction;
import cl.clillo.lighting.model.QLCModel;
import cl.clillo.lighting.model.QLCPoint;
import cl.clillo.lighting.model.QLCRoboticFixture;
import cl.clillo.lighting.model.QLCRunOrder;
import cl.clillo.lighting.model.QLCScene;
import cl.clillo.lighting.model.QLCSequence;
import cl.clillo.lighting.model.QLCStep;
import cl.clillo.lighting.model.RealPoint;
import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.utils.EffectEditPanel;
import cl.clillo.lighting.utils.FixtureRoboticPanel;
import cl.clillo.lighting.utils.EFXMConfigureApp;
import cl.clillo.lighting.utils.ScreenPoint;

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
        qlcEfxCircle.getFixtureList().add(qlcModel.getFixture(19));
        qlcEfxCircle.updateParameters(52672.0, 54977.4, 6880.0, 7490.0);

        final QLCEfxLine qlcEfxLine = qlcModel.getFunction(14); // 13 -circle
        qlcEfxLine.getFixtureList().add(qlcModel.getFixture(19));
        qlcEfxLine.updateParameters(60416.0, 45001.0, 45248.0, 51336.0);

        final QLCEfxMultiLine qlcEfxMultiLine = new QLCEfxMultiLine(0, "type", "name", "path",
            QLCDirection.FORWARD, QLCRunOrder.LOOP, new ArrayList<>(), null, new ArrayList<>());
        qlcEfxMultiLine.getFixtureList().add(qlcModel.getFixture(19));

       /* qlcEfxMultiLine.updateParameters(List.of(
                ,
                ,
                RealPoint.builder().x(52248.0).y(45220.0).build(),
                RealPoint.builder().x(58248.0).y(60220.0).build()));
*/

        qlcEfxMultiLine.updateParameters(RealPoint.builder().x(45248.0).y(45001.0).build(), RealPoint.builder().x(60416.0).y(60220.0).build());

        final Show dummy = Show.builder()
                .name("bouncing-auto")
                .executing(true)
                .firstTimeExecution(true)
                .stepList(List.of())
               // .function((QLCSequence)qlcSequence)
              //  .function(qlcModel.getFunction(61))
               // .function(qlcModel.getFunction(10))
            //    .function(qlcEfxCircle)
                .function(qlcEfxMultiLine)
             //  .function(qlcEfxLine)
                .build();

        showList.add(dummy);

        final EFXMConfigureApp p = EFXMConfigureApp.start(qlcEfxMultiLine);
        final EffectEditPanel effectEditPanel = ((FixtureRoboticPanel)p.getContentPane()).getPnlMovingHead1();
        ((QLCEfxExecutor)dummy.getStepExecutor()).setRoboticNotifiable(effectEditPanel);

        Scheduler scheduler = new Scheduler(showList);
        scheduler.start();
    }
}
