package cl.clillo.lighting.executor;

import cl.clillo.lighting.external.dmx.Dmx;
import cl.clillo.lighting.model.QLCPoint;
import cl.clillo.lighting.model.QLCScene;
import cl.clillo.lighting.model.Show;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class QLCSceneExecutor implements IQLCStepExecutor {

    private final Dmx dmx = Dmx.getInstance();
    private final Show show;
    private int sendNumber;

    public QLCSceneExecutor(final Show show) {
        this.show = show;
    }

    @Override
    public void execute() {
        if (show.isFirstTimeExecution()){
            show.setFirstTimeExecution(false);
            sendNumber = 2;
        }

        if (sendNumber<=0)
            return;

        sendNumber--;
        final QLCScene scene = show.getFunction();
        System.out.println("Sending "+ show.getId()+ "\t" +scene.getType() + "\t"+scene.getName()+"\t"+scene.getQlcPointList().size());
        for (QLCPoint qlcPoint: scene.getQlcPointList())
            dmx.send(qlcPoint.getDmxChannel(), qlcPoint.getData());

        show.setNextExecutionTime(System.currentTimeMillis() + 100);
    }
}
