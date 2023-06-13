package cl.clillo.lighting.config.scenes;

import cl.clillo.lighting.model.Point;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SceneNode {

    private String fixture;
    private int dimmer;
    private int speed;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String lights;
    @JsonInclude(JsonInclude.Include.NON_NULL)
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
