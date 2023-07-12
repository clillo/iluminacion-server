package cl.clillo.lighting.utils;

import cl.clillo.lighting.model.QLCEfx;
import cl.clillo.lighting.model.QLCEfxMultiLine;
import cl.clillo.lighting.model.RealPoint;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.List;

public class EffectMultiLineEditPanel extends EffectEditPanel {

    private static final long serialVersionUID = -5869553409971473557L;

    private final QLCEfxMultiLine qlcEfx;

    private ScreenPoint sc1;
    private ScreenPoint sc2;
    private ScreenPoint sc3;

    public EffectMultiLineEditPanel(final QLCEfxMultiLine qlcEfx) {
        super(qlcEfx);
        this.qlcEfx = qlcEfx;
    }

    @Override
    protected void drawCanvas(Graphics g) {
        drawLineControlLines(g);
    }

    List<String> nodes = List.of();

    private void drawLineControlLines(Graphics g) {
        if (nodes==null)
            return;

        final QLCEfxMultiLine qlcEfxLine = this.qlcEfx;
        RealPoint p1 = qlcEfxLine.getRealPoints().get(0);
        RealPoint initialPoint = p1;

        for(int i=1; i<qlcEfxLine.getRealPoints().size(); i++) {
            sc1 = new ScreenPoint(p1);
            sc2 = new ScreenPoint(qlcEfxLine.getRealPoints().get(i));
            g.setColor(Color.CYAN);
            g.drawLine(sc1.getScreenX(), sc1.getScreenY(), sc2.getScreenX(), sc2.getScreenY());
            p1 = qlcEfxLine.getRealPoints().get(i);
        }

        g.setColor(Color.CYAN);
        sc1 = new ScreenPoint(initialPoint);
        g.drawLine(sc1.getScreenX(), sc1.getScreenY(), sc2.getScreenX(), sc2.getScreenY());

        sc1 = new ScreenPoint(qlcEfx.getLeftUp());
        sc3 = new ScreenPoint(qlcEfx.getRightDown());

        g.setColor(Color.MAGENTA);
        g.drawLine(sc1.getScreenX(), sc1.getScreenY(), sc1.getScreenX(), sc3.getScreenY());
        g.drawLine(sc1.getScreenX(), sc3.getScreenY(), sc3.getScreenX(), sc3.getScreenY());
        g.drawLine(sc3.getScreenX(), sc3.getScreenY(), sc3.getScreenX(), sc1.getScreenY());
        g.drawLine(sc3.getScreenX(), sc1.getScreenY(), sc1.getScreenX(), sc1.getScreenY());

        drawCrossLine(g, sc1);
        drawCrossLine(g, sc3);
    }

    public void setQlcEfx(QLCEfx qlcEfx) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        double x = screenToRealX(e.getX());
        double y = screenToRealY(e.getY());

        if (this.getCursor()==CURSOR_X ){
            updateParametersMultiLine(qlcEfx.getLeftUp(), RealPoint.builder().x(x).y(y).build());
        }


        if (this.getCursor()==CURSOR_Y){
            updateParametersMultiLine(RealPoint.builder().x(x).y(y).build(), qlcEfx.getRightDown());
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        double mousePrevX = screenToRealX(e.getX());
        double mousePrevY = screenToRealY(e.getY());
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
    }

    public void updateParametersMultiLine(final RealPoint leftUp, RealPoint rightDown) {
        final QLCEfxMultiLine qlcEfx = this.qlcEfx;
        qlcEfx.updateParameters(leftUp, rightDown);
        this.setQlcEfx(qlcEfx);

    }
}