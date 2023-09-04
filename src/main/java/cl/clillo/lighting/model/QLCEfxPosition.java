package cl.clillo.lighting.model;

import cl.clillo.lighting.gui.ScreenPoint;
import lombok.Getter;

@Getter
public class QLCEfxPosition {

    private final int index;
    private final double x;
    private final double y;
    private final double vPan;
    private final double vPanFine;

    private final double vTilt;
    private final double vTiltFine;

    QLCEfxPosition(int index, double x, double y) {
        this.index = index;
        this.x = x;
        this.y = y;
        vPan = x / 256;
        vPanFine = x % 256;

        vTilt = y / 256;
        vTiltFine = y % 256;
    }

    public ScreenPoint buildScreenPoint(){
        return new ScreenPoint(x, y);
    }

    public int[] buildDataArray(){
        return new int[] {(int) vPan, (int) vTilt, (int) vPanFine, (int) vTiltFine};
    }

    public static QLCEfxPositionBuilder builder() {
        return new QLCEfxPositionBuilder();
    }

    public static class QLCEfxPositionBuilder {
        private int index;
        private double x;
        private double y;

        QLCEfxPositionBuilder() {
        }

        public QLCEfxPositionBuilder index(int index) {
            this.index = index;
            return this;
        }

        public QLCEfxPositionBuilder x(double x) {
            this.x = x;
            return this;
        }

        public QLCEfxPositionBuilder y(double y) {
            this.y = y;
            return this;
        }

        public QLCEfxPosition build() {
            return new QLCEfxPosition(this.index, this.x, this.y);
        }
    }
}
