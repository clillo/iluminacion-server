package cl.clillo.lighting.external.dmx;

import ch.bildspur.artnet.ArtNetClient;
import cl.clillo.lighting.config.ExternalConfigService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ArtNet {

    private static final int MAX_UNIVERSES = 2;
    private String artnetAddress;

    public enum ArtNetMode {
        NON_ART_NET,
        HTTP_ART_NET,
        DIRECT_ART_NET
    }

    private final ArtNetClient artNetClient;
    private final Map<Integer, byte[]> dmxData;

    private ArtNet(){
        // Cargar IP desde configuración externa
        ExternalConfigService configService = ExternalConfigService.getInstance();
        ArtNetUtils artNetUtils = new ArtNetUtils();
        artNetUtils.detectArtNet();
        artnetAddress = artNetUtils.getLocalArtNetAddress();// configService.getArtNetIpAddress();
        artNetClient = new ArtNetClient(null);
        artNetClient.start(artnetAddress);

        dmxData = new HashMap<>();
        for (int i=0; i<MAX_UNIVERSES; i++) {
            byte[] buffer = new byte[512];
            dmxData.put(i, buffer);

            for (int c = 0; c < 512; c++)
                buffer[c] = 0;
        }
    }

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

    public void send(final int universe, final int dmxChannel, final int dmxValue){
        send(universe, dmxChannel, (byte) dmxValue);
    }

    private void send(final int universe, final int channel, final byte data){
        dmxData.get(universe-1)[channel] = data;
    }

    public void broadCast(){
        for (int i=0; i<MAX_UNIVERSES; i++) {
          //  artNetClient.unicastDmx(artnetAddress, 0, i, dmxData.get(i));
            artNetClient.broadcastDmx(0, i, dmxData.get(i));
        }
    }

    /**
     * Actualiza la dirección IP de ArtNet desde la configuración.
     */
    public void updateArtNetAddress() {
        ExternalConfigService configService = ExternalConfigService.getInstance();
        artnetAddress = configService.getArtNetIpAddress();
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
           // System.out.println("\t"+channel+"\t"+data);
        }

        public void send(final int channel, final byte data){
        }

        public void broadCast(){
        }
    }
}
