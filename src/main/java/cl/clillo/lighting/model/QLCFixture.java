package cl.clillo.lighting.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class QLCFixture {

    private String manufacturer;
    private String model;
    private String mode;
    private int id;
    private String name;
    private int universe;
    private int address;
    private int channels;
    private QLCFixtureModel fixtureModel;

}
