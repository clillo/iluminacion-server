package cl.clillo.lighting.executor;

import cl.clillo.lighting.dmx.Dmx;
import cl.clillo.lighting.model.QLCEfx;
import cl.clillo.lighting.model.QLCExecutionNode;
import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.utils.RoboticNotifiable;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class QLCEfxExecutor implements IQLCStepExecutor {

    private final Dmx dmx = Dmx.getInstance();
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
            show.setFirstTimeExecution(false);
        }

        dmx.send(102, 40);
//        dmx.send(102, 185);
        dmx.send(106, 255);
        dmx.send(107, 255);

        final QLCExecutionNode node = efx.nextNode();
        if (node==null)
            return;
        node.send();

       // roboticNotifiable.notify(node.getId());
     //   roboticNotifiable.notify(node.getTimePos());
        roboticNotifiable.notify(node);

        //log.info("executing {} efx {} {} {}", show.getName(), node.getId(), node.getChannel());

        show.setNextExecutionTime(System.currentTimeMillis() + 25);
    }
}
