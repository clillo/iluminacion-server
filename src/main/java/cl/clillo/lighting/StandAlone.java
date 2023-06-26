package cl.clillo.lighting;

import cl.clillo.lighting.executor.QLCEfxExecutor;
import cl.clillo.lighting.model.QLCEfx;
import cl.clillo.lighting.model.QLCFunction;
import cl.clillo.lighting.model.QLCModel;
import cl.clillo.lighting.model.QLCPoint;
import cl.clillo.lighting.model.QLCSequence;
import cl.clillo.lighting.model.QLCStep;
import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.utils.EffectEditPanel;
import cl.clillo.lighting.utils.FixtureRoboticPanel;
import cl.clillo.lighting.utils.PruebaCreaProgramasRobotizados;

import java.util.ArrayList;
import java.util.List;

public class StandAlone {

    public static void main(String[] args) {

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

        final QLCEfx qlcEfx = qlcModel.getFunction(13);
        qlcEfx.getFixtureList().add(qlcModel.getFixture(19));
        qlcEfx.setCenterX(32700);
        qlcEfx.setCenterY(32700);
        qlcEfx.setWidth(10000);
        qlcEfx.setHeight(20000);

        final Show dummy = Show.builder()
                .name("bouncing-auto")
                .executing(true)
                .firstTimeExecution(true)
                .stepList(List.of())
               // .function((QLCSequence)qlcSequence)
              //  .function(qlcModel.getFunction(61))
               // .function(qlcModel.getFunction(10))
                .function(qlcEfx)
                .build();

        showList.add(dummy);

        PruebaCreaProgramasRobotizados p = PruebaCreaProgramasRobotizados.start();
        EffectEditPanel effectEditPanel = ((FixtureRoboticPanel)p.getContentPane()).getPnlMovingHead1();
        effectEditPanel.setQlcEfx(qlcEfx);

        ((QLCEfxExecutor)dummy.getStepExecutor()).setRoboticNotifiable(effectEditPanel);
        Scheduler scheduler = new Scheduler(showList);
        scheduler.start();
    }
}
