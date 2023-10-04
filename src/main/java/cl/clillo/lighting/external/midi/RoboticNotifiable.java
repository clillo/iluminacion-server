package cl.clillo.lighting.external.midi;

import cl.clillo.lighting.model.QLCExecutionNode;

public interface RoboticNotifiable {

    default void notify(double time){}

    default void notify(double[] timePos){}

    default void notify(final QLCExecutionNode node){}

    default void clear(){}
}
