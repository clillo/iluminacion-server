package cl.clillo.lighting.gui;

import cl.clillo.lighting.model.QLCEfx;
import cl.clillo.lighting.model.QLCEfxCircle;
import cl.clillo.lighting.model.Show;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

public class EffectCircleEditPanel extends EffectEditPanel {

    private static final long serialVersionUID = -5869553409971473557L;

    private final QLCEfxCircle qlcEfx;

    private ScreenPoint sc1;
    private ScreenPoint sc2;
    private ScreenPoint sc3;
    private ScreenPoint sc4;
    private double mousePrevX=0;
    private double mousePrevY=0;

    public EffectCircleEditPanel(final QLCEfxCircle qlcEfx, final Show show) {
        super(qlcEfx, show);
        this.qlcEfx = qlcEfx;
    }


    @Override
    protected void drawCanvas(Graphics g) {
        drawCrossLine(g, new ScreenPoint(this.qlcEfx.getCenterX(), this.qlcEfx.getCenterY()));
        drawCircleControlLines(g);
    }

    private void drawCircleControlLines(final Graphics g) {

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

    public void setQlcEfx(final QLCEfx qlcEfx) {
        final QLCEfxCircle qlcEfxCircle = ((QLCEfxCircle) qlcEfx);
        txtCircleCenterX.setText(String.valueOf((int)qlcEfxCircle.getCenterX()));
        txtCircleCenterY.setText(String.valueOf((int)qlcEfxCircle.getCenterY()));
        txtCircleWidth.setText(String.valueOf((int)qlcEfxCircle.getWidth()));
        txtCircleHeight.setText(String.valueOf((int)qlcEfxCircle.getHeight()));

    }

    @Override
    public void mouseDragged(MouseEvent e) {

        double x = screenToRealX(e.getX());
        double y = screenToRealY(e.getY());

        if (this.getCursor()==CURSOR_CROSS){
            updateParametersCircle(x, y,
                    Double.parseDouble(txtCircleWidth.getText()), Double.parseDouble(txtCircleHeight.getText()));
        }

        if (this.getCursor()==CURSOR_X){
            double delta = -10;
            if (mousePrevX>x)
                delta=10;
            updateParametersCircle(Double.parseDouble(txtCircleCenterX.getText()), Double.parseDouble(txtCircleCenterY.getText()),
                    Double.parseDouble(txtCircleWidth.getText()) + delta, Double.parseDouble(txtCircleHeight.getText()));
        }

        if (this.getCursor()==CURSOR_Y ){
            double delta = -10;
            if (mousePrevY>y)
                delta=10;

            updateParametersCircle(Double.parseDouble(txtCircleCenterX.getText()), Double.parseDouble(txtCircleCenterY.getText()),
                Double.parseDouble(txtCircleWidth.getText()) , Double.parseDouble(txtCircleHeight.getText())+ delta);
        }

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mousePrevX = screenToRealX(e.getX());
        mousePrevY = screenToRealY(e.getY());
        txtScreenPosition.setText(Math.round(mousePrevX)+","+ Math.round(mousePrevY));

            if (Math.abs(mousePrevX- qlcEfx.getCenterX())<500 && Math.abs(mousePrevY-((QLCEfxCircle)qlcEfx).getCenterY())<500) {
                this.setCursor(CURSOR_CROSS);
                return;
            }

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
        if (e.getSource().equals(txtCircleCenterX) || e.getSource().equals(txtCircleCenterY) ||
                e.getSource().equals(txtCircleWidth) || e.getSource().equals(txtCircleHeight)) {

             updateParametersCircle(Double.parseDouble(txtCircleCenterX.getText()), Double.parseDouble(txtCircleCenterY.getText()),
                Double.parseDouble(txtCircleWidth.getText()), Double.parseDouble(txtCircleHeight.getText()));
        }

    }

    public void updateParametersCircle(final double centerX, final double centerY, final double width, final double height) {
        final QLCEfxCircle qlcEfx = this.qlcEfx;
        qlcEfx.updateParameters(centerX, centerY, width, height);
        this.setQlcEfx(qlcEfx);
        this.repaint();
    }
}