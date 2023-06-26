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
        artNetClient = new ArtNetClient(null);
        artNetClient.start();

        dmxData = new byte[512];
        for (int i=0; i<512; i++)
            dmxData[i] = 0;
    }

    public void send(final int channel, final int data){
        send(channel, (byte) data);
    }

    public void send(final int channel, final byte data){
        dmxData[channel] = data;
    }

    public void broadCast(){
      //  System.out.println(dmxData[120]);
      // artNetClient.broadcastDmx(0, 0, dmxData);
        artNetClient.unicastDmx("169.254.255.255", 0, 0, dmxData);
    }

    public static void main(String[] args) throws InterruptedException {
        ArtNet artNet = ArtNet.getInstance();
        artNet.send(115, 255);
        artNet.send(116, 255);
        artNet.send(117, 100);
        artNet.send(119, 255);
        artNet.send(121, 255);
        artNet.send(120, 10);
        artNet.send(118, 120);
        for (int i=0; i<10000; i++) {
            artNet.send(122, 100);
            Thread.sleep(100);



            artNet.send(122, 0);
            Thread.sleep(100);
        }
        artNet.send(118, 0);
        artNet.send(120, 0);
        artNet.send(121, 0);
        artNet.send(119, 0);
        artNet.send(115, 0);
        artNet.send(116, 0);
        artNet.send(117, 0);
    }
}
