package cl.clillo.ilumination.fixture.dmx;

import com.google.common.collect.Maps;
import lombok.*;
import org.springframework.stereotype.Component;

import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class MovingHead90 extends MovingHead{

    private int goboCristal;
    private int goboCristalShake;
    private int goboColorShake;
    private int goboNormalShake;
    private int focus;

    public Map<String, Integer> getDMXMap(){
        Map<String, Integer> map = super.getDMXMap();
        map.put("goboCristal", goboCristal);
        map.put("goboCristalShake", goboCristalShake);
        map.put("goboColorShake", goboColorShake);
        map.put("goboNormalShake", goboNormalShake);
        map.put("focus", focus);

        return map;
    }
}
