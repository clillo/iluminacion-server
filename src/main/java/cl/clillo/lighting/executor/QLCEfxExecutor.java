package cl.clillo.lighting.executor;

import cl.clillo.lighting.dmx.Dmx;
import cl.clillo.lighting.model.QLCEfx;
import cl.clillo.lighting.model.QLCExecutionNode;
import cl.clillo.lighting.model.QLCRoboticFixture;
import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.utils.RoboticNotifiable;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class QLCEfxExecutor implements IQLCStepExecutor {

    private final Dmx dmx = Dmx.getInstance();
    private int t;
    private int inc;
    private final Show show;
    private final QLCEfx efx;
    private RoboticNotifiable roboticNotifiable;

    public QLCEfxExecutor(final Show show) {
        this.show = show;
        this.efx = show.getFunction();
    }

    public void setRoboticNotifiable(final RoboticNotifiable roboticNotifiable) {
        this.roboticNotifiable = roboticNotifiable;
    }

    @Override
    public void execute() {
        if (show.isFirstTimeExecution()){
            t = 0;
            inc = 1;
            show.setFirstTimeExecution(false);
        }

        dmx.send(102, 40);
//        dmx.send(102, 185);
        dmx.send(106, 255);
        dmx.send(107, 255);

        if (efx.getNodes().size()>0) {
            final QLCExecutionNode node = efx.getNodes().get(t);
            node.send();

            roboticNotifiable.notify(t);
            log.info("executing {} efx {} {} {}", show.getName(), t, node.getChannel(), node.getData());
        }
        t+=inc;

        if (t>=efx.getNodes().size()-1 || t<=0){
            inc = inc==1?-1:1;
        }

        show.setNextExecutionTime(System.currentTimeMillis() + 50);
    }
}
