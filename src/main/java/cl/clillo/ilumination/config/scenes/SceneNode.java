package cl.clillo.ilumination.config.scenes;

import cl.clillo.ilumination.model.Point;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class SceneNode {

    private String fixture;
    private int dimmer;
    private int speed;
    private String lights;
    private String positions;

    private Map<String, Integer> getDMXMap(){
        final Map<String, Integer> map = Maps.newHashMap();
        map.put("dimmer", dimmer);
        map.put("speed", speed);
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
