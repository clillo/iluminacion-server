package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.gui.movements.EffectCircleEditPanel;
import cl.clillo.lighting.gui.movements.EffectEditPanel;
import cl.clillo.lighting.gui.movements.EffectLineEditPanel;
import cl.clillo.lighting.gui.movements.EffectMultiLineEditPanel;
import cl.clillo.lighting.gui.movements.EffectSplineEditPanel;
import cl.clillo.lighting.model.QLCEfxCircle;
import cl.clillo.lighting.model.QLCEfxLine;
import cl.clillo.lighting.model.QLCEfxMultiLine;
import cl.clillo.lighting.model.QLCEfxSpline;
import cl.clillo.lighting.model.QLCFunction;
import cl.clillo.lighting.model.QLCScene;
import cl.clillo.lighting.model.Show;

import javax.swing.JPanel;

public class EFXMConfigureMainPanel extends JPanel {

    private static final long serialVersionUID = -5869553409971473557L;

    public static final int WIDTH1 = 1324;
    public static final int HEIGHT1 = 900;

    private final Show showSelected;
    private final EFXMConfigureJFrame parent;
    private final EffectEditPanel editPanel;

    public EFXMConfigureMainPanel(final Show showSelected, final EFXMConfigureJFrame parent) {
        this.showSelected = showSelected;
        this.parent = parent;

        editPanel = buildPanel(showSelected);
        if (editPanel != null) {
            editPanel.setParent(parent);
            editPanel.setBounds(0, 0, WIDTH1 + 300, HEIGHT1);

            this.add(editPanel);

            //((QLCEfxExecutor)showSelected.getStepExecutor()).setRoboticNotifiable(editPanel);
        }

        this.setBounds(0, 0, WIDTH1 + 300, HEIGHT1);
        this.setLayout(null);
    }

    private EffectEditPanel buildPanel(final Show show) {
        final QLCFunction qlcEfx = show.getFunction();
        if (qlcEfx instanceof QLCScene && ((QLCScene)qlcEfx).getQlcEfxScene()!=null)
            return new SceneEditPanel(((QLCScene)qlcEfx).getQlcEfxScene(), show);

        if (qlcEfx instanceof QLCEfxCircle)
            return new EffectCircleEditPanel((QLCEfxCircle) qlcEfx, show);
        if (qlcEfx instanceof QLCEfxLine)
            return new EffectLineEditPanel((QLCEfxLine) qlcEfx, show);
        if (qlcEfx instanceof QLCEfxMultiLine)
            return new EffectMultiLineEditPanel((QLCEfxMultiLine) qlcEfx, show);
        if (qlcEfx instanceof QLCEfxSpline)
            return new EffectSplineEditPanel((QLCEfxSpline) qlcEfx, show);

        return null;
    }

    public PositionAdjustable getPositionAdjustable(){
        return editPanel instanceof SceneEditPanel? (PositionAdjustable) editPanel :null;
    }
}