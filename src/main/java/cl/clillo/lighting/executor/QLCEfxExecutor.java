package cl.clillo.lighting.executor;

import cl.clillo.lighting.external.midi.RoboticNotifiable;
import cl.clillo.lighting.model.QLCEfx;
import cl.clillo.lighting.model.QLCExecutionNode;
import cl.clillo.lighting.model.Show;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class QLCEfxExecutor extends AbstractExecutor {

    private final Show show;
    private final QLCEfx efx;
    private RoboticNotifiable roboticNotifiable;
    private int speed;

    public QLCEfxExecutor(final Show show) {
        super(show, List.of());
        this.show = show;
        this.efx = show.getFunction();
        speed = 25;
    }

    public void setRoboticNotifiable(final RoboticNotifiable roboticNotifiable) {
        this.roboticNotifiable = roboticNotifiable;
    }

    @Override
    public void executeDefaultScheduler() {
        if (show.isFirstTimeExecution()){
            show.setFirstTimeExecution(false);
            log.info("executing {} efx {}", show.getName(), show.getFunction().getId());
        }

        final QLCExecutionNode node = efx.nextNode();
        if (node==null)
            return;

        node.send();

        //roboticNotifiable.notify(node);

      //  log.info("executing {} efx {}", show.getName(), show.getFunction().getId());

        show.setNextExecutionTime(System.currentTimeMillis() + speed);
    }


    public void setSpeed(int speed) {
        this.speed = speed;
    }
}
