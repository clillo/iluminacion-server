package cl.clillo.polygon.gui;

import cl.clillo.polygon.Point;
import lombok.Getter;

import java.awt.Color;
import java.awt.Graphics;

@Getter
public class GuiPoint extends Point  implements GuiObject {

    private static final double MAX = 1000;

    private static final int CANVAS_WIDTH = GuiApp.VisualizerMainPanel.WIDTH1;
    private static final int CANVAS_HEIGHT = GuiApp.VisualizerMainPanel.HEIGHT1;

    private final int screenX;
    private final int screenY;

    public GuiPoint(final double x, final double y) {
        super(x, y);
        screenX = (int) (((CANVAS_WIDTH * x)/MAX) + CANVAS_WIDTH/2.0);
        screenY = (int) (((CANVAS_HEIGHT * y*-1.0)/MAX)+ CANVAS_HEIGHT/2.0);
    }

    public GuiPoint(final Point realPoint) {
        this(realPoint.getX(), realPoint.getY());
    }

    public static double screenToRealX(int screenX){
        return (MAX * (screenX - CANVAS_WIDTH/2.0))/(CANVAS_WIDTH*1.0);
    }

    public static double screenToRealY(int screenY){
        return -1.0*(MAX * (screenY - CANVAS_HEIGHT/2.0))/(CANVAS_HEIGHT*1.0);
    }

    public boolean near(final int x, final int y){
        boolean nearX = (screenX - 10) < x && x < (screenX + 10);
        boolean nearY = (screenY - 10) < y && y < (screenY + 10);

        return nearX && nearY;
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(Color.RED);
        g.fillOval(screenX-5, screenY-5, 10, 10);
    }
}