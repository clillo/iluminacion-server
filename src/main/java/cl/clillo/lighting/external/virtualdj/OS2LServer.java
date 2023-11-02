package cl.clillo.lighting.external.virtualdj;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class OS2LServer {

    private final int port;
    private final boolean doPublish;
    private ServerSocketChannel serverSocket;
    private final List<SocketChannel> clients;
    private JmDNS jmdns;
    private VDJTimeService vdjTimeService;

    private final List<VDJBMPEvent> listeners = new ArrayList<>();

    private static final class InstanceHolder {
        private static final OS2LServer instance = new OS2LServer();

        static  {
            try {
                instance.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static OS2LServer getInstance() {
        return OS2LServer.InstanceHolder.instance;
    }

    public void addListener(VDJBMPEvent vdjbmpEvent){
       listeners.add(vdjbmpEvent);
     //   vdjTimeService.addListener(vdjbmpEvent);
    }

    public OS2LServer(int port, boolean doPublish) {
        this.port = port;
        this.doPublish = doPublish;
        this.clients = new CopyOnWriteArrayList<>();
    }

    public OS2LServer() {
        this(4444, true);
    }

    public void start() throws IOException {
        if (serverSocket != null && serverSocket.isOpen()) {
            throw new RuntimeException("OS2LServer is already running!");
        }

        serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(port));

        if (doPublish) {
            publishService();
        }

        new Thread(() -> {
            while (true) {
                try {
                    SocketChannel client = serverSocket.accept();
                    clients.add(client);

                    String remoteIP= client.getRemoteAddress().toString().substring(1, client.getRemoteAddress().toString().indexOf(':'));
                    for (VDJBMPEvent vdjbmpEvent : listeners)
                        vdjbmpEvent.remoteIp(remoteIP);

                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int bytesRead = client.read(buffer);

                    while (bytesRead != -1) {
                        buffer.flip();
                        final String event = new String (buffer.array(),0, bytesRead);

                        if (event.startsWith("{\"evt\":\"beat\"")) {
                            processBpmEvent(event);
                        }else
                        if (event.startsWith("{\"evt\":\"cmd\"")) {
                            processCommandEvent(event);
                        }else
                        if (event.startsWith("{\"evt\":\"btn\"")) {
                            processButtonEvent(event);
                        }else
                           System.err.println(event);


                        buffer.clear();
                        bytesRead = client.read(buffer);

                    }

                    clients.remove(client);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

     //   vdjTimeService = new VDJTimeService();
     //   vdjTimeService.start();
     //   listeners.add(vdjTimeService);
    }

    private void processBpmEvent(final String event){
        for (VDJBMPEvent vdjbmpEvent : listeners) {
            try {
                double bpm = getDoubleValue(event, "bpm");
                int pos = getIntValue(event, "pos");
                double strength = getDoubleValue(event, "strength",'}');
                boolean change = getBooleanValue(event, "change");

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
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void processCommandEvent(final String event){
        for (VDJBMPEvent vdjbmpEvent : listeners) {
            try {
                int id = getIntValue(event, "id");
                int param = getIntValue(event, "param",'}');

                vdjbmpEvent.command(id, param);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void processButtonEvent(final String event){
        for (VDJBMPEvent vdjbmpEvent : listeners) {
            try {
                String name = getStringValue(event, "name");
                String state = getStringValue(event, "state",'}');

                vdjbmpEvent.button(name, state);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private boolean getBooleanValue(final String event, final String value){
        return "true".equalsIgnoreCase(getValue(event, value));
    }

    private int getIntValue(final String event, final String value){
        return Integer.parseInt(getValue(event, value));
    }

    private int getIntValue(final String event, final String value, final char endChar){
        return Integer.parseInt(getValue(event, value, endChar));
    }

    private double getDoubleValue(final String event, final String value){
        return Double.parseDouble(getValue(event, value));
    }

    private double getDoubleValue(final String event, final String value, final char endChar){
        return Double.parseDouble(getValue(event, value, endChar));
    }

    private String getStringValue(final String event, final String value, final char endChar){
        final String raw = getValue(event, value, endChar);
        if (raw.isEmpty() || raw.length()<2)
            return raw;
        return raw.substring(1, raw.length()-1);
    }

    private String getStringValue(final String event, final String value){
        return getStringValue(event, value, ',');
    }

    private String getValue(final String event, final String value){
       return getValue(event, value, ',');
    }

    private String getValue(final String event, final String value, final char endChar){
        int ipos = event.indexOf(value);
        return event.substring(ipos + value.length() + 2, event.indexOf(endChar, ipos));
    }

    private void publishService() throws IOException {
        InetAddress addr = InetAddress.getLocalHost();
        jmdns = JmDNS.create(addr);
        ServiceInfo serviceInfo = ServiceInfo.create("os2l", "os2l", port, "");
        jmdns.registerService(serviceInfo);
    }

    public void stop() throws IOException {
        if (jmdns != null) {
            jmdns.unregisterAllServices();
            jmdns.close();
        }
        if (serverSocket != null) {
            serverSocket.close();
        }
    }

    public static void main(String[] args) throws IOException {
        OS2LServer server = new OS2LServer();
        server.start();
    }
}
