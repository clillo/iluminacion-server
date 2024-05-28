package cl.clillo.polygon;

public class Line {

    private final Point p1;
    private final Point p2;

    private final double a;
    private final double b;
    private final double c;

    public Line(final Point p1, final Point p2) {
        this.p1 = p1;
        this.p2 = p2;

        this.a = p2.getY() - p1.getY();
        this.b = p1.getX() - p2.getX();
        this.c = p2.getX() * p1.getY() - p1.getX() * p2.getY();
    }

    public Line(final double x1, final double y1, final double x2, final double y2){
        this(new Point(x1, y1), new Point(x2, y2));
    }

    public Point getP1() {
        return p1;
    }

    public Point getP2() {
        return p2;
    }

    public int distance(final Point point) {
        final double x0 = point.getX();
        final double y0 = point.getY();
        final double d = (a * x0 + b * y0 + c) / Math.sqrt(a * a + b * b);
        if (d<0)
            return -1;
        if (d>0)
            return 1;
        return 0;
    }

}
