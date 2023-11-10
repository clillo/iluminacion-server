package cl.clillo.lighting.executor;

import cl.clillo.lighting.external.dmx.Dmx;
import cl.clillo.lighting.model.QLCPoint;
import cl.clillo.lighting.model.QLCSequence;
import cl.clillo.lighting.model.QLCStep;
import cl.clillo.lighting.model.Show;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class QLCSequenceExecutor implements IStepExecutor {

    private final Dmx dmx = Dmx.getInstance();
    private int actualStep;
    private final Show show;

    public QLCSequenceExecutor(final Show show) {
        this.show = show;

    }

    @Override
    public void executeDefaultScheduler() {
        if (show.isFirstTimeExecution()){
            actualStep = 0;
            show.setFirstTimeExecution(false);
        }

        final QLCSequence sequence = show.getFunction();
        final QLCStep step = sequence.getQlcStepList().get(actualStep);

        log.info("executing {} sequence {}: id [{}] step id[{}] Points: {}", show.getName(), actualStep, sequence.getId(), step.getId(), step.getPointList());

        for (QLCPoint point: step.getPointList())
            dmx.send(point);

        actualStep++;

        if (actualStep>=sequence.getQlcStepList().size()){
            actualStep=0;

        }

        show.setNextExecutionTime(System.currentTimeMillis() + step.getFadeIn() + step.getHold() + step.getFadeOut());
    }
}
