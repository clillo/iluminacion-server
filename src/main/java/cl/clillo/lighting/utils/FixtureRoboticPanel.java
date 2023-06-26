package cl.clillo.lighting.utils;

import javax.swing.*;

public class FixtureRoboticPanel extends JPanel {

    private static final long serialVersionUID = -5869553409971473557L;

    public static final int WIDTH1 = 1024;
    public static final int HEIGHT1 = 900;

    private final EffectEditPanel pnlMovingHead1 = new EffectEditPanel();

    public FixtureRoboticPanel() {
        this.setLayout(null);
        this.setBounds(0, 0, WIDTH1+200, HEIGHT1);
        this.setLayout(null);
        pnlMovingHead1.setBounds(0, 0, WIDTH1+200, HEIGHT1);
        add(pnlMovingHead1);
    }

    public EffectEditPanel getPnlMovingHead1() {
        return pnlMovingHead1;
    }

}