package cl.clillo.lighting.executor;

import cl.clillo.lighting.external.dmx.Dmx;
import cl.clillo.lighting.model.QLCPoint;
import cl.clillo.lighting.model.QLCSequence;
import cl.clillo.lighting.model.QLCStep;
import cl.clillo.lighting.model.Show;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class QLCSequenceExecutor implements IQLCStepExecutor {

    private final Dmx dmx = Dmx.getInstance();
    private int actualStep;
    private final Show show;
    private int[] dimmerChannels;

    public QLCSequenceExecutor(final Show show) {
        this.show = show;
        dimmerChannels = show.getDimmerChannels();
    }

    @Override
    public void execute() {
        if (show.isFirstTimeExecution()){
            actualStep = 0;
            show.setFirstTimeExecution(false);
        }

        final QLCSequence sequence = show.getFunction();
        final QLCStep step = sequence.getQlcStepList().get(actualStep);

        log.info("executing {} sequence {}: [{}] [{}]", show.getName(), actualStep, sequence.getId(), step.getId());

        for (QLCPoint point: step.getPointList())
            dmx.send(point);

        actualStep++;

        if (actualStep>=sequence.getQlcStepList().size()){
            actualStep=0;

        }

        show.setNextExecutionTime(System.currentTimeMillis() + step.getFadeIn() + step.getHold() + step.getFadeOut());
    }
}
