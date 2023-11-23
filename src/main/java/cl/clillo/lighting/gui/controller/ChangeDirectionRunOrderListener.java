package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.executor.IOS2LEventListener;
import cl.clillo.lighting.model.QLCDirection;
import cl.clillo.lighting.model.QLCRunOrder;

public interface ChangeDirectionRunOrderListener {

    void change(QLCRunOrder runOrder, QLCDirection direction, IOS2LEventListener.Type type);
}
