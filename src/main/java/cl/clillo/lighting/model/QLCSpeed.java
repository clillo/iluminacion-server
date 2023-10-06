package cl.clillo.lighting.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class QLCSpeed {

    private int fadeIn;
    private int fadeOut;
    private int duration;
}
