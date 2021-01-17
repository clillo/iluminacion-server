package cl.clillo.ilumination.fixture.dmx;

import cl.clillo.ilumination.executor.ListenerCambioPosicion;
import cl.clillo.ilumination.executor.ListenerFinMovimiento;
import cl.clillo.ilumination.model.Coordinate;
import cl.clillo.ilumination.model.Point;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private Coordinate coordinates;
    private ArrayList<ListenerCambioPosicion> listenerCambioPosicion;

    public void init(){
        coordinates = new Coordinate(this);

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
     //   lista.freeze();
    }

    public List<Point> getPointList(int x, int y) {
        return coordinates.getPointList(x, y);
    }

    public int getPanMax(){ return 10000;}
    public int getPanMin(){ return 10000;}
    public int getTiltMax(){ return 10000;}
    public int getTiltMin(){ return 10000;}

    @Override
    public void finalizaMovimiento() {
        System.out.println("Finalizando: "+this);
    }
}
