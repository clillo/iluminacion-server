package cl.clillo.lighting.utils;


import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.UIManager;


public class PruebaCreaProgramasRobotizados extends JFrame implements ActionListener{

    private static final long serialVersionUID = 1823403452881818081L;

    public PruebaCreaProgramasRobotizados() {
        enableEvents(64L);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        initialize();
    }

    private void initialize() {
        setSize(FixtureRoboticPanel.WIDTH1+200, FixtureRoboticPanel.HEIGHT1+50);
        setContentPane(new FixtureRoboticPanel());
    }

    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == 201){
            dispose();

            System.exit(0);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    public static PruebaCreaProgramasRobotizados start() {
        PruebaCreaProgramasRobotizados vp = new PruebaCreaProgramasRobotizados();
        vp.validate();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = vp.getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        vp.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        vp.setVisible(true);

        return vp;
    }
}