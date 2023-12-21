package cl.clillo.lighting.model;

public interface Sequenceable {

    QLCDirection getDirection();

    QLCRunOrder getRunOrder();

    void setDirection(QLCDirection direction);

    int getSpeed();

    void setSpeed(int speed);
}
