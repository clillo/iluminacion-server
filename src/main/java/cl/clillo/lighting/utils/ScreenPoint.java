package cl.clillo.lighting.utils;

import cl.clillo.lighting.model.RealPoint;
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

    public ScreenPoint(final double realX, final double realY) {
        this.realX = realX;
        this.realY = realY;
        screenX = (int) ((CANVAS_WIDTH * realX)/MAX);
        screenY = (int) ((CANVAS_HEIGHT * realY)/MAX);
    }

    public ScreenPoint(final RealPoint realPoint) {
        this.realX = realPoint.getX();
        this.realY = realPoint.getY();
        screenX = (int) ((CANVAS_WIDTH * realX)/MAX);
        screenY = (int) ((CANVAS_HEIGHT * realY)/MAX);
    }

    public boolean isNear(final double mouseRealX, final double mouseRealY){
        return Math.abs(mouseRealX - realX)<500 && Math.abs(mouseRealY - realY)<500;
    }

    public boolean isNear(final ScreenPoint screenPoint){
        return Math.abs(screenPoint.realX - realX)<500 && Math.abs(screenPoint.realY - realY)<500;
    }

    public boolean isEquals(final ScreenPoint screenPoint){
        return Math.abs(screenPoint.realX - realX)<500 && Math.abs(screenPoint.realY - realY)<500;
    }
}