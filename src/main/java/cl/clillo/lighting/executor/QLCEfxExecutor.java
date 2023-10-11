package cl.clillo.lighting.executor;

import cl.clillo.lighting.external.midi.RoboticNotifiable;
import cl.clillo.lighting.model.QLCEfx;
import cl.clillo.lighting.model.QLCExecutionNode;
import cl.clillo.lighting.model.Show;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class QLCEfxExecutor implements IQLCStepExecutor {

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

        final QLCExecutionNode node = efx.nextNode();
        if (node==null)
            return;

        node.send();

        //roboticNotifiable.notify(node);

      //  log.info("executing {} efx {} {} {}", show.getName(), node.getId(), node.getChannel(), node.getData());

        show.setNextExecutionTime(System.currentTimeMillis() + 25);
    }
}
