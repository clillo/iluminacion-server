package cl.clillo.ilumination.fixture.dmx;

import com.google.common.collect.Maps;
import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovingHead extends Fixture{

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

    public MovingHead(final String id){
        super(id);
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
}
