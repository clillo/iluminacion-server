package cl.clillo.lighting.model;

import cl.clillo.lighting.executor.Punto;
import cl.clillo.lighting.fixture.dmx.MovingHead;
import cl.clillo.lighting.utils.Bresenham;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Data
public class Coordinate {

    private static final char[] path1 = {'A', 'B', 'C', 'D'};
    private static final char[] path2 = {'E', 'F', 'G', 'H'};

    private int x;
    private int y;

    private MovingHead fixture;
    private Random rndPositions = new Random();

    private int lastPan = -1;
    private int lastPanFine;
    private int lastTilt;
    private int lastTiltFine;
    private int lastSpeed=-1;

    private int dmxPan;
    private int dmxPanFine;
    private int dmxTilt;
    private int dmxTiltFine;
    private int dmxSpeed;

    private int index;

    private int alfa;
    private int beta;

    public Coordinate(MovingHead fixture) {
        this.fixture = fixture;

        alfa = (fixture.getPanMax() - fixture.getPanMin())/2 + fixture.getPanMin();
        beta = (fixture.getTiltMax() - fixture.getTiltMin())/2 + fixture.getTiltMin();

        index = 0;
        if (fixture.getId().endsWith("-1")) {
            x = getRandom(alfa, fixture.getPanMax()); //A
            //x = 25_000;
        }else{
            x = getRandom(fixture.getPanMin(), alfa); //E
        }

        y = fixture.getTiltMin();

        final Map<String, Integer> dmxMap = fixture.getDMXMap();
        dmxPan = dmxMap.get("pan");
        dmxPanFine = dmxMap.get("panFine");
        dmxTilt = dmxMap.get("tilt");
        dmxTiltFine = dmxMap.get("tiltFine");
        dmxSpeed = dmxMap.get("speed");

    }

    private int getRandom(final int min, final int max){
        return rndPositions.nextInt(max - min)+min;
    }

    public List<Punto> fixtureMove(){
        int pan = x;
        int tilt = y;

        final char status;
        final char initStatus;

        if (fixture.getId().endsWith("-1")) {
            initStatus = path1[index];
            index = (index + 1) % path1.length;
            status = path1[index];
        }else{
            initStatus = path2[index];
            index = (index + 1) % path2.length;
            status = path2[index];
        }

        switch (status){
            case 'A':
                pan = getRandom(alfa, fixture.getPanMax());
              //  pan = 25_000;
                tilt = fixture.getTiltMin();
                break;
            case 'B':
                pan = fixture.getPanMin();
               // tilt = getRandom(fixture.getTiltMin(), beta);

                tilt = 7_300;
                break;
            case 'C':
                pan = getRandom(fixture.getPanMin(), alfa);
                //pan = 18_500;
                tilt = fixture.getTiltMax();

                break;
            case 'D':
                pan = fixture.getPanMax();
                tilt = getRandom(beta, fixture.getTiltMax());
              //  tilt = 25_000;
                break;

            case 'E':
                pan = getRandom(fixture.getPanMin(), alfa);
                tilt = fixture.getTiltMin();
                break;
            case 'F':
                pan = fixture.getPanMax();
                tilt = getRandom(fixture.getTiltMin(), beta);

                break;
            case 'G':
                pan = getRandom(alfa, fixture.getPanMax());
                tilt = fixture.getTiltMax();

                break;
            case 'H':
                pan = fixture.getPanMin();
                tilt = getRandom(beta, fixture.getTiltMax());
                break;
        }

     //   System.out.println(" desde "+initStatus+": " + fixture.getCoordinates().getX() +","+ fixture.getCoordinates().getY()+
      //          "\thasta "+status+ ": " +pan +","+ tilt);

        return Bresenham.moverA(fixture.getCoordinates().getX(), fixture.getCoordinates().getY(), pan, tilt);
    }

    public List<Point> getPointList(int x, int y) {
        this.x = x;
        this.y = y;
        final List<Point> pointList = Lists.newArrayList();

        int vPan = x / 256;
        int vPanFine = x % 256;

        int vTilt = y / 256;
        int vTiltFine = y % 256;

        lastPan = -1;
        if (lastPan != vPan) {
            pointList.add(Point.builder()
                    .canal(dmxPan)
                    .dmx(vPan)
                 //   .fixtureId(fixture.getId())
                    .build());
            lastPan = vPan;
        }

        lastPanFine = -1;
        if (vPanFine != lastPanFine) {
            pointList.add(Point.builder()
                    .canal(dmxPanFine)
                    .dmx(vPanFine)
               //     .fixtureId(fixture.getId())
                    .build());
            lastPanFine = vPanFine;
        }

        lastTilt = -1;
        if (vTilt != lastTilt) {
            pointList.add(Point.builder()
                    .canal(dmxTilt)
                    .dmx(vTilt)
               //     .fixtureId(fixture.getId())
                    .build());
            lastTilt = vTilt;
        }

        lastTiltFine = -1;
        if (vTiltFine != lastTiltFine) {
            pointList.add(Point.builder()
                    .canal(dmxTiltFine)
                    .dmx(vTiltFine)
                //    .fixtureId(fixture.getId())
                    .build());
            lastTiltFine = vTiltFine;

        }

        if (lastSpeed!=0) {
            pointList.add(Point.builder()
                    .canal(dmxSpeed)
                    .dmx(0)
              //      .fixtureId(fixture.getId())
                    .build());
            lastSpeed=0;
        }
        return pointList;
    }

}
