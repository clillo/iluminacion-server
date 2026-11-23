package cl.clillo.lighting.external.dmx;

import ch.bildspur.artnet.ArtNetClient;
import cl.clillo.lighting.config.ExternalConfigService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ArtNet {

    private static final int MAX_UNIVERSES = 2;

    public enum ArtNetMode {
        NON_ART_NET,
        HTTP_ART_NET,
        DIRECT_ART_NET
    }

    protected final Map<Integer, byte[]> dmxData;
    protected final List<ArtNetConnectionListener> connectionListeners = new CopyOnWriteArrayList<>();

    private ArtNet(){
        dmxData = new HashMap<>();
        for (int i=0; i<MAX_UNIVERSES; i++)
            dmxData.put(i, new byte[512]);
    }

    private static final class InstanceHolder {
        private static ArtNet instance;
        private static ArtNetMode artNetMode;

        public static void setMode(final ArtNetMode artNetMode) {
            InstanceHolder.artNetMode = artNetMode;
        }

        public static synchronized ArtNet getInstance() {
            if (instance==null){
                instance = InstanceHolder.artNetMode==ArtNetMode.HTTP_ART_NET?new ArtNetHttpProxy():
                        InstanceHolder.artNetMode==ArtNetMode.DIRECT_ART_NET?new DirectArtNet():new ArtNet();
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
      //  System.out.println("ArtNet sent to " + universe + " " + channel + " " + data);
    }

    public void broadCast(){
    }

    public boolean isConnected() {
        return false;
    }

    public String getNodeAddress() {
        return null;
    }

    public void addConnectionListener(final ArtNetConnectionListener listener) {
        connectionListeners.add(listener);
    }

    /**
     * Actualiza la dirección IP de ArtNet desde la configuración.
     */
    public void updateArtNetAddress() {
    }

    private static class DirectArtNet extends ArtNet {

        private static final long WATCH_INTERVAL_SECONDS = 3;
        private static final int MAX_MISSED_POLLS = 3;

        private final ScheduledExecutorService watcher;
        private volatile ArtNetClient artNetClient;
        private volatile ArtNetUtils.Detection detection;
        private int missedPolls;

        private DirectArtNet() {
            checkNode();

            watcher = Executors.newSingleThreadScheduledExecutor(runnable -> {
                final Thread thread = new Thread(runnable, "artnet-node-watcher");
                thread.setDaemon(true);
                return thread;
            });
            watcher.scheduleWithFixedDelay(this::checkNode, WATCH_INTERVAL_SECONDS, WATCH_INTERVAL_SECONDS, TimeUnit.SECONDS);
        }

        @Override
        public void broadCast(){
            final ArtNetClient client = artNetClient;
            if (client == null)
                return;

            for (int i=0; i<MAX_UNIVERSES; i++) {
              //  artNetClient.unicastDmx(artnetAddress, 0, i, dmxData.get(i));
                client.broadcastDmx(0, i, dmxData.get(i));
               // System.out.println("Broadcaster: " + i+"\t"+ Arrays.toString(dmxData.get(i)));
            }
        }

        @Override
        public boolean isConnected() {
            return artNetClient != null;
        }

        @Override
        public String getNodeAddress() {
            final ArtNetUtils.Detection current = detection;
            return current == null ? null : current.nodeAddress().getHostAddress();
        }

        @Override
        public void updateArtNetAddress() {
            watcher.execute(this::reconnect);
        }

        private synchronized void reconnect() {
            if (artNetClient != null)
                disconnect();
            checkNode();
        }

        private synchronized void checkNode() {
            try {
                if (artNetClient == null)
                    connect();
                else if (ArtNetUtils.isReachable(detection))
                    missedPolls = 0;
                else if (++missedPolls >= MAX_MISSED_POLLS)
                    disconnect();
            } catch (RuntimeException e) {
                // an exception would cancel the scheduled watcher
                log.error("Error checking Art-Net node", e);
            }
        }

        private void connect() {
            final String preferredLocalAddress = ExternalConfigService.getInstance().getArtNetIpAddress();
            final Optional<ArtNetUtils.Detection> found = ArtNetUtils.detect(preferredLocalAddress);
            if (found.isEmpty())
                return;

            final ArtNetUtils.Detection newDetection = found.get();
            final ArtNetClient client = new ArtNetClient(null);
            client.start(newDetection.localAddress());
            if (!client.isRunning()) {
                log.warn("Could not start Art-Net client on {}", newDetection.localAddress());
                return;
            }

            detection = newDetection;
            missedPolls = 0;
            artNetClient = client;

            final String nodeAddress = newDetection.nodeAddress().getHostAddress();
            log.info("Art-Net node {} connected through {}", nodeAddress, newDetection.localAddress());
            for (ArtNetConnectionListener listener : connectionListeners)
                listener.onArtNetConnected(nodeAddress);
        }

        private void disconnect() {
            final ArtNetClient client = artNetClient;
            artNetClient = null;
            client.stop();
            log.info("Art-Net node {} disconnected", detection.nodeAddress().getHostAddress());
            detection = null;

            for (ArtNetConnectionListener listener : connectionListeners)
                listener.onArtNetDisconnected();
        }
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

    }
}
