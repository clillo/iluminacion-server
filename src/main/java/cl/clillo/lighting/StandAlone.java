package cl.clillo.lighting;

import cl.clillo.lighting.model.QLCFunction;
import cl.clillo.lighting.model.QLCModel;
import cl.clillo.lighting.model.QLCPoint;
import cl.clillo.lighting.model.QLCSequence;
import cl.clillo.lighting.model.QLCStep;
import cl.clillo.lighting.model.Show;

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

        final SequenceExecutor sequenceExecutor = new SequenceExecutor();

        final Show dummy = Show.builder()
                .name("bouncing-auto")
                .stepExecutor(sequenceExecutor)
                .executing(true)
                .firstTimeExecution(true)
                .stepList(List.of())
                .sequence((QLCSequence)qlcSequence)
                .sequence(qlcModel.getSequence(61))
                .build();

        showList.add(dummy);

        Scheduler scheduler = new Scheduler(showList);
        scheduler.start();
    }
}
