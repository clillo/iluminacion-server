package cl.clillo.ilumination.executor;

import cl.clillo.ilumination.config.ConcreteFixtureConfig;
import cl.clillo.ilumination.dmx.Dmx;
import cl.clillo.ilumination.fixture.dmx.Fixture;
import cl.clillo.ilumination.fixture.dmx.MovingHead;
import cl.clillo.ilumination.model.Point;
import cl.clillo.ilumination.model.Show;
import cl.clillo.ilumination.model.Step;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Builder
@Component
@Log4j2
public class BouncingExecutor implements StepExecutor{

    private enum PosicionRelativaVentana {Arriba, Abajo, Izquierda, Derecha};


    @Autowired
    private Dmx dmx;

    @Autowired
    private ConcreteFixtureConfig concreteFixtureConfig;

    @Override
    public void execute(Show show) {
        show.setNextExecutionTime(System.currentTimeMillis() + 100);
        MovingHead mh601 = (MovingHead)concreteFixtureConfig.getFixture("mh-60-1");
        MovingHead mh602 = (MovingHead)concreteFixtureConfig.getFixture("mh-60-2");
        MovingHead mh901 = (MovingHead)concreteFixtureConfig.getFixture("mh-90-1");
        MovingHead mh902 = (MovingHead)concreteFixtureConfig.getFixture("mh-90-2");

        log.info("executing {} bouncing", show.getName());

        fixtureMove(mh601, show.isFirstTimeExecution());
        fixtureMove(mh602, show.isFirstTimeExecution());
        fixtureMove(mh901, show.isFirstTimeExecution());
        fixtureMove(mh902, show.isFirstTimeExecution());

        show.setFirstTimeExecution(false);
        for (final Point point: mh601.getPointList())
            dmx.enviar(point);
        for (final Point point: mh602.getPointList())
            dmx.enviar(point);

        for (final Point point: mh901.getPointList())
            dmx.enviar(point);
        for (final Point point: mh902.getPointList())
            dmx.enviar(point);
    }

    public void fixtureMove(final MovingHead fixture, final boolean isFirstTimeExecution){
        Random rndPan = new Random();
        Random rndTilt = new Random();

        long pan = fixture.getRealPan();
        long tilt = fixture.getRealTilt();

        PosicionRelativaVentana posicion = null;

        if (pan <= fixture.getPanMin())
            posicion = PosicionRelativaVentana.Izquierda;

        if (pan >= fixture.getPanMax())
            posicion = PosicionRelativaVentana.Derecha;

        if (tilt <= fixture.getTiltMin())
            posicion = PosicionRelativaVentana.Arriba;

        if (tilt >= fixture.getTiltMax())
            posicion = PosicionRelativaVentana.Abajo;

        if (posicion == null) {
            if (!isFirstTimeExecution)
                return;

            posicion = PosicionRelativaVentana.Arriba;
        }

        switch (posicion){
            case Arriba:
                pan = fixture.getPanMin();
                tilt = fixture.getPosicionAleatoriaTilt(rndTilt);
                break;

            case Izquierda:
                pan = fixture.getPosicionAleatoriaPan(rndPan);
                tilt = fixture.getTiltMax();
                break;

            case Derecha:
                pan = fixture.getPosicionAleatoriaPan(rndPan);
                tilt = fixture.getTiltMin();
                break;

            case Abajo:
                pan = fixture.getPanMax();
                tilt = fixture.getPosicionAleatoriaTilt(rndTilt);
                break;
        }

        mueveEntidad(pan, tilt, fixture);

    }

    private void mueveEntidad(long pan, long tilt, final MovingHead fixture){
      //  fixture.setVelocidadActual(programa.getVelocidadParaMovimientoRoboticos());
        fixture.freeze();
        fixture.moverA(pan, tilt);
      //  programa.setEjecutandoMovimiento(true);
      //  fixture.setProgramaMovimientoEnEjecucion(programa);
    }
}
