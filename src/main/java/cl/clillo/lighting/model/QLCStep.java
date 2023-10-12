package cl.clillo.lighting.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Builder
@ToString
@Setter
public class QLCStep {

    private int id;
    private int fadeIn;
    private int hold;
    private int fadeOut;
    private List<QLCPoint> pointList;

    public List<QLCPoint> replaceDimmerValue(final int value){
        final List<QLCPoint> newPointList = new ArrayList<>();

        for (QLCPoint point: pointList){
            QLCPoint newPoint = point.replaceDimmerValue(value);
            newPointList.add(Objects.requireNonNullElse(newPoint, point));
        }

        return newPointList;
    }

    public List<QLCPoint> onlyDimmerValue(final int value){
        final List<QLCPoint> newPointList = new ArrayList<>();

        for (QLCPoint point: pointList){
            QLCPoint newPoint = point.replaceDimmerValue(value);
            if (newPoint!=null)
                newPointList.add(newPoint);
        }

        return newPointList;
    }
}
