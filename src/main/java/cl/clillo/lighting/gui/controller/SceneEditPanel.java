package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.gui.ScreenPoint;
import cl.clillo.lighting.gui.movements.EffectEditPanel;
import cl.clillo.lighting.model.QLCEfx;
import cl.clillo.lighting.model.QLCEfxScene;
import cl.clillo.lighting.model.QLCExecutionNode;
import cl.clillo.lighting.model.Show;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SceneEditPanel extends EffectEditPanel {

    private static final long serialVersionUID = -5869553409971473557L;

    private final QLCEfxScene qlcEfx;
    private List<ScreenPoint> scenePoints;

    private double mousePrevX=0;
    private double mousePrevY=0;

    private ScreenPoint screenPointSelected;

    public SceneEditPanel(final QLCEfxScene qlcEfx, final Show show) {
        super(null, show);
        this.qlcEfx = qlcEfx;
        qlcEfx.updateParameters(-1,0,0);
        scenePoints = new ArrayList<>();

        for (QLCExecutionNode node: qlcEfx.getNodes()) {
            ScreenPoint sc = node.getScreenPoints()[0];
            scenePoints.add(sc);

        }

    }

    @Override
    protected void drawCanvas(Graphics g) {
        for (ScreenPoint pb: scenePoints)
            g.fillOval(pb.getScreenX(), pb.getScreenY(), 8, 8);

    }

    @Override
    protected void setQlcEfx(QLCEfx qlcEfx) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

        double x = screenToRealX(e.getX());
        double y = screenToRealY(e.getY());

        if (this.getCursor()==CURSOR_CROSS && screenPointSelected!=null){
            updateParametersCircle(x, y);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mousePrevX = screenToRealX(e.getX());
        mousePrevY = screenToRealY(e.getY());
        txtScreenPosition.setText(Math.round(mousePrevX)+","+ Math.round(mousePrevY));
        screenPointSelected=null;
        for (ScreenPoint pb: scenePoints)
            if (Math.abs(mousePrevX- pb.getRealX())<400 && Math.abs(mousePrevY-pb.getRealY())<400) {
                this.setCursor(CURSOR_CROSS);
                screenPointSelected=pb;
                return;
            }

        this.setCursor(Cursor.getDefaultCursor());
    }

    @Override
    public void actionPerformed(ActionEvent e) {


    }

    public void updateParametersCircle(final double newX, final double newY) {
        final QLCEfxScene qlcEfx = this.qlcEfx;
        qlcEfx.updateParameters(screenPointSelected.getFixtureId(), newX, newY);
        this.setQlcEfx(qlcEfx);
        this.repaint();
    }
}