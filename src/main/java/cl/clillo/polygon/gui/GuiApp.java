package cl.clillo.polygon.gui;

import cl.clillo.lighting.utils.SystemUtils;
import cl.clillo.polygon.Scene;
import lombok.extern.slf4j.Slf4j;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.io.Serial;
import java.util.List;

public class GuiApp {

    private final VisualizerJFrame frame;

    public GuiApp(final Scene scene) {
        frame = new VisualizerJFrame(scene);
    }

    public void start(){
        frame.start();
    }

    @Slf4j
    static class VisualizerJFrame extends JFrame {

        @Serial
        private static final long serialVersionUID = 1823403452881818081L;

        public VisualizerJFrame(final Scene scene) {
            enableEvents(64L);

            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            setSize(VisualizerMainPanel.WIDTH1 + 150, VisualizerMainPanel.HEIGHT1 + 50);
            final VisualizerMainPanel mainPanel = new VisualizerMainPanel(scene);
            setContentPane(mainPanel);

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
                setLocation(-2000, 200);
            else
                setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

            setVisible(true);

        }
    }

    static class VisualizerMainPanel extends JPanel {

        @Serial
        private static final long serialVersionUID = -5869553409971473557L;

        public static final int WIDTH1 = 1024;
        public static final int HEIGHT1 = 800;

        public VisualizerMainPanel(final Scene scene) {
            this.setBounds(0, 0, WIDTH1 + 300, HEIGHT1);
            this.setLayout(null);

            final GuiEditPanel editPanel = new GuiEditPanel(scene);
            editPanel.setBounds(0, 0, WIDTH1 + 200, HEIGHT1);
            this.add(editPanel);

        }

    }
}
