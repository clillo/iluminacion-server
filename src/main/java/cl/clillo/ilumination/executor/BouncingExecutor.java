package cl.clillo.ilumination.executor;

import cl.clillo.ilumination.config.ConcreteFixtureConfig;
import cl.clillo.ilumination.dmx.Dmx;
import cl.clillo.ilumination.fixture.dmx.MovingHead;
import cl.clillo.ilumination.model.Point;
import cl.clillo.ilumination.model.Show;
import com.google.common.collect.Maps;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Builder
@Component
@Log4j2
public class BouncingExecutor implements StepExecutor{

    @Autowired
    private Dmx dmx;

    @Autowired
    private ConcreteFixtureConfig concreteFixtureConfig;

    private List<Point> pointsQueue;

    private Map<String, LinkedList<Punto>> positionsQueue;
    private Map<String, Long> nextMoveMilisec;

    private List<MovingHead> fixtureList;

    @PostConstruct
    public void init(){
        fixtureList = concreteFixtureConfig.getMovingHeads();

        positionsQueue = Maps.newHashMap();
        nextMoveMilisec = Maps.newHashMap();

        for(MovingHead fixture: fixtureList){
            positionsQueue.put(fixture.getId(), new LinkedList<>());
            nextMoveMilisec.put(fixture.getId(), System.currentTimeMillis());
        }
    }

    @Override
    public void execute(Show show) {
        show.setNextExecutionTime(System.currentTimeMillis() + 5);

        final long now = System.currentTimeMillis();
        for(MovingHead fixture: fixtureList){
            final String id = fixture.getId();
            final LinkedList<Punto> queue = positionsQueue.get(id);
            if(queue.isEmpty()) {
                final List<Punto> pointList = fixture.getCoordinates().fixtureMove();
                queue.addAll(pointList);
            }else{
                if (nextMoveMilisec.get(id)<now) {
                    nextMoveMilisec.put(id, now+30);
                    final Punto point = queue.getFirst();
                    queue.removeFirst();
                    final List<Point> pointList = fixture.getPointList(point.getPosX(), point.getPosY());
                    for (final Point p : pointList) {
                        dmx.enviar(p);
                        //   System.out.print(p.getCanal()+","+p.getDmx()+";");
                    }

                    //System.out.println("");
                }
            }
        }

        show.setFirstTimeExecution(false);
    }

}
