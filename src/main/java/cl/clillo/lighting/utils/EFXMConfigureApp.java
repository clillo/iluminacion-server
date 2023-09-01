package cl.clillo.lighting.utils;

import cl.clillo.lighting.model.QLCEfx;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.UIManager;

public class EFXMConfigureApp extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1823403452881818081L;
    private final QLCEfx qlcEfx;

    public EFXMConfigureApp(final QLCEfx qlcEfx) {
        this.qlcEfx = qlcEfx;

        enableEvents(64L);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        initialize();
    }

    private void initialize() {
        setSize(FixtureRoboticPanel.WIDTH1 + 200, FixtureRoboticPanel.HEIGHT1 + 50);
        FixtureRoboticPanel fixtureRoboticPanel = new FixtureRoboticPanel(qlcEfx);
        setContentPane(fixtureRoboticPanel);

    }

    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == 201) {
            dispose();

            System.exit(0);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    public static EFXMConfigureApp start(final QLCEfx qlcEfx) {
        EFXMConfigureApp vp = new EFXMConfigureApp(qlcEfx);
        vp.validate();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = vp.getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        // vp.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        vp.setLocationRelativeTo(null);
        vp.setLocation(-1250, 200);
        vp.setVisible(true);

        return vp;
    }
}