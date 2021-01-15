package cl.clillo.ilumination.model;

import cl.clillo.ilumination.fixture.dmx.MovingHead;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Data
public class Coordinate {

    private enum RelativePositionToWindow {Top, Bottom, Left, Right};

    private static final int factor = 4;

    private long x;
    private long y;
    private long maxX;
    private long minX;
    private long maxY;
    private long minY;

    private MovingHead fixture;
    private Random rndPan = new Random();
    private Random rndTilt = new Random();

    public Coordinate(MovingHead fixture){
        this.fixture = fixture;
      //  x = (fixture.getPanMax()-fixture.getPanMin())*factor+fixture.getPanMin();
       // y = (fixture.getTiltMax()-fixture.getTiltMin())*factor+fixture.getTiltMin();

        minX = fixture.getPanMin()*factor;
        maxX = fixture.getPanMax()*factor;

        minY = fixture.getTiltMin()*factor;
        maxY = fixture.getTiltMax()*factor;

        x = (Math.random()>0.5?minX:maxX);
        y = (Math.random()>0.5?minY:maxX);
    }

    public void fixtureMove(final boolean isFirstTimeExecution){
        long pan = x;
        long tilt = y;

        RelativePositionToWindow posicion = null;

        if (pan <= minX)
            posicion = RelativePositionToWindow.Left;

        if (pan >= maxX)
            posicion = RelativePositionToWindow.Right;

        if (tilt <= minY)
            posicion = RelativePositionToWindow.Top;

        if (tilt >= maxX)
            posicion = RelativePositionToWindow.Bottom;

        if (posicion == null) {
            if (!isFirstTimeExecution)
                return;

            posicion = RelativePositionToWindow.Top;
        }

        switch (posicion){
            case Top:
                pan = minX;
                tilt = getPosicionAleatoriaTilt(rndTilt);
                break;

            case Left:
                pan = getPosicionAleatoriaPan(rndPan);
                tilt = maxY;
                break;

            case Right:
                pan = getPosicionAleatoriaPan(rndPan);
                tilt = minY;
                break;

            case Bottom:
                pan = maxX;
                tilt = getPosicionAleatoriaTilt(rndTilt);
                break;
        }

        mueveEntidad(pan, tilt, fixture);

    }


    public long getPosicionAleatoriaPan(Random rndPan){
        long ancho = maxX - minX;
        long pos =  rndPan.nextInt((int)ancho);
        return minX + pos;
    }

    public long getPosicionAleatoriaTilt(Random rndTilt){
        long alto = maxY - minY;
        long pos =  rndTilt.nextInt((int)alto);
        return minY + pos;
    }

    private void mueveEntidad(long pan, long tilt, final MovingHead fixture){
        //  fixture.setVelocidadActual(programa.getVelocidadParaMovimientoRoboticos());
        fixture.freeze();
        fixture.moverA(pan, tilt);
        //  programa.setEjecutandoMovimiento(true);
        //  fixture.setProgramaMovimientoEnEjecucion(programa);
    }

    public List<Point> getPointList(Map<String, Integer> dmxMap){
        final List<Point> pointList = Lists.newArrayList();

        int vPan = (int)( x  / factor);
        int vPanFine = (int)(x % factor);

        int vTilt = (int)(y/ factor);
        int vTiltFine = (int)(y % factor);

        pointList.add(Point.builder()
                .canal(dmxMap.get("pan"))
                .dmx(vPan)
                .build());

        pointList.add(Point.builder()
                .canal(dmxMap.get("panFine"))
                .dmx(vPanFine)
                .build());

        pointList.add(Point.builder()
                .canal(dmxMap.get("tilt"))
                .dmx(vTilt)
                .build());

        pointList.add(Point.builder()
                .canal(dmxMap.get("tiltFine"))
                .dmx(vTiltFine)
                .build());

        return pointList;
    }

}
