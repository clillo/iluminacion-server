package cl.clillo.lighting.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QLCEfxFixtureData {

    private QLCRoboticFixture fixture;
    private boolean reverse;
    private double startOffset;


}
