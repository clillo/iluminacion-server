package cl.clillo.ilumination.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class QLCStep {

    private int fadeIn;
    private int hold;
    private int fadeOut;
    private List<QLCPoint> pointList;

}
