package cl.clillo.lighting;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import cl.clillo.lighting.external.dmx.ArtNet;
import cl.clillo.lighting.gui.controller.ControllerJFrame;
import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.model.ShowCollection;
import cl.clillo.lighting.utils.CSVSupport;
import cl.clillo.lighting.utils.FileUtils;
import org.slf4j.LoggerFactory;


public class StandAlone {

    private void writeFunctions(final String type, final String path){
          for (Show show: ShowCollection.getInstance().getShowList())
             if (show.getFunction().getType().equals(type) && show.getFunction().getPath().equals(path)) {
                System.out.println(show.getFunction());
                 String dir = FileUtils.getDirectory(ShowCollection.BASE_DIR+"/"+show.getFunction().getClass().getSimpleName()+"."+show.getFunction().getPath()).getAbsolutePath();

                 show.getFunction().writeToConfigFile(dir);
           }
    }

    public static void main(String[] args) {
   //     StandAlone standAlone = new StandAlone();
       // CSVSupport csvSupport = new CSVSupport();
      //  csvSupport.printFunctionIds();
      // csvSupport.rewriteFunctions();
       // standAlone.readCSVFunctionDefinition("/Users/carlos.lillo/IdeaProjects/iluminacion-server/src/main/resources/qlc/QLCSequence.RGBW/RGBW - Sheet2.csv");
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);

        ArtNet.setMode(ArtNet.ArtNetMode.DIRECT_ART_NET);
     //   ArtNet.setMode(ArtNet.ArtNetMode.HTTP_ART_NET);
       // ArtNet.setMode(ArtNet.ArtNetMode.NON_ART_NET);

       final ControllerJFrame controllerJFrame = new ControllerJFrame();
        controllerJFrame.start();
    }
}
