package cl.clillo.lighting.gui.movements;

import cl.clillo.lighting.gui.ScreenPoint;
import cl.clillo.lighting.external.midi.RoboticNotifiable;
import cl.clillo.lighting.model.QLCEfx;
import cl.clillo.lighting.model.QLCExecutionNode;
import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.model.ShowCollection;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public abstract class EffectEditPanel extends JPanel implements MouseMotionListener, RoboticNotifiable, ActionListener {

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
    protected JButton btnSave;
    protected JButton btnRun;

    private static final double MAX_X = 65536;
    private static final double MAX_Y = 65536;

    private ScreenPoint[] screenPoints;
    private final QLCEfx qlcEf;
    private final Show show;

    public EffectEditPanel(final QLCEfx qlcEfx, final Show show) {
        setLayout(null);
        this.qlcEf = qlcEfx;
        this.show = show;

        canvas = new JPanel(){
            private static final long serialVersionUID = 9056031188937687827L;

            public void paint(final Graphics g){
                super.paint(g);
                g.setColor(Color.gray);
                g.drawLine(canvas.getWidth()/2, 0, canvas.getWidth()/2, canvas.getHeight());
                g.drawLine(0, canvas.getHeight()/2, canvas.getWidth(), canvas.getHeight()/2);

                if (screenPoints!=null){
                    char index=0;
                    for (ScreenPoint screenPoint : screenPoints) {
                        char [] str = { (char)('0'+(index++))};
                        g.setColor(Color.RED);
                        g.fillOval(screenPoint.getScreenX() - 4, screenPoint.getScreenY() - 4, 8, 8);
                        g.drawChars(str,0,1,screenPoint.getScreenX()+10 , screenPoint.getScreenY()+10);
                    }

                }
                drawCanvas(g);

            }
        };

        canvas.setBackground(Color.BLACK);
        canvas.setBounds(10, 0, EFXMConfigureMainPanel.WIDTH1, EFXMConfigureMainPanel.HEIGHT1);
        canvas.addMouseMotionListener(this);
        canvas.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && event.getButton() == MouseEvent.BUTTON1) {
                    doubleClick(event.getX(), event.getY());
                }
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
        });
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

        btnSave = new JButton();
        btnSave.setText("Save");
        btnSave.setBounds(EFXMConfigureMainPanel.WIDTH1+ 20, 50, 120, 20);

        btnSave.addActionListener(e -> save());
        add(btnSave);

        btnRun = new JButton();
        btnRun.setText("Start/Stop");
        btnRun.setBounds(EFXMConfigureMainPanel.WIDTH1+ 20, 10, 120, 20);

        btnRun.addActionListener(e -> run());
        add(btnRun);
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
        txt.setBounds(EFXMConfigureMainPanel.WIDTH1+ 20, posY, 120, 20);
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


    protected void doubleClick(int x, int y){
    }

    protected void save(){
        System.out.println("saving efx: " +qlcEf.getName());
        qlcEf.writeToConfigFile();
    }

    protected void run() {
        ShowCollection.getInstance().toggleShow(show);
    /*    if (show.isExecuting())
            btnRun.setText("Stop");
        else
            btnRun.setText("Start");*/
    }


    @Override
    public void notify(final QLCExecutionNode node) {
        this.screenPoints = node.getScreenPoints();
        canvas.repaint();
    }

    @Override
    public void clear() {
        canvas.repaint();
    }

    @Override
    public void actionPerformed(final ActionEvent e) {

    }
    public QLCEfx getQlcEf() {
        return qlcEf;
    }
}