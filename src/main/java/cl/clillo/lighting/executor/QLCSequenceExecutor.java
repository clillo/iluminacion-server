package cl.clillo.lighting.executor;

import cl.clillo.lighting.dmx.Dmx;
import cl.clillo.lighting.executor.StepExecutor;
import cl.clillo.lighting.model.QLCPoint;
import cl.clillo.lighting.model.QLCSequence;
import cl.clillo.lighting.model.QLCStep;
import cl.clillo.lighting.model.Show;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class QLCSequenceExecutor implements StepExecutor {

    private final Dmx dmx = Dmx.getInstance();
    private int actualStep;

    @Override
    public void execute(final Show show) {
        if (show.isFirstTimeExecution()){
            actualStep = 0;
            show.setFirstTimeExecution(false);
        }

        final QLCSequence sequence = show.getFunction();
        final QLCStep step = sequence.getQlcStepList().get(actualStep);

        log.info("executing {} sequence {}: [{}] [{}]", show.getName(), actualStep, sequence.getId(), step);

        for (QLCPoint point: step.getPointList())
            dmx.send(point);

        actualStep++;

        if (actualStep>=sequence.getQlcStepList().size()){
            actualStep=0;

        }

        show.setNextExecutionTime(System.currentTimeMillis() + step.getFadeIn() + step.getHold() + step.getFadeOut());
    }
}
