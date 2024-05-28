package cl.clillo.polygon;

public class Edge extends Line{

    private Point cutPoint;

    public Edge(final Point v1, final Point v2) {
        super(v1, v2);

    }

    public boolean hasCuttingPoint(){
        return cutPoint!=null;
    }

    public void addCutPoint(final Line l1){
        this.cutPoint = intersection(l1);
    }

    public Point getCutPoint() {
        return cutPoint;
    }

    public Point intersection(final Line l1){
        double x1 = getP1().getX();
        double y1 = getP1().getY();
        double x2 = getP2().getX();
        double y2 = getP2().getY();

        double x3 = l1.getP1().getX();
        double y3 = l1.getP1().getY();
        double x4 = l1.getP2().getX();
        double y4 = l1.getP2().getY();

        double d = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        if (d == 0) {
            return null;
        }

        double t = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / d;
        double u = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / d;

        if (t >= 0 && t <= 1 && u >= 0 && u <= 1) {
            double x = x1 + t * (x2 - x1);
            double y = y1 + t * (y2 - y1);
            return new Point(x, y);
        }

        return null;
    }
}
