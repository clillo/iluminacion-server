package cl.clillo.ilumination.fixture.dmx;

import cl.clillo.ilumination.executor.ColaPosiciones;
import cl.clillo.ilumination.executor.ListenerCambioPosicion;
import cl.clillo.ilumination.executor.ListenerFinMovimiento;
import cl.clillo.ilumination.executor.Punto;
import cl.clillo.ilumination.model.Point;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovingHead extends Fixture implements ListenerFinMovimiento {

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

    private ColaPosiciones lista;

    public void init(){
        lista = new ColaPosiciones(this);
        lista.setListenerFinMovimiento(this);

        thLista = new Thread(lista);
        thLista.setName("Lista de "+this);
        thLista.start();

        posX = getPanMax();
        posY = getTiltMax();
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


    public long getPosicionAleatoriaPan(Random rndPan){
        long ancho = getPanMax() - getPanMin();
        long pos =  rndPan.nextInt((int)ancho);
        return getPanMin() + pos;
    }

    public long getPosicionAleatoriaTilt(Random rndTilt){
        long alto = getTiltMax() - getTiltMin();
        long pos =  rndTilt.nextInt((int)alto);
        return getTiltMin() + pos;
    }

    public void freeze(){
        lista.freeze();
    }

    private boolean moviendose;
    private int velocidadActual;
    private long posX;
    private long posY;
    private long fakeX;
    private long fakeY;
    private Thread thLista = null;

    private ArrayList<ListenerCambioPosicion> listenerCambioPosicion;

    public void moverA(long x, long y){
        moverA(x, y, velocidadActual);
    }

    private void moverA(long x, long y, int velocidad) {
        long deltax = Math.abs(x - posX);
        long deltay = Math.abs(y - posY);
        boolean steep = deltay > deltax;
        long error;
        int xstep = (posX < x)?1:-1;
        int ystep = (posY < y)?1:-1;
        int i;

        fakeX = posX;
        fakeY = posY;

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

    public long getRealTilt(){return posY;}

    public long getRealPan(){return posX;}

    private List<Point> pointList;

    public void saltarA(long x, long y) {
        setPosX(x);
        setPosY(y);
        pointList = Lists.newArrayList(getPointList());

   //     System.out.println("Moviendo a "+x+","+y+"\t"+pointList);
        if (listenerCambioPosicion!=null)
            for (ListenerCambioPosicion l: listenerCambioPosicion)
                l.moverHasta(x, y, false);
    }

    public List<Point> getPointList() {
        final List<Point> pointList = Lists.newArrayList();
/*        int vpan = (int)(posX / 256);
        int vpanFine = (int)(posX % 256);

        int vtilt = (int)(posY/ 256);
        int vtiltFine = (int)(posY % 256);
*/
        int vpan = (int)(posX);
        int vpanFine = 0;

        int vtilt = (int)(posY);
        int vtiltFine = 0;


        pointList.add(Point.builder()
                .canal(pan)
                .dmx(vpan)
                .build());
        pointList.add(Point.builder()
                .canal(panFine)
                .dmx(vpanFine)
                .build());
        pointList.add(Point.builder()
                .canal(tilt)
                .dmx(vtilt)
                .build());
        pointList.add(Point.builder()
                .canal(tiltFine)
                .dmx(vtiltFine)
                .build());

        return pointList;
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
