package cl.clillo.lighting.external.virtualdj;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

public class OS2LServer {

    private final int port;
    private final boolean doPublish;
    private ServerSocketChannel serverSocket;
    private final List<SocketChannel> clients;
    private JmDNS jmdns;

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

                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int bytesRead = client.read(buffer);

                    while (bytesRead != -1) {
                        buffer.flip();
                        System.out.println(new String (buffer.array(),0, bytesRead));


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
        // Clear clients and other cleanups as needed.
    }

    public static void main(String[] args) throws IOException {
        OS2LServer server = new OS2LServer();
        server.start();
    }
}
