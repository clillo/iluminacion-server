package cl.clillo.polygon.gui;

import cl.clillo.polygon.Line;
import cl.clillo.polygon.Point;

import java.awt.Color;
import java.awt.Graphics;

public class GuiLine extends Line implements GuiObject {

    private final GuiPoint gp1;
    private final GuiPoint gp2;

    public GuiLine(final Point p1, final Point p2) {
        super(p1, p2);

        gp1 = new GuiPoint(p1);
        gp2 = new GuiPoint(p2);
    }

    public GuiLine(final Line line) {
        this(line.getP1(), line.getP2());
    }

    @Override
    public void paint(final Graphics g) {
        paint(g, Color.YELLOW);
    }

    public void paint(final Graphics g, Color c) {
        gp1.paint(g);
        gp2.paint(g);

        g.setColor(c);
        g.drawLine(gp1.getScreenX(), gp1.getScreenY(), gp2.getScreenX(), gp2.getScreenY());
    }

    public boolean nearP1(final int x, final int y){
        return gp1.near(x, y);
    }

    public boolean nearP2(final int x, final int y){
        return gp2.near(x, y);
    }
}
