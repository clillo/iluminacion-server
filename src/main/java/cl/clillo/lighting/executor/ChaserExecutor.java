package cl.clillo.lighting.executor;

import cl.clillo.lighting.model.QLCChaser;
import cl.clillo.lighting.model.QLCChaserStep;
import cl.clillo.lighting.model.QLCSequence;
import cl.clillo.lighting.model.QLCStep;
import cl.clillo.lighting.model.Show;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class ChaserExecutor extends AbstractExecutor {

    private final QLCChaser chaser;
    private ChaserExecutorShowListener chaserExecutorShowListener;

    public ChaserExecutor(final Show show) {
        super(show, ((QLCChaser)show.getFunction()).getChaserSteps().size());
        chaser = show.getFunction();

    }

    @Override
    public void executeDefaultScheduler() {
        preExecuteDefaultScheduler();
        if (chaser.getBlackoutShow()==null)
            return;

        chaser.getBlackoutShow().setExecuting(false);
        for (QLCChaserStep chaserStep: chaser.getChaserSteps()) {
            if (chaserExecutorShowListener!=null)
                chaserExecutorShowListener.stopExecuting(chaserStep.getShow());
            chaserStep.getShow().setExecuting(false);
        }

        final QLCChaserStep chaserStep = chaser.getChaserSteps().get(actualStep);
        chaserStep.getShow().setExecuting(true);
        if (chaserExecutorShowListener!=null)
            chaserExecutorShowListener.startExecuting(chaserStep.getShow());

        log.info("executing {} chaser {}: id [{}] show[{}] chaserStep id[{}]", show.getName(), actualStep, chaser.getId(), chaserStep.getShow().getName(), chaserStep.getId());

        show.setNextExecutionTime(System.currentTimeMillis() + (long) (chaserStep.getFadeIn() + chaserStep.getHold() + chaserStep.getFadeOut()) * chaser.getSpeed());

        postExecuteDefaultScheduler();
    }

    @Override
    public void stop(){
        for (QLCChaserStep chaserStep: chaser.getChaserSteps()) {
            chaserStep.getShow().setExecuting(false);
            if (chaserExecutorShowListener!=null)
                chaserExecutorShowListener.stopExecuting(chaserStep.getShow());
        }

        if (chaser.getBlackoutShow()==null)
            return;
        chaser.getBlackoutShow().setExecuting(true);
        log.info("Blackout: "+chaser.getBlackoutShow().getName()+"\t"+chaser.getBlackoutShow().isExecuting());
    }

    public void setChaserExecutorShowListener(ChaserExecutorShowListener chaserExecutorShowListener) {
        this.chaserExecutorShowListener = chaserExecutorShowListener;
    }
}
