package cl.clillo.ilumination.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Point {

    private int canal;
    private int dmx;
    private String fixtureId;
}
