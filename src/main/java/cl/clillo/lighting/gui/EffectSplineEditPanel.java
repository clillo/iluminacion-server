package cl.clillo.lighting.gui;

import cl.clillo.lighting.model.QLCEfx;
import cl.clillo.lighting.model.QLCEfxSpline;
import cl.clillo.lighting.model.RealPoint;
import cl.clillo.lighting.model.Show;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class EffectSplineEditPanel extends EffectEditPanel {

    private static final long serialVersionUID = -5869553409971473557L;

    private final QLCEfxSpline qlcEfx;
    private final List<ScreenPoint> controlPoints;
    private List<ScreenPoint> splinePoints;
    private int selectedScreenPoint;

    public EffectSplineEditPanel(final QLCEfxSpline qlcEfx, final Show show) {
        super(qlcEfx, show);
        this.qlcEfx = qlcEfx;
        controlPoints = new ArrayList<>(qlcEfx.getRealPoints().size());
        for (RealPoint realPoint: qlcEfx.getRealPoints())
            controlPoints.add(new ScreenPoint(realPoint));

        selectedScreenPoint = -1;
    }

    @Override
    protected void drawCanvas(Graphics g) {
        drawLineControlLines(g);
    }

    private void drawLineControlLines(Graphics g) {
        if (qlcEfx.getNodes()==null || controlPoints==null)
            return;

        g.setColor(Color.CYAN);

        splinePoints = new ArrayList<>();

        ScreenPoint pb = qlcEfx.getNodes().get(0).getScreenPoints()[0];
        ScreenPoint pa;
        for (int i = 1; i < qlcEfx.getNodes().size()-1; i++) {
            pa = qlcEfx.getNodes().get(i).getScreenPoints()[0];
            splinePoints.add(pa);

            g.drawLine(pb.getScreenX(), pb.getScreenY(), pa.getScreenX(), pa.getScreenY());
            pb = pa;
        }

        pa = qlcEfx.getNodes().get(0).getScreenPoints()[0];
        g.drawLine(pb.getScreenX(), pb.getScreenY(), pa.getScreenX(), pa.getScreenY());
        splinePoints.add(pa);

        for (ScreenPoint screenPoint: controlPoints)
           drawCrossLine(g, screenPoint);
    }

    public void setQlcEfx(QLCEfx qlcEfx) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        double x = screenToRealX(e.getX());
        double y = screenToRealY(e.getY());

        if (this.selectedScreenPoint !=-1 ){
            updateParametersMultiLine(selectedScreenPoint, x, y);
        }

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        double realX = screenToRealX(e.getX());
        double realY = screenToRealY(e.getY());
        txtScreenPosition.setText(Math.round(realX)+","+ Math.round(realY));
        selectedScreenPoint = -1;

        for (int i=0; i<controlPoints.size(); i++) {
            ScreenPoint screenPoint = controlPoints.get(i);

            if (screenPoint.isNear(realX, realY)) {
                this.setCursor(CURSOR_X);
                selectedScreenPoint = i;
                return;
            }
        }
        this.setCursor(Cursor.getDefaultCursor());
    }

    protected void doubleClick(int x, int y){
        double realX = screenToRealX(x);
        double realY = screenToRealY(y);
        appendPoint(realX, realY);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    public void appendPoint(double x, double y) {
        final QLCEfxSpline qlcEfx = this.qlcEfx;
        controlPoints.add(new ScreenPoint(x,y));

        List<RealPoint> updatedPoints = new ArrayList<>(controlPoints.size());
        for (ScreenPoint screenPoint1: controlPoints)
            updatedPoints.add(new RealPoint(screenPoint1));

        qlcEfx.updateParameters(updatedPoints);
        this.setQlcEfx(qlcEfx);

    }

    public void updateParametersMultiLine(final int screenPoint, double x, double y) {
        final QLCEfxSpline qlcEfx = this.qlcEfx;
        controlPoints.remove(screenPoint);
        controlPoints.add(screenPoint, new ScreenPoint(x,y));

        List<RealPoint> updatedPoints = new ArrayList<>(controlPoints.size());
        for (ScreenPoint screenPoint1: controlPoints)
            updatedPoints.add(new RealPoint(screenPoint1));

        qlcEfx.updateParameters(updatedPoints);
        this.setQlcEfx(qlcEfx);
        this.repaint();
    }

}