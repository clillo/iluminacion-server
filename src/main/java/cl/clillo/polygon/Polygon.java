package cl.clillo.polygon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Polygon {

    private final List<Edge> edgeList;

    public Polygon() {
        edgeList = new ArrayList<>();
    }

    public void addEdge(final Edge edge) {
        this.edgeList.add(edge);
    }

    public void addEdge(final double x1, final double y1, final double x2, final double y2) {
        this.addEdge(new Edge(new Point(x1, y1), new Point(x2, y2)));
    }

    public List<Edge> getEdgeList() {
        return edgeList;
    }

    public List<Polygon> cutByLine(final Line line) {
        final List<Polygon> polygonList = new ArrayList<>();
        for (Edge edge : edgeList)
            edge.addCutPoint(line);

        final Polygon cutPolygon1 = new Polygon();
        final Polygon cutPolygon2 = new Polygon();
        final Point sidePoint = new Point(0, -500.0);

        polygonList.add(cutPolygon1);
        polygonList.add(cutPolygon2);
        final int sign = line.distance(sidePoint);
        final List<Edge> dividedEdges = new ArrayList<>();

        for (Edge edge : edgeList){
            int signP1 = line.distance(edge.getP1());
            int signP2 = line.distance(edge.getP2());
            if (!edge.hasCuttingPoint()) {
                if (sign == signP1 &&  sign== signP2)
                    cutPolygon1.edgeList.add(edge);
                else
                    cutPolygon2.edgeList.add(edge);
            }else {
                dividedEdges.add(edge);
                if (signP1 == sign) {
                    cutPolygon1.edgeList.add(new Edge(edge.getP1(), edge.getCutPoint()));
                    cutPolygon2.edgeList.add(new Edge(edge.getP2(), edge.getCutPoint()));
                }else {
                    cutPolygon1.edgeList.add(new Edge(edge.getP2(), edge.getCutPoint()));
                    cutPolygon2.edgeList.add(new Edge(edge.getP1(), edge.getCutPoint()));
                }
            }
        }

        if (dividedEdges.size()%2!=0)
            return List.of(this);

        for (int i=0; i<dividedEdges.size(); i+=2){
            final Edge actualEdge = dividedEdges.get(i);
            final Edge nextEdge = dividedEdges.get(i+1);
            final Point p1 = actualEdge.getCutPoint();
            final Point p2 = nextEdge.getCutPoint();

            cutPolygon1.addEdge(new Edge(p1, p2));
            cutPolygon2.addEdge(new Edge(p1, p2));
        }

        if (dividedEdges.size()==2)
            return polygonList;

        polygonList.clear();

        polygonList.addAll(review(cutPolygon1));
        polygonList.addAll(review(cutPolygon2));

        return polygonList;
    }

    private List<Polygon> review(final Polygon polygon) {
        final List<Polygon> polygonList = new ArrayList<>();

        final Map<Point, List<Edge>> map = new HashMap<>();
        for (Edge edge : polygon.getEdgeList()) {
            Point p1 = edge.getP1();
            Point p2 = edge.getP2();
            List<Edge> e1 = map.get(p1);
            List<Edge> e2 = map.get(p2);

            if (e1==null) {
                e1 = new ArrayList<>();
                map.put(p1, e1);
            }
            if (e2==null) {
                e2 = new ArrayList<>();
                map.put(p2, e2);
            }

            e1.add(edge);
            e2.add(edge);

        }

        final Set<Edge> processed = new HashSet<>();

        for (Edge edge : polygon.getEdgeList()) {
            if (!processed.contains(edge)) {
                Polygon p = buildPolygon(edge, map, processed);
                if (p != null) {
                    polygonList.add(p);
                }
            }
        }

        return polygonList;
    }

    private Polygon buildPolygon(Edge initEdge, Map<Point, List<Edge>> map, Set<Edge> processed) {
        final Polygon polygon = new Polygon();
        final List<Edge> path = new ArrayList<>();
        path.add(initEdge);
        processed.add(initEdge);

        Point point = initEdge.getP2();

        while (true) {
            Edge nextEdge = null;
            for (Edge edge : map.get(point)) {
                if (!processed.contains(edge)) {
                    nextEdge = edge;
                    break;
                }
            }

            if (nextEdge == null)
                return null;

            path.add(nextEdge);
            processed.add(nextEdge);

            point = (nextEdge.getP1().equals(point)) ? nextEdge.getP2() : nextEdge.getP1();

            if (point.equals(initEdge.getP1())) {
                for (Edge e : path)
                    polygon.addEdge(e);
                return polygon;
            }
        }
    }
}
