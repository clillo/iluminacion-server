package cl.clillo.lighting;

import cl.clillo.lighting.external.dmx.ArtNet;
import cl.clillo.lighting.gui.controller.ControllerJFrame;
import cl.clillo.lighting.model.Point;
import cl.clillo.lighting.model.QLCFunction;
import cl.clillo.lighting.model.QLCScene;
import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.model.ShowCollection;
import cl.clillo.lighting.repository.StateRepository;

import java.util.List;

public class StandAlone {

    public static void main(String[] args) {
      //  ArtNet.setMode(ArtNet.ArtNetMode.DIRECT_ART_NET);
        ArtNet.setMode(ArtNet.ArtNetMode.HTTP_ART_NET);
      //  ArtNet.setMode(ArtNet.ArtNetMode.NON_ART_NET);

        final ControllerJFrame controllerJFrame = new ControllerJFrame();
        controllerJFrame.start();
/*
        final List<QLCFunction> list = ShowCollection.getInstance().getOriginalFunctionList("Sequence", "Derby");
        //list.addAll(ShowCollection.getInstance().getOriginalFunctionList("Sequence", "Spiders/Single"));
        for (QLCFunction function: list){
            System.out.println(function.getId()+"\t"+function.getName());
         //   function.writeToConfigFile();
       }*/

        StateRepository stateRepository = StateRepository.getInstance();
        stateRepository.read();
      //  stateRepository.write();*/

        for (Show show: ShowCollection.getInstance().getShowList())
            if (show.getFunction().getType().equals("Scene") && show.getFunction().getPath().equals("RGBW")) {
               // System.out.println(show.getFunction());
              //  show.getFunction().writeToConfigFile();
            }
    }
}
