package cl.clillo.lighting.executor;

import cl.clillo.lighting.external.dmx.Dmx;
import cl.clillo.lighting.model.QLCPoint;
import cl.clillo.lighting.model.QLCSequence;
import cl.clillo.lighting.model.QLCStep;
import cl.clillo.lighting.model.Show;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class OS2LStepExecutor extends IStepExecutor{

    private final QLCSequence sequence;

    public OS2LStepExecutor(Show show) {
        super(show, ((QLCSequence)show.getFunction()).getQlcStepList());
        sequence = show.getFunction();
        direction = sequence.getDirection();
        runOrder = sequence.getRunOrder();
    }

    @Override
    public void executeOS2LScheduler() {
        preExecuteDefaultScheduler();

        final QLCStep step = sequence.getQlcStepList().get(actualStep);

       // log.info("executing {} sequence {}: [{}] [{}] {}", show.getName(), actualStep, sequence.getId(), step.getId(), step.getPointList());

        postExecuteDefaultScheduler(step);

    }
}
