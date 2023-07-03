package cl.clillo.lighting.utils;

import cl.clillo.lighting.model.QLCEfx;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

public abstract class EffectEditPanel extends JPanel implements MouseMotionListener, MouseListener, RoboticNotifiable, ActionListener {

    private static final long serialVersionUID = -5869553409971473557L;

    protected final Cursor CURSOR_CROSS = new Cursor(Cursor.CROSSHAIR_CURSOR);
    protected final Cursor CURSOR_X = new Cursor(Cursor.E_RESIZE_CURSOR);
    protected final Cursor CURSOR_Y = new Cursor(Cursor.N_RESIZE_CURSOR);

    protected final JTextField txtScreenPosition;
    protected final JTextField txtCircleCenterX;
    protected final JTextField txtCircleCenterY;
    protected final JTextField txtCircleWidth;
    protected JTextField txtCircleHeight;
    protected final JTextField txtLineOriginX;
    protected final JTextField txtLineOriginY;
    protected final JTextField txtLineDestinyX;
    protected final JTextField txtLineDestinyY;
    protected JPanel canvas;
    private final JTextArea txtPointList;

    private static final double MAX_X = 65536;
    private static final double MAX_Y = 65536;

    private final List<ScreenPoint> pointList = new ArrayList<>();


    protected List<ScreenPoint> nodes;

    public EffectEditPanel(QLCEfx qlcEfx) {
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

                txtPointList.setText("");
                if (pointList!=null && pointList.size()>1){
                    g.setColor(Color.RED);
                    ScreenPoint p = pointList.get(pointList.size()-1);
                    g.fillOval(p.getScreenX()-4, p.getScreenY()-4, 8, 8);
                }

                drawCanvas(g);
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

        setQlcEfx(qlcEfx);
    }

    protected abstract void drawCanvas(final Graphics g);

    protected void drawCrossLine(final Graphics g, final ScreenPoint point){
        g.setColor(Color.yellow);

        g.drawLine(point.getScreenX() - 10, point.getScreenY(), point.getScreenX()+10, point.getScreenY());
        g.drawLine(point.getScreenX() , point.getScreenY()-10, point.getScreenX(), point.getScreenY()+10);
    }

    protected JTextField buildTxt(final int posY){
        final JTextField txt = new JTextField();
        txt.setBounds(FixtureRoboticPanel.WIDTH1+ 20, posY, 120, 20);
        txt.addActionListener(this);
        add(txt);
        return txt;
    }

    protected abstract void setQlcEfx(QLCEfx qlcEfx);

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    protected double screenToRealX(int screenX){
        return (MAX_X * screenX)/(canvas.getWidth()*1.0);
    }

    protected double screenToRealY(int screenY){
        return (MAX_Y * screenY)/(canvas.getHeight()*1.0);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

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

    }

    public void setNodes(final List<ScreenPoint> nodes) {
        this.nodes = nodes;
    }
}