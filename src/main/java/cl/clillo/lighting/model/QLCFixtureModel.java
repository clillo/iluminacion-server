package cl.clillo.lighting.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class QLCFixtureModel {

    private String manufacturer;
    private String model;
    private String type;
    private String [] channels;


}
