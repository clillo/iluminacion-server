package cl.clillo.ilumination;

import ch.bildspur.artnet.ArtNetClient;

public class ArtNet {

    public static void main(String[] args) {
        ArtNetClient artNetClient = new ArtNetClient();
        artNetClient.start();
        byte[] dmxData = new byte[512];
        dmxData[0] = (byte) 128;
        artNetClient.broadcastDmx(0,1, dmxData);
        //artNetClient.stop();
    }
}
