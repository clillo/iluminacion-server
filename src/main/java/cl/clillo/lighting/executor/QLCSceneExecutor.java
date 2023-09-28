package cl.clillo.lighting.executor;

import cl.clillo.lighting.dmx.Dmx;
import cl.clillo.lighting.model.QLCPoint;
import cl.clillo.lighting.model.QLCScene;
import cl.clillo.lighting.model.QLCSequence;
import cl.clillo.lighting.model.QLCStep;
import cl.clillo.lighting.model.Show;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class QLCSceneExecutor implements IQLCStepExecutor {

    private final Dmx dmx = Dmx.getInstance();
    private final Show show;

    public QLCSceneExecutor(final Show show) {
        this.show = show;
    }

    @Override
    public void execute() {
        if (show.isFirstTimeExecution()){
             show.setFirstTimeExecution(false);
        }

        System.out.println("Executing "+ show.getId() + "\t"+show.getName());

        final QLCScene scene = show.getFunction();
        for (QLCPoint qlcPoint: scene.getQlcPointList())
            dmx.send(qlcPoint.getDmxChannel(), qlcPoint.getData());

        show.setNextExecutionTime(System.currentTimeMillis() + 10000);
    }
}
