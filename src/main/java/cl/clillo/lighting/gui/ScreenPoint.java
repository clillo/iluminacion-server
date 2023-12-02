package cl.clillo.lighting.gui;

import cl.clillo.lighting.gui.movements.EFXMConfigureMainPanel;
import cl.clillo.lighting.model.RealPoint;
import lombok.Getter;

@Getter
public class ScreenPoint {

    private final double MAX = 65536;
    private final int CANVAS_WIDTH = EFXMConfigureMainPanel.WIDTH1;
    private final int CANVAS_HEIGHT = EFXMConfigureMainPanel.HEIGHT1;

    private double realX;
    private double realY;
    private int screenX;
    private int screenY;

    private int fixtureId;

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

    public int getFixtureId() {
        return fixtureId;
    }

    public void setFixtureId(int fixtureId) {
        this.fixtureId = fixtureId;
    }

    @Override
    public String toString() {
        return realX+","+realY;
    }

    public void setRealX(double realX) {
        this.realX = realX;
        screenX = (int) ((CANVAS_WIDTH * realX)/MAX);
    }

    public void setRealY(double realY) {
        this.realY = realY;
        screenY = (int) ((CANVAS_HEIGHT * realY)/MAX);
    }
}