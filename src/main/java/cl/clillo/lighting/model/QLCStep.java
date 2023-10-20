package cl.clillo.lighting.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@ToString
@Setter
public class QLCStep {

    private int id;
    private int fadeIn;
    private int hold;
    private int fadeOut;
    private List<QLCPoint> pointList;

    QLCStep(int id, int fadeIn, int hold, int fadeOut, List<QLCPoint> pointList) {
        this.id = id;
        this.fadeIn = fadeIn;
        this.hold = hold;
        this.fadeOut = fadeOut;
        this.pointList = pointList;
    }

    public static QLCStepBuilder builder() {
        return new QLCStepBuilder();
    }

    public List<QLCPoint> replaceDimmerValue(final int value) {
        final List<QLCPoint> newPointList = new ArrayList<>();

        for (QLCPoint point : pointList) {
            QLCPoint newPoint = point.replaceDimmerValue(value);
            newPointList.add(Objects.requireNonNullElse(newPoint, point));
        }

        return newPointList;
    }

    public List<QLCPoint> onlyDimmerValue(final int value) {
        final List<QLCPoint> newPointList = new ArrayList<>();

        for (QLCPoint point : pointList) {
            QLCPoint newPoint = point.replaceDimmerValue(value);
            if (newPoint != null)
                newPointList.add(newPoint);
        }

        return newPointList;
    }

    public static class QLCStepBuilder {
        private int id;
        private int fadeIn;
        private int hold;
        private int fadeOut;
        private List<QLCPoint> pointList;

        QLCStepBuilder() {
        }

        public QLCStepBuilder id(int id) {
            this.id = id;
            return this;
        }

        public QLCStepBuilder fadeIn(int fadeIn) {
            this.fadeIn = fadeIn;
            return this;
        }

        public QLCStepBuilder hold(int hold) {
            this.hold = hold;
            return this;
        }

        public QLCStepBuilder fadeOut(int fadeOut) {
            this.fadeOut = fadeOut;
            return this;
        }

        public QLCStepBuilder pointList(List<QLCPoint> pointList) {
            this.pointList = pointList;
            return this;
        }

        public QLCStep build() {
            return new QLCStep(this.id, this.fadeIn, this.hold, this.fadeOut, this.pointList);
        }


        public QLCStep buildFake() {
            return new QLCFakeStep(this.id, this.fadeIn, this.hold, this.fadeOut, this.pointList);
        }

    }
}
