package cl.clillo.lighting.executor;

import cl.clillo.lighting.model.QLCSequence;
import cl.clillo.lighting.model.QLCStep;
import cl.clillo.lighting.model.Show;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class QLCSequenceExecutor extends AbstractExecutor {

    private final QLCSequence sequence;

    public QLCSequenceExecutor(final Show show) {
        super(show, ((QLCSequence)show.getFunction()).getQlcStepList());
        sequence = show.getFunction();

    }

    @Override
    public void executeDefaultScheduler() {

        preExecuteDefaultScheduler();

        final QLCStep step = sequence.getQlcStepList().get(actualStep);

   //     log.info("executing {} sequence {}: id [{}] step id[{}] Points: {}", show.getName(), actualStep, sequence.getId(), step.getId(), step.getPointList());
        log.info("executing {} sequence {}: id [{}] step id[{}] Direction: {} Run Order: {}", show.getName(), actualStep, sequence.getId(), step.getId(), sequence.getDirection(), sequence.getRunOrder());

        postExecuteDefaultScheduler(step);

        show.setNextExecutionTime(System.currentTimeMillis() + step.getFadeIn() + step.getHold() + step.getFadeOut());
    }

}
