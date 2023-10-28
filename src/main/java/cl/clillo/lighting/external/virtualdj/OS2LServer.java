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
                            for (VDJBMPEvent vdjbmpEvent : listeners) {
                                try {
                                    int ipos = event.indexOf("bpm");
                                    String str = event.substring(ipos + 5, event.indexOf(',', ipos));
                                    double bpm = Double.parseDouble(str);

                                    ipos = event.indexOf("pos");
                                    str = event.substring(ipos + 5, event.indexOf(',', ipos));
                                    int pos = Integer.parseInt(str);

                                    ipos = event.indexOf("strength");
                                    str = event.substring(ipos + 10, event.indexOf('}', ipos));
                                    double strength = Double.parseDouble(str);

                                    ipos = event.indexOf("change");
                                    str = event.substring(ipos + 8, event.indexOf(',', ipos));
                                    boolean change = "true".equalsIgnoreCase(str);
                                  //  System.out.println(event + "\t" + str);
                                    //    double bpm = ;

                                    vdjbmpEvent.beat(change, pos, bpm, strength);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
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
