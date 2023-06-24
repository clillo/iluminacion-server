package cl.clillo.lighting.executor;

import cl.clillo.lighting.dmx.Dmx;
import cl.clillo.lighting.model.QLCEfx;
import cl.clillo.lighting.model.QLCRoboticFixture;
import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.utils.RoboticNotifiable;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class QLCEfxExecutor implements StepExecutor {

    private static final double RADIUS = 7000.0;
    private final Dmx dmx = Dmx.getInstance();
    private double t;

    private RoboticNotifiable roboticNotifiable;

    public void setRoboticNotifiable(RoboticNotifiable roboticNotifiable) {
        this.roboticNotifiable = roboticNotifiable;
    }

    @Override
    public void execute(final Show show) {
        if (show.isFirstTimeExecution()){
            t = 0;
            show.setFirstTimeExecution(false);
        }

        final QLCEfx efx = show.getFunction();
        final List<QLCRoboticFixture> fixtureList = efx.getFixtureList();

        dmx.send(102, 40);
//        dmx.send(102, 185);
        dmx.send(106, 255);
        dmx.send(107, 255);

        double x = 30000 + (int)( Math.cos(Math.toRadians(t))*RADIUS);
        double y = 20000 + (int)( Math.sin(Math.toRadians(t))*RADIUS);

        double vPan = x / 256;
        double vPanFine = x % 256;

        double vTilt = y / 256;
        double vTiltFine = y % 256;

        roboticNotifiable.notify(x, y);
        log.info("executing {} efx {} {} {}", show.getName(), efx.getName(), x, y);

        for (QLCRoboticFixture fixture: fixtureList) {
            dmx.send(fixture.getPanDmxChannel(), (int) vPan);
            dmx.send(fixture.getTiltDmxChannel(), (int) vTilt);
            dmx.send(fixture.getPanFineDmxChannel(), (int) vPanFine);
            dmx.send(fixture.getTiltFineDmxChannel(), (int) vTiltFine);
        }
        t+=5;
//        t+=30;

        if (t>=360){
            t=0;
            roboticNotifiable.clear();
        }

        show.setNextExecutionTime(System.currentTimeMillis() + 50);
    }
}
