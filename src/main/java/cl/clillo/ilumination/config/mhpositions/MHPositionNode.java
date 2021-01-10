package cl.clillo.ilumination.config.mhpositions;

import cl.clillo.ilumination.model.Point;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class MHPositionNode {

    private String fixture;
    private int pan;
    private int panFine;
    private int tilt;
    private int tiltFine;

    public Map<String, Integer> getDMXMap(){
        Map<String, Integer> map = Maps.newHashMap();
        map.put("pan", pan);
        map.put("tilt", tilt);
        map.put("panFine", panFine);
        map.put("tiltFine", tiltFine);
        return map;
    }

    public List<Point> getPointList(final Map<String, Integer> fixtureMap) {
        final List<Point> pointList = Lists.newArrayList();

        final Map<String, Integer> positionMap = this.getDMXMap();

        for(String canal: fixtureMap.keySet()){
            if (positionMap.containsKey(canal)) {
                int dmxCanal = fixtureMap.get(canal);
                int dmxValue = positionMap.get(canal);
                pointList.add(Point.builder()
                        .canal(dmxCanal)
                        .dmx(dmxValue)
                        .build());
            }
        }

        return pointList;
    }
}
