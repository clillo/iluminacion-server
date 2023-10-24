package cl.clillo.lighting.executor;

import cl.clillo.lighting.external.dmx.Dmx;
import cl.clillo.lighting.model.QLCCollection;
import cl.clillo.lighting.model.QLCFunction;
import cl.clillo.lighting.model.QLCPoint;
import cl.clillo.lighting.model.QLCSequence;
import cl.clillo.lighting.model.QLCStep;
import cl.clillo.lighting.model.Show;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class QLCCollectionExecutor implements IQLCStepExecutor {

    private final Dmx dmx = Dmx.getInstance();
    private int actualStep;
    private final Show show;

    public QLCCollectionExecutor(final Show show) {
        this.show = show;

    }

    @Override
    public void execute() {
        if (show.isFirstTimeExecution()){
            actualStep = 0;
            show.setFirstTimeExecution(false);
        }

        final QLCCollection sequence = show.getFunction();
        log.info("executing {} collection {}:", show.getName(), sequence.getName() );
 //       final QLCFunction step = sequence.getQlcStepList().get(actualStep);

//        log.info("executing {} sequence {}: [{}] [{}] {}", show.getName(), actualStep, sequence.getId(), step.getId(), step.getPointList());
/*
        for (QLCPoint point: step.getPointList())
            dmx.send(point);

        actualStep++;

        if (actualStep>=sequence.getQlcStepList().size()){
            actualStep=0;

        }

        /show.setNextExecutionTime(System.currentTimeMillis() + step.getFadeIn() + step.getHold() + step.getFadeOut());*/
    }
}
