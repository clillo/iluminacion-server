package cl.clillo.lighting.gui;

import cl.clillo.lighting.model.QLCEfx;
import cl.clillo.lighting.model.QLCEfxLine;
import cl.clillo.lighting.model.Show;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

public class EffectLineEditPanel extends EffectEditPanel {

    private static final long serialVersionUID = -5869553409971473557L;

    private final QLCEfxLine qlcEfx;

    private ScreenPoint sc1;
    private ScreenPoint sc2;
    private ScreenPoint sc3;
    private ScreenPoint sc4;
    private double mousePrevX=0;
    private double mousePrevY=0;

    public EffectLineEditPanel(final QLCEfxLine qlcEfx, final Show show) {
        super(qlcEfx, show);
        this.qlcEfx = qlcEfx;
    }


    @Override
    protected void drawCanvas(Graphics g) {
        drawLineControlLines(g);
    }

    private void drawLineControlLines(Graphics g) {
        if (qlcEfx.getNodes()==null)
            return;

        final QLCEfxLine qlcEfxLine = this.qlcEfx;
        sc1 = new ScreenPoint(qlcEfxLine.getOriginX(), qlcEfxLine.getOriginY());
        sc3 = new ScreenPoint(qlcEfxLine.getDestinyX(), qlcEfxLine.getDestinyY());

        g.setColor(Color.CYAN);
        g.drawLine(sc1.getScreenX(), sc1.getScreenY(), sc3.getScreenX(), sc3.getScreenY());

        drawCrossLine(g, sc1);
        drawCrossLine(g, sc3);
    }

    public void setQlcEfx(QLCEfx qlcEfx) {
        final QLCEfxLine qlcEfxLine = ((QLCEfxLine) qlcEfx);
        txtLineOriginX.setText(String.valueOf((int)qlcEfxLine.getOriginX()));
        txtLineOriginY.setText(String.valueOf((int)qlcEfxLine.getOriginY()));
        txtLineDestinyX.setText(String.valueOf((int)qlcEfxLine.getDestinyX()));
        txtLineDestinyY.setText(String.valueOf((int)qlcEfxLine.getDestinyY()));
    }

    @Override
    public void mouseDragged(MouseEvent e) {

        double x = screenToRealX(e.getX());
        double y = screenToRealY(e.getY());

        if (this.getCursor()==CURSOR_X ){
            updateParametersLine(Double.parseDouble(txtLineOriginX.getText()) , Double.parseDouble(txtLineOriginY.getText()), x, y);
        }


        if (this.getCursor()==CURSOR_Y){
            updateParametersLine(x, y, Double.parseDouble(txtLineDestinyX.getText()) , Double.parseDouble(txtLineDestinyY.getText()));
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mousePrevX = screenToRealX(e.getX());
        mousePrevY = screenToRealY(e.getY());
        txtScreenPosition.setText(Math.round(mousePrevX)+","+ Math.round(mousePrevY));

        if (sc3.isNear(mousePrevX, mousePrevY)) {
            this.setCursor(CURSOR_X);
            return;
        }


        if (sc1.isNear(mousePrevX, mousePrevY)) {
            this.setCursor(CURSOR_Y);
            return;
        }

        this.setCursor(Cursor.getDefaultCursor());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(txtLineOriginX) || e.getSource().equals(txtLineOriginX) ||
                e.getSource().equals(txtLineDestinyX) || e.getSource().equals(txtLineDestinyY)) {

            updateParametersLine(Double.parseDouble(txtLineOriginX.getText()), Double.parseDouble(txtLineOriginY.getText()),
                Double.parseDouble(txtLineDestinyX.getText()), Double.parseDouble(txtLineDestinyY.getText()));
        }

    }

    public void updateParametersLine(final double originX, final double originY, final double destinyX, final double destinyY) {
        final QLCEfxLine qlcEfx = this.qlcEfx;
        qlcEfx.updateParameters(originX, originY, destinyX, destinyY);
        this.setQlcEfx(qlcEfx);

    }
}