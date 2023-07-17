package cl.clillo.lighting.utils;

import cl.clillo.lighting.model.QLCEfx;
import cl.clillo.lighting.model.QLCEfxMultiLine;
import cl.clillo.lighting.model.QLCEfxSpline;
import cl.clillo.lighting.model.RealPoint;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.List;

public class EffectSplineEditPanel extends EffectEditPanel {

    private static final long serialVersionUID = -5869553409971473557L;

    private final QLCEfxSpline qlcEfx;

    private ScreenPoint sc1;
    private ScreenPoint sc2;
    private ScreenPoint sc3;

    public EffectSplineEditPanel(final QLCEfxSpline qlcEfx) {
        super(qlcEfx);
        this.qlcEfx = qlcEfx;
    }

    @Override
    protected void drawCanvas(Graphics g) {
        drawLineControlLines(g);
    }

    List<String> nodes = List.of();

    private void drawLineControlLines(Graphics g) {
        if (qlcEfx.getNodes()==null)
            return;

        double minX=Double.MAX_VALUE;
        double maxX=-1;
        double minY=Double.MAX_VALUE;;
        double maxY=-1;

        g.setColor(Color.CYAN);
        ScreenPoint pb = qlcEfx.getNodes().get(0).getScreenPoints()[0];
        ScreenPoint pa;
        for (int i = 1; i < qlcEfx.getNodes().size()-1; i++) {
            pa = qlcEfx.getNodes().get(i).getScreenPoints()[0];

            if (pa.getRealX()<minX)
                minX= pa.getRealX();

            if (pa.getRealX()>maxX)
                maxX= pa.getRealX();

            if (pa.getRealY()<minY)
                minY= pa.getRealY();

            if (pa.getRealY()>maxY)
                maxY= pa.getRealY();

            g.drawLine(pb.getScreenX(), pb.getScreenY(), pa.getScreenX(), pa.getScreenY());
            pb = pa;
        }

        pa = qlcEfx.getNodes().get(0).getScreenPoints()[0];
        g.drawLine(pb.getScreenX(), pb.getScreenY(), pa.getScreenX(), pa.getScreenY());

        sc1 = new ScreenPoint(minX+ (maxX - minX)/2, minY);
        sc3 = new ScreenPoint(minX, minY + (maxY -minY)/2);

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

        if (sc3!=null && sc3.isNear(mousePrevX, mousePrevY)) {
            this.setCursor(CURSOR_X);
            return;
        }

        if (sc1!=null && sc1.isNear(mousePrevX, mousePrevY)) {
            this.setCursor(CURSOR_Y);
            return;
        }

        this.setCursor(Cursor.getDefaultCursor());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    public void updateParametersMultiLine(final RealPoint leftUp, RealPoint rightDown) {
        final QLCEfxSpline qlcEfx = this.qlcEfx;
     //   qlcEfx.updateParameters(leftUp, rightDown);
        this.setQlcEfx(qlcEfx);

    }
}