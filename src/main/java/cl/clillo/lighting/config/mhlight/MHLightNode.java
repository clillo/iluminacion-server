package cl.clillo.lighting.config.mhlight;

import cl.clillo.lighting.model.Point;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class MHLightNode {

    private String fixture;
    private int dimmer;
    private int color;
    private int gobo;
    private int goboCristal;
    private int goboRotation;
    private int goboShake;
    private int strobo;
    private int prisma;
    private int focus;

    public Map<String, Integer> getDMXMap(){
        Map<String, Integer> map = Maps.newHashMap();
        map.put("dimmer", dimmer);
        map.put("color", color);
        map.put("gobo", gobo);
        map.put("goboCristal", goboCristal);
        map.put("goboRotation", goboRotation);
        map.put("goboShake", goboShake);
        map.put("strobo", strobo);
        map.put("prisma", prisma);
        map.put("focus", focus);
        return map;
    }

    public List<Point> getPointList(final Map<String, Integer> fixtureMap) {
        final List<Point> pointList = Lists.newArrayList();

        final Map<String, Integer> mhLightsMap = this.getDMXMap();

        for(String canal: fixtureMap.keySet()){
            if (mhLightsMap.containsKey(canal)) {
                int dmxCanal = fixtureMap.get(canal);
                int dmxValue = mhLightsMap.get(canal);
                pointList.add(Point.builder()
                        .canal(dmxCanal)
                        .dmx(dmxValue)
                        .build());
            }
        }

        return pointList;
    }
}
