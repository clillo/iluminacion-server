package cl.clillo.polygon.gui;

import cl.clillo.polygon.Edge;
import cl.clillo.polygon.Polygon;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

public class GuiPolygon extends Polygon implements GuiObject {

    private final List<GuiEdge> edgeList;
    private final Color color;
    private boolean visible = true;

    public GuiPolygon(final Polygon polygon, final Color color) {
        super();
        this.color = color;
        edgeList = new ArrayList<>();
        for (Edge e: polygon.getEdgeList())
            edgeList.add(new GuiEdge(e.getP1(), e.getP2()));
    }

    @Override
    public void paint(Graphics g) {
        for (GuiEdge e: edgeList)
            e.paint(g, color);
    }

    @Override
    public String toString() {
        if (color == Color.ORANGE)
            return "ORANGE";
        if (color == Color.CYAN)
            return "CYAN";
        if (color == Color.WHITE)
            return "WHITE";
        if (color == Color.PINK)
            return "PINK";
        if (color == Color.GREEN)
            return "GREEN";
        if (color == Color.MAGENTA)
            return "MAGENTA";
        if (color == Color.GRAY)
            return "GRAY";
        if (color == Color.LIGHT_GRAY)
            return "LIGHT_GRAY";
        return "nac";
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
