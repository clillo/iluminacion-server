package cl.clillo.lighting.utils;

import lombok.Getter;

@Getter
public class ScreenPoint {

    private final double MAX = 65536;
    private final int CANVAS_WIDTH = FixtureRoboticPanel.WIDTH1;
    private final int CANVAS_HEIGHT = FixtureRoboticPanel.HEIGHT1;

    private final double realX;
    private final double realY;
    private final int screenX;
    private final int screenY;

    public ScreenPoint(double realX, double realY) {
        this.realX = realX;
        this.realY = realY;
        screenX = (int) ((CANVAS_WIDTH * realX)/MAX);
        screenY = (int) ((CANVAS_HEIGHT * realY)/MAX);
    }
}