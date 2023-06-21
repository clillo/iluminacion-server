package cl.clillo.lighting.dmx;

import ch.bildspur.artnet.ArtNetClient;

public class ArtNet {

    private final ArtNetClient artNetClient;
    private final byte[] dmxData;

    private static final class InstanceHolder {
        private static final ArtNet instance = new ArtNet();
    }

    public static ArtNet getInstance() {
        return ArtNet.InstanceHolder.instance;
    }

    private ArtNet(){
        artNetClient = new ArtNetClient();
        dmxData = new byte[512];
        for (int i=0; i<512; i++)
            dmxData[i] = 0;

        artNetClient.start();
    }

    public void send(final int channel, final int data){
        send(channel, (byte) data);
    }

    public void send(final int channel, final byte data){
        dmxData[channel] = data;
        artNetClient.broadcastDmx(0, 0, dmxData);
    }

    public static void main(String[] args) throws InterruptedException {
        ArtNet artNet = ArtNet.getInstance();
        artNet.send(419, 255);
        for (int i=0; i<10000; i++) {
            artNet.send(423, 255);
            Thread.sleep(600);

            artNet.send(423, 0);
            Thread.sleep(600);
        }
    }
}
