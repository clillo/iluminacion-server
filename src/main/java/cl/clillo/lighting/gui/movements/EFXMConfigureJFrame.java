package cl.clillo.lighting.gui.movements;

import cl.clillo.lighting.utils.SystemUtils;

import javax.swing.JFrame;
import javax.swing.UIManager;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;

public class EFXMConfigureJFrame extends JFrame {

    private static final long serialVersionUID = 1823403452881818081L;

    public EFXMConfigureJFrame() {
        enableEvents(64L);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        setSize(EFXMConfigureMainPanel.WIDTH1 + 600, EFXMConfigureMainPanel.HEIGHT1 + 150);
        EFXMConfigureMainPanel EFXMConfigureMainPanel = new EFXMConfigureMainPanel();
        setContentPane(EFXMConfigureMainPanel);

    }

    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == 201) {
            dispose();

            System.exit(0);
        }
    }

    public void start() {
        validate();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        setLocationRelativeTo(null);

        if (SystemUtils.getSO() == SystemUtils.SO.MAC_OS)
            setLocation(-2250, 200);
        else
            setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

        setVisible(true);

    }
}