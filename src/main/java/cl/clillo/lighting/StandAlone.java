package cl.clillo.lighting;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import cl.clillo.lighting.external.dmx.ArtNet;
import cl.clillo.lighting.gui.controller.ControllerJFrame;
import cl.clillo.lighting.model.ColorsCatalog;
import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.model.ShowCollection;
import cl.clillo.lighting.utils.FileUtils;
import cl.clillo.lighting.web.WebServer;
import cl.clillo.utilities.BeeEyeDemo;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class StandAlone {

    private void writeFunctions(final String type, final String path){
          for (Show show: ShowCollection.getInstance().getShowList())
             if (show.getFunction().getType().equals(type) && show.getFunction().getPath().equals(path)) {
                System.out.println(show.getFunction());
                 String dir = FileUtils.getDirectory(ShowCollection.BASE_DIR+"/"+show.getFunction().getClass().getSimpleName()+"."+show.getFunction().getPath()).getAbsolutePath();

                 show.getFunction().writeToConfigFile(dir);
           }
    }

    private void writeDMXMap(){
        new Thread(){
            @Override
            public void run() {
                ArtNet artNet = ArtNet.getInstance();
                //  for (int u=6; u<20; u++)
                //    artNet.send(u, 255);

                artNet.send(4, 0);
                artNet.send(5, 0);

                artNet.send(7, 255);
                artNet.send(0, 0);

                artNet.send(9, 255);
                artNet.send(10, 255);

                artNet.send(21, 255);
                artNet.send(25, 255);
                artNet.send(29, 255);
                artNet.send(33, 255);
                artNet.send(37, 255);
                artNet.send(41, 255);
                artNet.send(45, 255);
                int i=0;
                while(true){
                    i++;
                    artNet.send(0, i%255);
                    artNet.send(2, i%255);

                    System.out.println(i%255);
                    artNet.broadCast();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }.start();

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
       // ArtNet.setMode(ArtNet.ArtNetMode.DIRECT_ART_NET);
     //   ArtNet.setMode(ArtNet.ArtNetMode.HTTP_ART_NET);
        ArtNet.setMode(ArtNet.ArtNetMode.NON_ART_NET);

       // Cargar catálogo de colores
       ColorsCatalog.loadFromClasspath();

       // Iniciar servidor web para control desde tablet
       WebServer webServer = new WebServer(8080);
       try {
           webServer.start();
       } catch (Exception e) {
           System.err.println("Error starting web server: " + e.getMessage());
           e.printStackTrace();
       }

       final ControllerJFrame controllerJFrame = new ControllerJFrame();
       controllerJFrame.start();

       // Lanzar también el frame demo con 4 BeeEye
     //  BeeEyeDemo.main(new String[0]);
    }
}
