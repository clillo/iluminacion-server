package cl.clillo.ilumination.executor;

import cl.clillo.ilumination.config.ConcreteFixtureConfig;
import cl.clillo.ilumination.dmx.Dmx;
import cl.clillo.ilumination.fixture.dmx.MovingHead;
import cl.clillo.ilumination.model.Point;
import cl.clillo.ilumination.model.Show;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;

@Builder
@Component
@Log4j2
public class BouncingExecutor implements StepExecutor{

    @Autowired
    private Dmx dmx;

    @Autowired
    private ConcreteFixtureConfig concreteFixtureConfig;

    @Override
    public void execute(Show show) {
        show.setNextExecutionTime(System.currentTimeMillis() + 100);

        log.info("executing {} bouncing", show.getName());

        for(MovingHead fixture: concreteFixtureConfig.getMovingHeads()){
            fixture.getCoordinates().fixtureMove(show.isFirstTimeExecution());
            for (final Point point: fixture.getPointList())
                dmx.enviar(point);
        }

        show.setFirstTimeExecution(false);

    }


}
