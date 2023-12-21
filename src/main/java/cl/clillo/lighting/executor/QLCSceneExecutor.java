package cl.clillo.lighting.executor;

import cl.clillo.lighting.external.dmx.Dmx;
import cl.clillo.lighting.model.QLCPoint;
import cl.clillo.lighting.model.QLCScene;
import cl.clillo.lighting.model.Show;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class QLCSceneExecutor extends AbstractExecutor {

    private final Dmx dmx = Dmx.getInstance();
    private final Show show;
    private int sendNumber;

    public QLCSceneExecutor(final Show show) {
        super(show, List.of());
        this.show = show;
    }

    @Override
    public void executeDefaultScheduler() {
        if (show.isFirstTimeExecution()){
            show.setFirstTimeExecution(false);
            sendNumber = 2;
        }

        if (sendNumber<=0)
            return;

        sendNumber--;
        final QLCScene scene = show.getFunction();
        log.info("Sending "+ show.getId()+ "\t" +scene.getType() + "\t"+scene.getName()+"\t"+scene.getQlcPointList());
        for (QLCPoint qlcPoint: scene.getQlcPointList())
            dmx.send(qlcPoint);

        show.setNextExecutionTime(System.currentTimeMillis() + 100);
    }
}
