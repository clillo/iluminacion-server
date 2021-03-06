package cl.clillo.ilumination.executor;

import cl.clillo.ilumination.dmx.Dmx;
import cl.clillo.ilumination.model.Point;
import cl.clillo.ilumination.model.Show;
import cl.clillo.ilumination.model.Step;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Builder
@Component
@Log4j2
public class GenericExecutor implements StepExecutor{

    @Autowired
    private Dmx dmx;

    @Override
    public void execute(Show show) {

        final Step step = show.nextStep();
        final List<Point> pointList = step.getPointList();
        log.info("executing {} step {}: [{}]", show.getName(), show.getPasoActual(), step.getDescription());

        for (final Point point: pointList)
            dmx.enviar(point);

    }
}
