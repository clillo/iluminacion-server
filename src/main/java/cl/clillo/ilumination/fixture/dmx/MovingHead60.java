package cl.clillo.ilumination.fixture.dmx;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Builder
@NoArgsConstructor
@Component
@Data
public class MovingHead60 extends MovingHead{
/*
    public long getPanMax(){ return 33000;}
    public long getPanMin(){ return 12000;}
    public long getTiltMax(){ return 35000;}
    public long getTiltMin(){ return 0;}
*/

    public long getPanMax(){ return 128;}
    public long getPanMin(){ return 55;}
    public long getTiltMax(){ return 130;}
    public long getTiltMin(){ return 10;}
}
