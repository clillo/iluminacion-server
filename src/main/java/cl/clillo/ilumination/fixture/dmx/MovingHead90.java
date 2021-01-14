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
/*
    public long getPanMax(){ return 23000;}
    public long getPanMin(){ return 0;}
    public long getTiltMax(){ return 31000;}
    public long getTiltMin(){ return 0;}*/

    public long getPanMax(){ return 88;}
    public long getPanMin(){ return 10;}
    public long getTiltMax(){ return 120;}
    public long getTiltMin(){ return 10;}
}
