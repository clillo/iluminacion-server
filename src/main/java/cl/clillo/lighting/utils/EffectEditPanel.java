package cl.clillo.lighting.utils;

import cl.clillo.lighting.model.QLCEfx;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class EffectEditPanel extends JPanel implements MouseMotionListener, MouseListener, RoboticNotifiable, ActionListener {

    private static final long serialVersionUID = -5869553409971473557L;
    private final JTextField txtScreenPosition;
    private final JTextField txtCircleCenterX;
    private final JTextField txtCircleCenterY;
    private final JTextField txtCircleWidth;
    private final JTextField txtCircleHeight;
    private final JPanel canvas;
    private final JTextArea txtPointList;

    private static final double MAX_X = 65536;
    private static final double MAX_Y = 65536;

    private final List<ScreenPoint> pointList = new ArrayList<>();
    private QLCEfx qlcEfx;

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

            public void paint(Graphics g){
                super.paint(g);
                g.setColor(Color.gray);
                g.drawLine(canvas.getWidth()/2, 0, canvas.getWidth()/2, canvas.getHeight());
                g.drawLine(0, canvas.getHeight()/2, canvas.getWidth(), canvas.getHeight()/2);

                txtPointList.setText("");
                for (int i = 0; i< pointList.size(); i++){
                    ScreenPoint p = pointList.get(i);
                    if (i== pointList.size()-1) {
                        g.setColor(Color.RED);
                        g.fillOval(p.getScreenX(), p.getScreenY(), 8, 8);
                    }
                    else {
                        g.setColor(Color.WHITE);
                        g.drawOval(p.getScreenX(), p.getScreenY(), 8, 8);
                    }
                    txtPointList.append(p.getRealX()+","+ p.getRealY()+"\n");
                }
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
        txtCircleCenterX.setText(String.valueOf(qlcEfx.getCenterX()));
        txtCircleCenterY.setText(String.valueOf(qlcEfx.getCenterY()));
        txtCircleWidth.setText(String.valueOf(qlcEfx.getWidth()));
        txtCircleHeight.setText(String.valueOf(qlcEfx.getHeight()));
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        txtScreenPosition.setText(Math.round(screenToRealX(e.getX()))+","+ Math.round(screenToRealY(e.getY())));
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
    public void notify(double realX, double realY) {
        pointList.add(new ScreenPoint(realX, realY));
        canvas.repaint();
    }

    @Override
    public void clear() {
        pointList.clear();
        canvas.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(txtCircleCenterX))
            qlcEfx.setCenterX(Double.parseDouble(txtCircleCenterX.getText()));

        if (e.getSource().equals(txtCircleCenterY))
            qlcEfx.setCenterY(Double.parseDouble(txtCircleCenterY.getText()));

        if (e.getSource().equals(txtCircleWidth))
            qlcEfx.setWidth(Double.parseDouble(txtCircleWidth.getText()));

        if (e.getSource().equals(txtCircleHeight))
            qlcEfx.setHeight(Double.parseDouble(txtCircleHeight.getText()));
    }
}