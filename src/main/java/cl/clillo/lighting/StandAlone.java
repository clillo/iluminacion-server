package cl.clillo.lighting;

import cl.clillo.lighting.dmx.ArtNet;
import cl.clillo.lighting.gui.controller.ControllerJFrame;
import cl.clillo.lighting.midi.KeyData;
import cl.clillo.lighting.midi.MidiEvent;
import cl.clillo.lighting.midi.MidiHandler;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class StandAlone implements MidiEvent {

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
      //  ArtNet.setMode(ArtNet.ArtNetMode.DIRECT_ART_NET);
      //  ArtNet.setMode(ArtNet.ArtNetMode.HTTP_ART_NET);
        ArtNet.setMode(ArtNet.ArtNetMode.NON_ART_NET);
        final StandAlone standAlone = new StandAlone();


        ControllerJFrame controllerJFrame = new ControllerJFrame();
        controllerJFrame.start();

    }

    @Override
    public void onKeyPress(final KeyData keyData) {
        System.out.println("Key press: "+keyData);
    }

    @Override
    public void onKeyRelease(final KeyData keyData) {
        System.out.println("Key release: "+keyData);
    }

    @Override
    public void onSlide(KeyData keyData) {
        System.out.println("Slide: "+keyData);
    }
}
