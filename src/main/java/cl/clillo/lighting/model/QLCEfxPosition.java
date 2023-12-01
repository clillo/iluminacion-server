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

    QLCEfxPosition(int index, int pan, int panFine, int tilt, int tiltFine){
        this.index = index;
        this.x = pan*256.0+panFine*1.0;
        this.y = tilt*256.0+tiltFine*1.0;

        vPan = x / 256;
        vPanFine = x % 256;

        vTilt = y / 256;
        vTiltFine = y % 256;
    }

    public ScreenPoint buildScreenPoint(int fixtureId){
        ScreenPoint screenPoint = new ScreenPoint(x, y);
        screenPoint.setFixtureId(fixtureId);
        return screenPoint;
    }

    public int[] buildDataArray(){
        return new int[] {(int) vPan, (int) vTilt, (int) vPanFine, (int) vTiltFine};
    }

    public int[] buildSimpleDataArray(){
        return new int[] {(int) vPan, (int) vTilt};
    }

    public static QLCEfxPositionBuilder builder() {
        return new QLCEfxPositionBuilder();
    }

    public static class QLCEfxPositionBuilder {
        private int index;
        private double x;
        private double y;
        private int pan = -1;
        private int panFine = -1;

        private int tilt = -1;
        private int tiltFine = -1;

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

        public QLCEfxPositionBuilder pan(int pan, int panFine) {
            this.pan = pan;
            this.panFine = panFine;
            return this;
        }

        public QLCEfxPositionBuilder tilt(int tilt, int tiltFine) {
            this.tilt = tilt;
            this.tiltFine = tiltFine;
            return this;
        }

        public QLCEfxPosition build() {
            if (pan!=1 && panFine!=-1 && tilt!=-1 && tiltFine!=-1)
                return new QLCEfxPosition(this.index, pan, panFine, tilt, tiltFine);
            return new QLCEfxPosition(this.index, this.x, this.y);
        }
    }
}
