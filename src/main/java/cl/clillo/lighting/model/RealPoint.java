package cl.clillo.lighting.model;

import cl.clillo.lighting.utils.ScreenPoint;
import lombok.Data;

@Data
public class RealPoint {

    private double x;
    private double y;

    public RealPoint(final ScreenPoint screenPoint) {
        x = screenPoint.getRealX();
        y = screenPoint.getRealY();
    }

    RealPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static RealPointBuilder builder() {
        return new RealPointBuilder();
    }

    public static class RealPointBuilder {
        private double x;
        private double y;

        RealPointBuilder() {
        }

        public RealPointBuilder x(double x) {
            this.x = x;
            return this;
        }

        public RealPointBuilder y(double y) {
            this.y = y;
            return this;
        }

        public RealPoint build() {
            return new RealPoint(this.x, this.y);
        }

    }
}
