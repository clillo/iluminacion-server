package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.gui.ScreenPoint;
import cl.clillo.lighting.gui.movements.EFXMConfigureMainPanel;
import cl.clillo.lighting.gui.movements.EffectEditPanel;
import cl.clillo.lighting.model.QLCEfx;
import cl.clillo.lighting.model.QLCEfxScene;
import cl.clillo.lighting.model.QLCExecutionNode;
import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.model.ShowCollection;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class SceneEditPanel extends EffectEditPanel implements ChangeListener {

    private static final long serialVersionUID = -5869553409971473557L;

    private final QLCEfxScene qlcEfx;
    private List<ScreenPoint> screenPointList;

    private double mousePrevX=0;
    private double mousePrevY=0;

    private ScreenPoint screenPointSelected;

    private final JRadioButton[] rbtFixtures;
    private final JButton btnUpX1 = new JButton("1");
    private final JButton btnUpX100 = new JButton("100");
    private final JButton btnUpX500 = new JButton("500");

    private final JButton btnDownX1 = new JButton("1");
    private final JButton btnDownX100 = new JButton("100");
    private final JButton btnDownX500 = new JButton("500");

    private final JButton btnUpY1 = new JButton("1");
    private final JButton btnUpY100 = new JButton("100");
    private final JButton btnUpY500 = new JButton("500");

    private final JButton btnDownY1 = new JButton("1");
    private final JButton btnDownY100 = new JButton("100");
    private final JButton btnDownY500 = new JButton("500");

    public SceneEditPanel(final QLCEfxScene qlcEfx, final Show show) {
        super(null, show);
        this.qlcEfx = qlcEfx;
        qlcEfx.updateParameters(-1,0,0);
        screenPointList = new ArrayList<>();

        for (QLCExecutionNode node: qlcEfx.getNodes()) {
            ScreenPoint sc = node.getScreenPoints()[0];
            screenPointList.add(sc);

        }

        rbtFixtures = new JRadioButton[screenPointList.size()];
        for (int i = 0; i< screenPointList.size(); i++) {
            rbtFixtures[i] = new JRadioButton("Fix: " + i);
            rbtFixtures[i].setBounds(EFXMConfigureMainPanel.WIDTH1+ 20,600 + i*30,140,30);

        }
        addButton(btnUpY1, 1, 2);
        addButton(btnUpY100, 2, 2);
        addButton(btnUpY500, 3, 2);

        addButton(btnDownY1, 1, 1);
        addButton(btnDownY100, 2, 1);
        addButton(btnDownY500, 3, 1);

        addButton(btnUpX1, 1, 3);
        addButton(btnUpX100, 2, 3);
        addButton(btnUpX500, 3, 3);

        addButton(btnDownX1, 1, 4);
        addButton(btnDownX100, 2, 4);
        addButton(btnDownX500, 3, 4);

        addToButtonGroup(this, rbtFixtures);
        setQlcEfx(qlcEfx);
    }

    private void addButton(final JButton btn, final int col, final int line) {
        int y = 0;
        int offsetX = 0;
        switch (line){
            case 1:
                y=740;
                offsetX = 50;
                break;
            case 2:
                y=860;
                offsetX = 50;
                break;
            case 4:
                y=780;
                offsetX = 30;
                break;
            case 3:
                y=820;
                offsetX = 70;
                break;
        }

        btn.setBounds(EFXMConfigureMainPanel.WIDTH1+ offsetX + 40* (col-1), y,30,30);
        btn.addActionListener(this);
        add(btn);

    }

    private void addToButtonGroup(final JPanel panelDimmers, final JRadioButton... buttons){
        javax.swing.ButtonGroup bg = new javax.swing.ButtonGroup();
        for (JRadioButton rb: buttons) {
            bg.add(rb);
            rb.addChangeListener(this);
            panelDimmers.add(rb);
        }

    }

    @Override
    protected void drawCanvas(Graphics g) {
        int i=0;
        for (ScreenPoint pb: screenPointList) {
            if (rbtFixtures[i].isSelected()) {
                g.setColor(Color.RED);
              //  System.out.println(pb.toString());
            }else
                g.setColor(Color.WHITE);
            g.fillOval(pb.getScreenX(), pb.getScreenY(), 8, 8);
            i++;
        }
    }

    @Override
    protected void setQlcEfx(QLCEfx qlcEfx) {
        txtLineOriginX.setText("");
        txtLineOriginY.setText("");
        txtLineDestinyX.setText("");
        txtLineDestinyY.setText("");

        if (screenPointList ==null)
            return;

        if (screenPointList.size()>=1)
            txtLineOriginX.setText(screenPointList.get(0).toString());

        if (screenPointList.size()>=2)
            txtLineOriginY.setText(screenPointList.get(1).toString());

        if (screenPointList.size()>=3)
            txtLineDestinyX.setText(screenPointList.get(2).toString());

        if (screenPointList.size()>=4)
            txtLineDestinyY.setText(screenPointList.get(3).toString());

    }

    @Override
    public void mouseDragged(MouseEvent e) {

        double x = screenToRealX(e.getX());
        double y = screenToRealY(e.getY());

        if (this.getCursor()==CURSOR_CROSS && screenPointSelected!=null){
            updateParametersCircle(x, y);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mousePrevX = screenToRealX(e.getX());
        mousePrevY = screenToRealY(e.getY());
        txtScreenPosition.setText(Math.round(mousePrevX)+","+ Math.round(mousePrevY));
        screenPointSelected=null;
        int i=0;
        for (ScreenPoint pb: screenPointList) {
            if (Math.abs(mousePrevX - pb.getRealX()) < 400 && Math.abs(mousePrevY - pb.getRealY()) < 400) {
                this.setCursor(CURSOR_CROSS);
                screenPointSelected = pb;
                rbtFixtures[i].setSelected(true);
                repaint();
                return;
            }
            i++;
        }

        this.setCursor(Cursor.getDefaultCursor());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(btnUpY100)){
            mov(0,100);
            return;
        }
        if (e.getSource().equals(btnUpY1)){
            mov(0,1);
            return;
        }
        if (e.getSource().equals(btnUpY500)){
            mov(0,500);
            return;
        }
        if (e.getSource().equals(btnDownY1)){
            mov(0,-1);
            return;
        }
        if (e.getSource().equals(btnDownY100)){
            mov(0,-100);
            return;
        }
        if (e.getSource().equals(btnDownY500)){
            mov(0,-500);
            return;
        }

        if (e.getSource().equals(btnUpX100)){
            mov(100,0);
            return;
        }
        if (e.getSource().equals(btnUpX1)){
            mov(1,0);
            return;
        }
        if (e.getSource().equals(btnUpX500)){
            mov(500,0);
            return;
        }
        if (e.getSource().equals(btnDownX1)){
            mov(-1,0);
            return;
        }
        if (e.getSource().equals(btnDownX100)){
            mov(-100,0);
            return;
        }
        if (e.getSource().equals(btnDownX500)){
            mov(-500,0);
            return;
        }
    }

    private void mov(int dX, int dY){
        int i=0;
        for (ScreenPoint pb: screenPointList) {
            if (rbtFixtures[i].isSelected()){
                screenPointSelected = pb;
                pb.setRealX(pb.getRealX()+dX);
                pb.setRealY(pb.getRealY()+dY);
            }
            i++;
        }

       refresh();
    }

    public void updateParametersCircle(final double newX, final double newY) {
        final QLCEfxScene qlcEfx = this.qlcEfx;
        qlcEfx.updateParameters(screenPointSelected.getFixtureId(), newX, newY);
        this.setQlcEfx(qlcEfx);
        this.repaint();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        int i=0;
        for (ScreenPoint pb: screenPointList) {
            if (rbtFixtures[i].isSelected()){
                screenPointSelected = pb;
            }
            i++;
        }

        repaint();
    }

    protected void save(){
        System.out.println("saving show: " +qlcEfx.getShow().getName());
        qlcEfx.updateParameters(screenPointList);
        String dir = ShowCollection.getInstance().getDirectory(qlcEfx.getShow().getFunction());
        qlcEfx.getShow().getFunction().writeToConfigFile(dir);
        parent.dispose();
    }

    protected void refresh(){
        this.repaint();
        qlcEfx.updateParameters(screenPointList);
    }
}