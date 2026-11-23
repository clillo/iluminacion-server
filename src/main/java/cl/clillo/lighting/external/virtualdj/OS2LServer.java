package cl.clillo.lighting.external.virtualdj;

import cl.clillo.lighting.external.dmx.NetworkInterfaceScanner;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Servidor OS2L: VirtualDJ se conecta por TCP y envía eventos JSON (beat, cmd, btn).
 * <p>
 * Puerto configurable con -Dos2l.port (por defecto 4444). Con -Dos2l.publish=true se anuncia por Bonjour
 * para que VirtualDJ lo encuentre sin configurar la IP.
 */
@Slf4j
public class OS2LServer {

    public static final int DEFAULT_PORT = 4444;
    private static final String SERVICE_TYPE = "_os2l._tcp.local.";
    private static final String SERVICE_NAME = "iluminacion-server";
    private static final long BIND_RETRY_MS = 5_000;
    private static final int MAX_MESSAGE_LENGTH = 64 * 1024;

    private final int port;
    private final boolean doPublish;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<VDJBMPEvent> listeners = new CopyOnWriteArrayList<>();
    private final List<Socket> clients = new CopyOnWriteArrayList<>();

    private volatile ServerSocket serverSocket;
    private volatile boolean running;
    private volatile String bindError;
    private JmDNS jmdns;

    private static final class InstanceHolder {
        private static final OS2LServer instance = new OS2LServer();

        static {
            instance.start();
        }
    }

    public static OS2LServer getInstance() {
        return OS2LServer.InstanceHolder.instance;
    }

    public OS2LServer(int port, boolean doPublish) {
        this.port = port;
        this.doPublish = doPublish;
    }

    public OS2LServer() {
        this(Integer.getInteger("os2l.port", DEFAULT_PORT), Boolean.getBoolean("os2l.publish"));
    }

    public void addListener(VDJBMPEvent vdjbmpEvent){
        listeners.add(vdjbmpEvent);
    }

    public int getPort() {
        return port;
    }

    public boolean isListening() {
        final ServerSocket socket = serverSocket;
        return socket != null && !socket.isClosed();
    }

    public String getBindError() {
        return bindError;
    }

    public List<String> getConnectedClients() {
        return clients.stream().map(socket -> socket.getInetAddress().getHostAddress()).toList();
    }

    /**
     * Direcciones "IP:puerto" de este equipo que VirtualDJ puede usar en os2lDirectIp desde otra máquina.
     * Las link-local (169.254.x.x) solo se incluyen si no hay otra.
     */
    public List<String> getLocalAddresses() {
        final List<NetworkInterfaceScanner.InterfaceInfo> interfaces;
        try {
            interfaces = NetworkInterfaceScanner.findIpv4Interfaces();
        } catch (SocketException e) {
            log.debug("Could not list network interfaces", e);
            return List.of();
        }

        final List<NetworkInterfaceScanner.InterfaceInfo> routable = interfaces.stream()
                .filter(networkInterface -> !networkInterface.address().isLinkLocalAddress())
                .toList();
        return (routable.isEmpty() ? interfaces : routable).stream()
                .map(networkInterface -> networkInterface.address().getHostAddress() + ":" + port)
                .distinct()
                .toList();
    }

    public synchronized void start() {
        if (running)
            throw new IllegalStateException("OS2LServer is already running!");
        running = true;
        daemon("os2l-acceptor", this::acceptLoop).start();
    }

    private void acceptLoop() {
        while (running) {
            final ServerSocket socket = bind();
            if (socket == null) {
                sleep(BIND_RETRY_MS);
                continue;
            }

            while (running && !socket.isClosed()) {
                try {
                    final Socket client = socket.accept();
                    daemon("os2l-client-" + client.getInetAddress().getHostAddress(), () -> handleClient(client)).start();
                } catch (IOException e) {
                    if (running && !socket.isClosed()) {
                        log.warn("Error accepting OS2L connection", e);
                        sleep(100);
                    }
                }
            }
        }
    }

