package cl.clillo.lighting.gui.controller;

public interface ButtonSelectedListener {

    void selected(QLCButton qlcButton);

    void unSelected(QLCButton qlcButton);

    void onFinishChange(QLCButton qlcButton);
}
