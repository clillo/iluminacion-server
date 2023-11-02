package cl.clillo.lighting;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import cl.clillo.lighting.gui.virtualdj.VirtualDJFrame;
import org.slf4j.LoggerFactory;

public class ConfigureVDJ {

    private final VirtualDJFrame virtualDJFrame;

    public ConfigureVDJ() {
        virtualDJFrame = new VirtualDJFrame();

    }

    public void start(){
        virtualDJFrame.start();
    }

    public static void main(String[] args) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
        final ConfigureVDJ configureApp = new ConfigureVDJ();
        configureApp.start();
    }
}
