package cl.clillo.lighting.fixture.dmx;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Builder
@NoArgsConstructor
@Component
@Data
public class MovingHead60 extends MovingHead{

    public int getPanMax(){ return 30_500;}
    public int getPanMin(){ return 14_000;}
    public int getTiltMax(){ return 31_500;}
    public int getTiltMin(){ return 2_400;}

/*
    public int getPanMax(){ return 128;}
    public int getPanMin(){ return 55;}
    public int getTiltMax(){ return 130;}
    public int getTiltMin(){ return 10;}

 */
}
