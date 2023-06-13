package cl.clillo.ilumination.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class QLCPoint {

    private QLCFixture fixture;
    private int channel;
    private int dmxChannel;
    private int value;

}
