package cl.clillo.polygon.gui;

import cl.clillo.polygon.Line;
import cl.clillo.polygon.Point;
import cl.clillo.polygon.Polygon;
import cl.clillo.polygon.Scene;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

public class GuiEditPanel extends JPanel implements MouseMotionListener, ActionListener {

    @Serial
    private static final long serialVersionUID = -5869553409971473557L;

    private final Cursor CURSOR_CROSS = new Cursor(Cursor.CROSSHAIR_CURSOR);
    private static final Color []COLOR_LIST = {Color.ORANGE, Color.CYAN, Color.WHITE, Color.PINK, Color.GREEN,
            Color.LIGHT_GRAY, Color.GRAY, Color.MAGENTA};

    private final JPanel canvas;

    private final JTextField cursorPosition = new JTextField();
    private final JList<GuiPolygon> polygonJList = new JList<>();

    private List<GuiObject> objectList = new ArrayList<>();
    private GuiLine referenceGuiLine;
    private boolean nearP1;
    private boolean nearP2;

    private Scene scene;

    public GuiEditPanel(final Scene scene) {
        this.scene = scene;
        setup();
        setLayout(null);

        cursorPosition.setBounds(GuiApp.VisualizerMainPanel.WIDTH1+10, 20, 140, 20);
        this.add(cursorPosition);

        final JScrollPane polygonListScroll = new JScrollPane(polygonJList);
        polygonListScroll.setBounds(GuiApp.VisualizerMainPanel.WIDTH1+15, 50, 110, 200);
        this.add(polygonListScroll);

        polygonJList.addListSelectionListener(e -> polygonSelected());

        canvas = new JPanel(){
            @Serial
            private static final long serialVersionUID = 9056031188937687827L;

            public void paint(final Graphics g){
                super.paint(g);
                g.setColor(Color.gray);
                g.drawLine(canvas.getWidth()/2, 0, canvas.getWidth()/2, canvas.getHeight());
                g.drawLine(0, canvas.getHeight()/2, canvas.getWidth(), canvas.getHeight()/2);

                for (GuiObject object: objectList)
                    if (object.isVisible())
                        object.paint(g);

            }
        };

        canvas.setBackground(Color.BLACK);
        canvas.setBounds(10, 0, GuiApp.VisualizerMainPanel.WIDTH1, GuiApp.VisualizerMainPanel.HEIGHT1);
        canvas.addMouseMotionListener(this);
        canvas.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent event) {

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
    }

    private void setup(){
        objectList = new ArrayList<>();
        referenceGuiLine = new GuiLine(scene.line());
        objectList.add(new GuiPoint(referenceGuiLine.getP1()));
        objectList.add(new GuiPoint(referenceGuiLine.getP2()));
   //     objectList.add(referenceGuiLine);

  //     objectList.add(new GuiPolygon(scene.polygon()));
        final List<Polygon> polygonList = scene.polygon().cutByLine(scene.line());

        if (polygonList.isEmpty())
            return;
        final List<GuiPolygon> guiPolygonList = new ArrayList<>();

        final GuiPolygon[] scrPolygonList = new GuiPolygon[polygonList.size()];
        int i=0;
        for (Polygon p: polygonList) {
            GuiPolygon gp = new GuiPolygon(p, COLOR_LIST[i]);
            guiPolygonList.add(gp);
            scrPolygonList[i] = gp;
            i++;
        }

        polygonJList.setListData(scrPolygonList);
        objectList.addAll(guiPolygonList);
    }

    private void polygonSelected(){
        GuiPolygon gp = polygonJList.getSelectedValue();
        if (gp==null)
            return;

        for (GuiObject object: objectList)
            if (object instanceof GuiPolygon polygon)
                polygon.setVisible(polygon == gp);

        repaint();
    }

    @Override
    public void mouseDragged(final MouseEvent e) {
        if (!nearP1 && !nearP2)
            return;

        final double x = GuiPoint.screenToRealX(e.getX());
        final double y = GuiPoint.screenToRealY(e.getY());
        final Point p1 = scene.line().getP1();
        final Point p2 = scene.line().getP2();
        final Line line;
        if (nearP1)
            line = new Line(new Point(x,y), p2);
        else
            line = new Line(p1, new Point(x,y));

        scene = new Scene(scene.polygon(), line);
        setup();
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        double x = GuiPoint.screenToRealX(e.getX());
        double y = GuiPoint.screenToRealY(e.getY());
        cursorPosition.setText((int)x+","+(int)y);
        nearP1 = false;
        nearP2 = false;

        if (referenceGuiLine.nearP1(e.getX(), e.getY())) {
            this.setCursor(CURSOR_CROSS);
            nearP1 = true;
            return;
        }

        if (referenceGuiLine.nearP2(e.getX(), e.getY())) {
            this.setCursor(CURSOR_CROSS);
            nearP2 = true;
            return;
        }

        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    @Override
    public void actionPerformed(final ActionEvent e) {

    }

}