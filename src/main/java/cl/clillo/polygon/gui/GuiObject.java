package cl.clillo.polygon.gui;

import java.awt.Graphics;

public interface GuiObject {

    void paint(Graphics g);

    default boolean isVisible(){
        return true;
    }
}
