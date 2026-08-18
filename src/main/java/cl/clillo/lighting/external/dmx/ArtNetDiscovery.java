package cl.clillo.lighting.external.dmx;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class ArtNetDiscovery {

    private static final int ARTNET_PORT = 6454;

    private ArtNetDiscovery() {
    }

    public static List<InetAddress> discover(
            NetworkInterfaceScanner.InterfaceInfo interfaceInfo,
            int timeoutMs
    ) throws Exception {

        List<InetAddress> nodes = new ArrayList<>();

        InetAddress localAddress = interfaceInfo.address();
        InetAddress broadcastAddress = interfaceInfo.broadcast();

        if (broadcastAddress == null) {
            return nodes;
        }

        try (DatagramSocket socket =
                     new DatagramSocket(
                             new InetSocketAddress(localAddress, 0))) {

            socket.setBroadcast(true);
            socket.setSoTimeout(timeoutMs);

            byte[] artPoll = createArtPoll();

            DatagramPacket request =
                    new DatagramPacket(
                            artPoll,
                            artPoll.length,
                            broadcastAddress,
                            ARTNET_PORT
                    );

            socket.send(request);

            long end =
                    System.currentTimeMillis() + timeoutMs;

            while (System.currentTimeMillis() < end) {
                try {
                    byte[] buffer = new byte[1024];

                    DatagramPacket response =
                            new DatagramPacket(
                                    buffer,
                                    buffer.length
                            );

                    socket.receive(response);

                    if (isArtPollReply(
                            response.getData(),
                            response.getLength())) {

                        nodes.add(response.getAddress());

                        System.out.println(
                                "Art-Net node encontrado: "
                                        + response.getAddress()
                                        .getHostAddress()
                        );
                    }

                } catch (SocketTimeoutException e) {
                    break;
                }
            }
        }

        return nodes;
    }

    private static byte[] createArtPoll() {

        byte[] packet = new byte[14];

        byte[] id =
                "Art-Net\0".getBytes(StandardCharsets.US_ASCII);

        System.arraycopy(
                id,
                0,
                packet,
                0,
                id.length
        );

        // OpCode ArtPoll = 0x2000
        // Art-Net usa little endian para OpCode.
        packet[8] = 0x00;
        packet[9] = 0x20;

        // Protocol version 14, big endian
        packet[10] = 0x00;
        packet[11] = 0x0E;

        // TalkToMe
        packet[12] = 0x00;

        // Priority
        packet[13] = 0x00;

        return packet;
    }

    private static boolean isArtPollReply(
            byte[] data,
            int length
    ) {

        if (length < 10) {
            return false;
        }

        String id =
                new String(
                        data,
                        0,
                        8,
                        StandardCharsets.US_ASCII
                );

        if (!"Art-Net\0".equals(id)) {
            return false;
        }

        int opcode =
                (data[8] & 0xff)
                        | ((data[9] & 0xff) << 8);

        // ArtPollReply = 0x2100
        return opcode == 0x2100;
    }
}