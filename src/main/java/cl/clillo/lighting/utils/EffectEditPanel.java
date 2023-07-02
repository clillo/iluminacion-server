package cl.clillo.lighting.utils;

import cl.clillo.lighting.model.QLCEfx;
import cl.clillo.lighting.model.QLCEfxCircle;
import cl.clillo.lighting.model.QLCEfxLine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

public class EffectEditPanel extends JPanel implements MouseMotionListener, MouseListener, RoboticNotifiable, ActionListener {

    private static final long serialVersionUID = -5869553409971473557L;

    private final Cursor CURSOR_CROSS = new Cursor(Cursor.CROSSHAIR_CURSOR);
    private final Cursor CURSOR_X = new Cursor(Cursor.E_RESIZE_CURSOR);
    private final Cursor CURSOR_Y = new Cursor(Cursor.N_RESIZE_CURSOR);

    private final JTextField txtScreenPosition;
    private final JTextField txtCircleCenterX;
    private final JTextField txtCircleCenterY;
    private final JTextField txtCircleWidth;
    private final JTextField txtCircleHeight;
    private final JTextField txtLineOriginX;
    private final JTextField txtLineOriginY;
    private final JTextField txtLineDestinyX;
    private final JTextField txtLineDestinyY;
    private final JPanel canvas;
    private final JTextArea txtPointList;

    private static final double MAX_X = 65536;
    private static final double MAX_Y = 65536;

    private final List<ScreenPoint> pointList = new ArrayList<>();

    private QLCEfx qlcEfx;
    private List<ScreenPoint> nodes;
    private ScreenPoint sc1;
    private ScreenPoint sc2;
    private ScreenPoint sc3;
    private ScreenPoint sc4;
    private double mousePrevX=0;
    private double mousePrevY=0;

    public EffectEditPanel() {
        setLayout(null);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(FixtureRoboticPanel.WIDTH1+ 20, 11, 123, 189);
        add(scrollPane);

        txtPointList = new JTextArea();
        txtPointList.setFont(new Font("Monospaced", Font.PLAIN, 9));
        scrollPane.setViewportView(txtPointList);

        canvas = new JPanel(){
            private static final long serialVersionUID = 9056031188937687827L;

            public void paint(final Graphics g){
                super.paint(g);
                g.setColor(Color.gray);
                g.drawLine(canvas.getWidth()/2, 0, canvas.getWidth()/2, canvas.getHeight());
                g.drawLine(0, canvas.getHeight()/2, canvas.getWidth(), canvas.getHeight()/2);

                if (qlcEfx instanceof QLCEfxCircle)
                    drawCrossLine(g, new ScreenPoint(((QLCEfxCircle)qlcEfx).getCenterX(), ((QLCEfxCircle)qlcEfx).getCenterY()));

                txtPointList.setText("");
                if (pointList!=null && pointList.size()>1){
                    g.setColor(Color.RED);
                    ScreenPoint p = pointList.get(pointList.size()-1);
                    g.fillOval(p.getScreenX()-4, p.getScreenY()-4, 8, 8);
                }

                if (qlcEfx instanceof QLCEfxCircle)
                    drawCircleControlLines(g);

                if (qlcEfx instanceof QLCEfxLine)
                    drawLineControlLines(g);
            }
        };

        canvas.setBackground(Color.BLACK);
        canvas.setBounds(10, 0, FixtureRoboticPanel.WIDTH1, FixtureRoboticPanel.HEIGHT1);
        canvas.addMouseMotionListener(this);
        canvas.addMouseListener(this);
        add(canvas);

        txtScreenPosition = buildTxt(240);
        txtCircleCenterX = buildTxt(280);
        txtCircleCenterY = buildTxt(320);
        txtCircleWidth = buildTxt(360);
        txtCircleHeight = buildTxt(400);

        txtLineOriginX = buildTxt(440);
        txtLineOriginY = buildTxt(480);
        txtLineDestinyX = buildTxt(520);
        txtLineDestinyY = buildTxt(560);
    }

