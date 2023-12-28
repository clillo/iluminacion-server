package cl.clillo.lighting.executor;

import cl.clillo.lighting.model.Chaser;
import cl.clillo.lighting.model.ChaserStep;
import cl.clillo.lighting.model.QLCPoint;
import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.model.ShowCollection;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class ChaserExecutor extends AbstractExecutor {

    private final Chaser chaser;
    private ChaserExecutorShowListener chaserExecutorShowListener;

    public ChaserExecutor(final Show show) {
        super(show, ((Chaser)show.getFunction()).getChaserSteps().size());
        chaser = show.getFunction();

    }

    @Override
    public void executeDefaultScheduler() {
        preExecuteDefaultScheduler();
        if (chaser.getBlackoutShow()==null)
            return;

        chaser.getBlackoutShow().setExecuting(true);
        for (ChaserStep chaserStep: chaser.getChaserSteps()) {
            chaserStep.getShow().setExecuting(false);
            if (chaserExecutorShowListener!=null)
                chaserExecutorShowListener.stopExecuting(chaserStep.getShow());

        }

        blackout(chaser);

        final ChaserStep chaserStep = chaser.getChaserSteps().get(actualStep);
        chaserStep.getShow().setExecuting(true);
        if (chaserExecutorShowListener!=null)
            chaserExecutorShowListener.startExecuting(chaserStep.getShow());

        long nextStepExecInMillis = (long) (chaserStep.getFadeIn() + chaserStep.getHold() + chaserStep.getFadeOut()) * chaser.getSpeed();
        log.info("executing {} chaser {}: id [{}] show[{}] chaserStep id[{}] nextStepExecInMillis [{}]", show.getName(), actualStep, chaser.getId(), chaserStep.getShow().getName(), chaserStep.getId(), nextStepExecInMillis);

        show.setNextExecutionTime(System.currentTimeMillis() + nextStepExecInMillis);

        postExecuteDefaultScheduler();
    }

    @Override
    public void stop(){
        for (ChaserStep chaserStep: chaser.getChaserSteps()) {
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
