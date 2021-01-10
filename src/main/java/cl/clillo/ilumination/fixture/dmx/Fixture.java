package cl.clillo.ilumination.fixture.dmx;

import lombok.*;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Fixture {

    private String id;

    public Map<String, Integer> getFixtureMap(){
        Map<String, Integer> fixtureMap = null;
        if (this.getId().startsWith("mh-90")){
            fixtureMap = ((MovingHead90)this).getDMXMap();
        }

        if (this.getId().startsWith("mh-60")){
            fixtureMap = ((MovingHead60)this).getDMXMap();
        }
        return fixtureMap;
    }
}
