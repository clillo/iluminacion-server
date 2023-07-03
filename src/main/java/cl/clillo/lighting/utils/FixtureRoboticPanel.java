package cl.clillo.lighting.utils;

import cl.clillo.lighting.model.QLCEfx;
import cl.clillo.lighting.model.QLCEfxCircle;
import cl.clillo.lighting.model.QLCEfxLine;

import javax.swing.*;

public class FixtureRoboticPanel extends JPanel {

    private static final long serialVersionUID = -5869553409971473557L;

    public static final int WIDTH1 = 1024;
    public static final int HEIGHT1 = 900;

    private final EffectEditPanel pnlMovingHead1;

    public FixtureRoboticPanel(final QLCEfx qlcEfx) {

        if (qlcEfx instanceof QLCEfxCircle)
            pnlMovingHead1 = new EffectCircleEditPanel((QLCEfxCircle) qlcEfx);
        else
            pnlMovingHead1 = new EffectLineEditPanel((QLCEfxLine) qlcEfx);

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