    private ServerSocket bind() {
        try {
            final ServerSocket socket = new ServerSocket();
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(port));
            serverSocket = socket;
            bindError = null;
            log.info("OS2L server listening on port {}", port);
            if (doPublish)
                publishService();
            return socket;
        } catch (IOException e) {
            final String error = e.getClass().getSimpleName() + ": " + e.getMessage();
            if (!error.equals(bindError))
                log.warn("Could not open OS2L port {} ({}), retrying every {} s", port, error, BIND_RETRY_MS / 1000);
            bindError = error;
            return null;
        }
    }

    private void handleClient(final Socket client) {
        final String remoteIp = client.getInetAddress().getHostAddress();
        clients.add(client);
        log.info("VirtualDJ connected from {}", remoteIp);
        notifyListeners(listener -> listener.remoteIp(remoteIp));

        final JsonObjectSplitter splitter = new JsonObjectSplitter(MAX_MESSAGE_LENGTH);
        try (client; Reader in = new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8)) {
            client.setKeepAlive(true);
            final char[] buffer = new char[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                for (String message : splitter.feed(buffer, read))
                    processMessage(message);
                if (splitter.takeOverflow())
                    log.warn("Discarded OS2L message longer than {} chars from {}", MAX_MESSAGE_LENGTH, remoteIp);
            }
        } catch (IOException e) {
            if (running)
                log.info("OS2L connection from {} closed: {}", remoteIp, e.getMessage());
        } finally {
            clients.remove(client);
            log.info("VirtualDJ disconnected from {}", remoteIp);
            notifyListeners(listener -> listener.remoteDisconnected(remoteIp));
        }
    }

    private void processMessage(final String message) {
        final JsonNode event;
        try {
            event = objectMapper.readTree(message);
        } catch (JsonProcessingException e) {
            log.warn("Discarded malformed OS2L message: {}", message);
            return;
        }

        switch (event.path("evt").asText()) {
            case "beat" -> processBeatEvent(event);
            case "cmd" -> processCommandEvent(event);
            case "btn" -> processButtonEvent(event);
            default -> log.debug("Ignored OS2L message: {}", message);
        }
    }

    private void processBeatEvent(final JsonNode event) {
        if (!event.hasNonNull("pos") || !event.hasNonNull("bpm")) {
            log.debug("Ignored OS2L beat without pos/bpm: {}", event);
            return;
        }
        final boolean change = event.path("change").asBoolean();
        final int pos = event.path("pos").asInt();
        final double bpm = event.path("bpm").asDouble();
        final double strength = event.path("strength").asDouble();

        notifyListeners(listener -> fireBeat(listener, change, pos, bpm, strength));
    }

    private static void fireBeat(final VDJBMPEvent vdjbmpEvent, final boolean change, final int pos,
                                 final double bpm, final double strength) {
        vdjbmpEvent.beat(change, pos, bpm, strength);
        vdjbmpEvent.beat();

        vdjbmpEvent.beat(Math.abs(pos%16)+1);

        switch (Math.abs(pos%16)+1) {
            case 1:
                vdjbmpEvent.beatX2(1);
                vdjbmpEvent.beatX4(1);
                vdjbmpEvent.beatX8(1);
                break;
            case 3:
                vdjbmpEvent.beatX2(2);
                break;
            case 5:
                vdjbmpEvent.beatX2(3);
                vdjbmpEvent.beatX4(2);
                break;
            case 7:
                vdjbmpEvent.beatX2(4);
                break;
            case 9:
                vdjbmpEvent.beatX2(5);
                vdjbmpEvent.beatX4(3);
                vdjbmpEvent.beatX8(2);
                break;
            case 11:
                vdjbmpEvent.beatX2(6);
                break;
            case 13:
                vdjbmpEvent.beatX2(7);
                vdjbmpEvent.beatX4(4);
                break;
            case 15:
                vdjbmpEvent.beatX2(8);
                break;
        }
        if (pos%2==0)
            vdjbmpEvent.beatX2();
        if (pos%4==0)
            vdjbmpEvent.beatX4();
        if (pos%8==0)
            vdjbmpEvent.beatX8();
        if (pos%16==0)
            vdjbmpEvent.beatX16();
    }

    private void processCommandEvent(final JsonNode event) {
        if (!event.hasNonNull("id")) {
            log.debug("Ignored OS2L cmd without id: {}", event);
            return;
        }
        final int id = event.path("id").asInt();
        final int param = event.path("param").asInt();
        notifyListeners(listener -> listener.command(id, param));
    }

    private void processButtonEvent(final JsonNode event) {
        if (!event.hasNonNull("name")) {
            log.debug("Ignored OS2L btn without name: {}", event);
            return;
        }
        final String name = event.path("name").asText();
        final String state = event.path("state").asText();
        notifyListeners(listener -> listener.button(name, state));
    }

    private void notifyListeners(final Consumer<VDJBMPEvent> action) {
        for (VDJBMPEvent listener : listeners) {
            try {
                action.accept(listener);
            } catch (RuntimeException e) {
                log.error("OS2L listener {} failed", listener.getClass().getName(), e);
            }
        }
    }

    private void publishService() {
        try {
            jmdns = JmDNS.create(InetAddress.getLocalHost());
            jmdns.registerService(ServiceInfo.create(SERVICE_TYPE, SERVICE_NAME, port, ""));
            log.info("OS2L service published as {} on {}", SERVICE_TYPE, jmdns.getInetAddress().getHostAddress());
        } catch (IOException e) {
            log.warn("Could not publish OS2L service via Bonjour", e);
        }
    }

    public synchronized void stop() throws IOException {
        running = false;
        if (jmdns != null) {
            jmdns.unregisterAllServices();
            jmdns.close();
            jmdns = null;
        }
        if (serverSocket != null)
            serverSocket.close();
        for (Socket client : clients)
            client.close();
    }

    private static Thread daemon(final String name, final Runnable runnable) {
        final Thread thread = new Thread(runnable, name);
        thread.setDaemon(true);
        return thread;
    }

    private static void sleep(final long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        OS2LServer server = new OS2LServer();
        server.start();
        Thread.currentThread().join();
    }
}
