package cl.clillo.ilumination.fixture.dmx;

import cl.clillo.ilumination.executor.PositionsQueue;
import cl.clillo.ilumination.executor.ListenerCambioPosicion;
import cl.clillo.ilumination.executor.ListenerFinMovimiento;
import cl.clillo.ilumination.executor.Punto;
import cl.clillo.ilumination.model.Coordinate;
import cl.clillo.ilumination.model.Point;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Data()
@NoArgsConstructor
@AllArgsConstructor
public class MovingHead extends Fixture implements ListenerFinMovimiento {

    // begin DMX channels
    private int pan;
    private int panFine;

    private int tilt;
    private int tiltFine;

    private int color;

    private int gobo;
    private int goboRotation;

    private int strobo;
    private int prisma;

    private int dimmer;
    private int speed;
    // end DMX channels

    private PositionsQueue lista;

    private boolean moviendose;
    private int velocidadActual;

    private long fakeX;
    private long fakeY;
    private Thread thLista = null;
    private Coordinate coordinates;
    private ArrayList<ListenerCambioPosicion> listenerCambioPosicion;
    private List<Point> pointList;

    public void init(){
        lista = new PositionsQueue(this);
        lista.setListenerFinMovimiento(this);

        thLista = new Thread(lista);
        thLista.setName("Lista de "+this);
        thLista.start();

        coordinates = new Coordinate(this);

        velocidadActual = 50;
    }

    public Map<String, Integer> getDMXMap(){
        Map<String, Integer> map = Maps.newHashMap();
        map.put("pan", pan);
        map.put("tilt", tilt);
        map.put("panFine", panFine);
        map.put("tiltFine", tiltFine);

        map.put("dimmer", dimmer);
        map.put("color", color);
        map.put("gobo", gobo);
        map.put("goboRotation", goboRotation);
        map.put("strobo", strobo);
        map.put("prisma", prisma);
        map.put("speed", speed);

        return map;
    }

    public void freeze(){
        lista.freeze();
    }

    public void moverA(long x, long y){
        moverA(x, y, velocidadActual);
    }

    private void moverA(long x, long y, int velocidad) {
        long deltax = Math.abs(x - coordinates.getX());
        long deltay = Math.abs(y - coordinates.getY());
        boolean steep = deltay > deltax;
        long error;
        int xstep = (coordinates.getX() < x)?1:-1;
        int ystep = (coordinates.getY() < y)?1:-1;
        int i;

        fakeX = coordinates.getX();
        fakeY = coordinates.getY();

        error = 0;

        if (steep) {
            for (i = 0; i < deltay; i++) {
                incy(ystep);

                error = error + deltax;
                if (2 * error >= deltay) {
                    incx(xstep);
                    error = error - deltay;
                }
                punto(velocidad);
            }
        } else {
            for (i = 0; i < deltax; i++) {
                incx(xstep);
                error = error + deltay;
                if (2 * error >= deltax) {
                    incy(ystep);
                    error = error - deltax;
                }
                punto(velocidad);
            }
        }
    }

    private void punto(int velocidad) {
        lista.agregar(new Punto(fakeX, fakeY, velocidad));

    }

    public void saltarA(long x, long y) {
        coordinates.setX(x);
        coordinates.setY(y);
        pointList = Lists.newArrayList(getPointList());

   //     System.out.println("Moviendo a "+x+","+y+"\t"+pointList);
        if (listenerCambioPosicion!=null)
            for (ListenerCambioPosicion l: listenerCambioPosicion)
                l.moverHasta(x, y, false);
    }

    public List<Point> getPointList() {
        return coordinates.getPointList(this.getDMXMap());
    }

    private void incx(long p) {
        if (p == 1)
            fakeX++;
        else
            fakeX--;
    }

    private void incy(long p) {
        if (p == 1)
            fakeY++;
        else
            fakeY--;
    }

    public long getPanMax(){ return 10000;}
    public long getPanMin(){ return 10000;}
    public long getTiltMax(){ return 10000;}
    public long getTiltMin(){ return 10000;}

    @Override
    public void finalizaMovimiento() {
        System.out.println("Finalizando: "+this);
     /*   if (programaMovimientoEnEjecucion!=null){
            programaMovimientoEnEjecucion.setEjecutandoMovimiento(false);
            programaMovimientoEnEjecucion.setSiguienteEjecucion();
            programaMovimientoEnEjecucion = null;
        }*/
    }
}
