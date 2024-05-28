package cl.clillo.polygon;

import cl.clillo.polygon.gui.GuiApp;

public class Main {

    public static void main(String[] args) {
        final Polygon p1 = new Polygon();
        p1.addEdge(-400.0, -400.0, 0.0, 400.0);
        p1.addEdge(400.0, -400.0,-400.0, -400.0);
        p1.addEdge(0.0, 400.0, 400.0, -400.0);

        final Polygon p2 = new Polygon();
        p2.addEdge(-400.0, -400.0, -200.0, 400.0);
        p2.addEdge(-200.0, 400.0,0.0, 100.0);
        p2.addEdge(0.0, 100.0, 200.0, 400.0);
        p2.addEdge(200.0, 400.0, 400.0, -400.0);
        p2.addEdge(400.0, -400.0, -400.0, -400.0);

        final Line l1 = new Line(-450.0, 200.0, 450.0, 200.0);

        final GuiApp guiApp = new GuiApp(new Scene(p2, l1));
        guiApp.start();
    }
}
