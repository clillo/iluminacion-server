package cl.clillo.polygon;

import java.util.Objects;

public class Point{

    private final double x;
    private final double y;

    private Point next;

    public Point(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point point)) return false;
        return Double.compare(getX(), point.getX()) == 0 && Double.compare(getY(), point.getY()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getX(), getY());
    }

    public void setNext(final Point next) {
        this.next = next;
    }

    public Point getNext() {
        return next;
    }
}