    private void drawCircleControlLines(Graphics g) {
        if (nodes==null)
            return;

        double minX=Double.MAX_VALUE;
        double maxX=-1;
        double minY=Double.MAX_VALUE;;
        double maxY=-1;

        g.setColor(Color.CYAN);
        ScreenPoint pb = nodes.get(0);
        ScreenPoint pa;
        for (int i = 1; i < nodes.size()-1; i++) {
            pa = nodes.get(i);

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

        pa=nodes.get(0);
        g.drawLine(pb.getScreenX(), pb.getScreenY(), pa.getScreenX(), pa.getScreenY());

        sc1 = new ScreenPoint(minX+ (maxX - minX)/2, minY);
        sc3 = new ScreenPoint(minX, minY + (maxY -minY)/2);

        drawCrossLine(g, sc1);
        drawCrossLine(g, sc3);
    }

    private void drawLineControlLines(Graphics g) {
        if (nodes==null)
            return;

        final QLCEfxLine qlcEfxLine = ((QLCEfxLine) this.qlcEfx);
        sc1 = new ScreenPoint(qlcEfxLine.getOriginX(), qlcEfxLine.getOriginY());
        sc3 = new ScreenPoint(qlcEfxLine.getDestinyX(), qlcEfxLine.getDestinyY());

        g.setColor(Color.CYAN);
        g.drawLine(sc1.getScreenX(), sc1.getScreenY(), sc3.getScreenX(), sc3.getScreenY());

        drawCrossLine(g, sc1);
        drawCrossLine(g, sc3);


    }

    private void drawCrossLine(final Graphics g, final ScreenPoint point){
        g.setColor(Color.yellow);

        g.drawLine(point.getScreenX() - 10, point.getScreenY(), point.getScreenX()+10, point.getScreenY());
        g.drawLine(point.getScreenX() , point.getScreenY()-10, point.getScreenX(), point.getScreenY()+10);
    }

    private JTextField buildTxt(final int posY){
        final JTextField txt = new JTextField();
        txt.setBounds(FixtureRoboticPanel.WIDTH1+ 20, posY, 120, 20);
        txt.addActionListener(this);
        add(txt);
        return txt;
    }

    public void setQlcEfx(QLCEfx qlcEfx) {
        this.qlcEfx = qlcEfx;
        if (qlcEfx instanceof QLCEfxCircle) {
            final QLCEfxCircle qlcEfxCircle = ((QLCEfxCircle) this.qlcEfx);
            txtCircleCenterX.setText(String.valueOf((int)qlcEfxCircle.getCenterX()));
            txtCircleCenterY.setText(String.valueOf((int)qlcEfxCircle.getCenterY()));
            txtCircleWidth.setText(String.valueOf((int)qlcEfxCircle.getWidth()));
            txtCircleHeight.setText(String.valueOf((int)qlcEfxCircle.getHeight()));
            return;
        }

        if (qlcEfx instanceof QLCEfxLine) {
            final QLCEfxLine qlcEfxLine = ((QLCEfxLine) this.qlcEfx);
            txtLineOriginX.setText(String.valueOf((int)qlcEfxLine.getOriginX()));
            txtLineOriginY.setText(String.valueOf((int)qlcEfxLine.getOriginY()));
            txtLineDestinyX.setText(String.valueOf((int)qlcEfxLine.getDestinyX()));
            txtLineDestinyY.setText(String.valueOf((int)qlcEfxLine.getDestinyY()));
            return;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {

        double x = screenToRealX(e.getX());
        double y = screenToRealY(e.getY());

        if (this.getCursor()==CURSOR_CROSS){
            this.nodes = updateParametersCircle(x, y,
                    Double.parseDouble(txtCircleWidth.getText()), Double.parseDouble(txtCircleHeight.getText()));
        }

        if (this.getCursor()==CURSOR_X && qlcEfx instanceof QLCEfxCircle){
            double delta = -10;
            if (mousePrevX>x)
                delta=10;
            this.nodes = updateParametersCircle(Double.parseDouble(txtCircleCenterX.getText()), Double.parseDouble(txtCircleCenterY.getText()),
                    Double.parseDouble(txtCircleWidth.getText()) + delta, Double.parseDouble(txtCircleHeight.getText()));
        }

        if (this.getCursor()==CURSOR_X && qlcEfx instanceof QLCEfxLine){
            this.nodes = updateParametersLine(Double.parseDouble(txtLineOriginX.getText()) , Double.parseDouble(txtLineOriginY.getText()), x, y);
        }

        if (this.getCursor()==CURSOR_Y && qlcEfx instanceof QLCEfxCircle){
            double delta = -10;
            if (mousePrevY>y)
                delta=10;


            this.nodes = updateParametersCircle(Double.parseDouble(txtCircleCenterX.getText()), Double.parseDouble(txtCircleCenterY.getText()),
                Double.parseDouble(txtCircleWidth.getText()) , Double.parseDouble(txtCircleHeight.getText())+ delta);
        }

        if (this.getCursor()==CURSOR_Y && qlcEfx instanceof QLCEfxLine){
            this.nodes = updateParametersLine(x, y, Double.parseDouble(txtLineDestinyX.getText()) , Double.parseDouble(txtLineDestinyY.getText()));
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mousePrevX = screenToRealX(e.getX());
        mousePrevY = screenToRealY(e.getY());
        txtScreenPosition.setText(Math.round(mousePrevX)+","+ Math.round(mousePrevY));

        if (qlcEfx instanceof QLCEfxCircle)
            if (Math.abs(mousePrevX-((QLCEfxCircle)qlcEfx).getCenterX())<500 && Math.abs(mousePrevY-((QLCEfxCircle)qlcEfx).getCenterY())<500) {
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

    private double screenToRealX(int screenX){
        return (MAX_X * screenX)/(canvas.getWidth()*1.0);
    }

    private double screenToRealY(int screenY){
        return (MAX_Y * screenY)/(canvas.getHeight()*1.0);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
       // ScreenPoint p = new ScreenPoint();
        //p.setX(e.getX());
       // p.setY(e.getY());
        //pointList.add(p);
        canvas.repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void notify(double time) {
        pointList.add(nodes.get((int)time));
        canvas.repaint();
    }

    @Override
    public void clear() {
        pointList.clear();
        canvas.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(txtCircleCenterX) || e.getSource().equals(txtCircleCenterY) ||
                e.getSource().equals(txtCircleWidth) || e.getSource().equals(txtCircleHeight)) {

            if (qlcEfx instanceof QLCEfxCircle)
                this.nodes = updateParametersCircle(Double.parseDouble(txtCircleCenterX.getText()), Double.parseDouble(txtCircleCenterY.getText()),
                    Double.parseDouble(txtCircleWidth.getText()), Double.parseDouble(txtCircleHeight.getText()));

        }

        if (e.getSource().equals(txtLineOriginX) || e.getSource().equals(txtLineOriginX) ||
                e.getSource().equals(txtLineDestinyX) || e.getSource().equals(txtLineDestinyY)) {

            if (qlcEfx instanceof QLCEfxLine)
                this.nodes = updateParametersLine(Double.parseDouble(txtLineOriginX.getText()), Double.parseDouble(txtLineOriginY.getText()),
                        Double.parseDouble(txtLineDestinyX.getText()), Double.parseDouble(txtLineDestinyY.getText()));
        }

    }

    public void setNodes(final List<ScreenPoint> nodes) {
        this.nodes = nodes;
    }

    public List<ScreenPoint> updateParametersCircle(final double centerX, final double centerY, final double width, final double height) {
        final QLCEfxCircle qlcEfx = ((QLCEfxCircle)this.qlcEfx);
        return qlcEfx.updateParameters(centerX, centerY, width, height);
    }

    public List<ScreenPoint> updateParametersLine(final double originX, final double originY, final double destinyX, final double destinyY) {
        final QLCEfxLine qlcEfx = ((QLCEfxLine)this.qlcEfx);
        List<ScreenPoint> nodes = qlcEfx.updateParameters(originX, originY, destinyX, destinyY);
        this.setQlcEfx(qlcEfx);
        return nodes;
    }
}