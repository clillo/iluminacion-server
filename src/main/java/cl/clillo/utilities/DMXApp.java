package cl.clillo.utilities;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import cl.clillo.lighting.external.dmx.ArtNet;
import org.slf4j.LoggerFactory;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class DMXApp {

    public static void main(String[] args) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
        ArtNet.setMode(ArtNet.ArtNetMode.DIRECT_ART_NET);
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            int n = 249;
            if (args.length > 0) {
                try { n = Math.min(512, Math.max(1, Integer.parseInt(args[0]))); } catch (NumberFormatException ignored) {}
            }
            new MainFrame(n).setVisible(true);
        });
    }
}
