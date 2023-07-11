package cl.clillo.lighting.utils;

public interface RoboticNotifiable {

    void notify(double time);

    void notify(double[] timePos);

    void clear();
}
