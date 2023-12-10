package cl.clillo.lighting.external.dmx;

import ch.bildspur.artnet.ArtNetClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ArtNet {

    public enum ArtNetMode {
        NON_ART_NET,
        HTTP_ART_NET,
        DIRECT_ART_NET
    }

    private final ArtNetClient artNetClient;
    private final byte[] dmxData;

    private static final class InstanceHolder {
        private static ArtNet instance;
        private static ArtNetMode artNetMode;

        public static void setMode(final ArtNetMode artNetMode) {
            InstanceHolder.artNetMode = artNetMode;
        }

        public static ArtNet getInstance() {
            if (instance==null){
                instance = InstanceHolder.artNetMode==ArtNetMode.HTTP_ART_NET?new ArtNetHttpProxy():
                        InstanceHolder.artNetMode==ArtNetMode.DIRECT_ART_NET?new ArtNet():new NoComm();
            }
            return instance;
        }
    }

    public static ArtNet getInstance() {
        return ArtNet.InstanceHolder.getInstance();
    }

    public static void setMode(final ArtNetMode artNetMode){
        ArtNet.InstanceHolder.setMode(artNetMode);
    }

    private ArtNet(){
        artNetClient = new ArtNetClient(null);
        artNetClient.start();

        dmxData = new byte[512];
        for (int i=0; i<512; i++)
            dmxData[i] = 0;
    }

    public void send(final int dmxChannel, final int dmxValue){

        send(dmxChannel, (byte) dmxValue);
    }

    public void send(final int channel, final byte data){
        dmxData[channel] = data;
    }

    public byte getDmxData(int channel) {
        return dmxData[channel];
    }

    public void broadCast(){
      //  System.out.println(dmxData[120]);
      // artNetClient.broadcastDmx(0, 0, dmxData);
        artNetClient.unicastDmx("169.254.0.255", 0, 0, dmxData);
      //  artNetClient.unicastDmx("169.254.215.1", 0, 0, dmxData);

    }

    private static class ArtNetHttpProxy extends ArtNet{

        public void send(final int dmxChannel, final int dmxValue){

            try {
                final URL url = new URL("http://192.168.1.141:8090/dmx/"+ dmxChannel +"/"+dmxValue);

                final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.getInputStream().close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void send(final int channel, final byte data){

        }

        public void broadCast(){

        }

    }

    private static class NoComm extends ArtNet{

        public void send(final int channel, final int data){
        }

        public void send(final int channel, final byte data){
        }

        public void broadCast(){
        }
    }
}
