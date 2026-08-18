package cl.clillo.lighting.external.dmx;

import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NetworkInterfaceScanner {

    public record InterfaceInfo(
            String interfaceName,
            String displayName,
            Inet4Address address,
            Inet4Address broadcast,
            short prefixLength
    ) {
        @Override
        public String toString() {
            return "%s (%s): ip=%s/%d broadcast=%s"
                    .formatted(
                            interfaceName,
                            displayName,
                            address.getHostAddress(),
                            prefixLength,
                            broadcast != null
                                    ? broadcast.getHostAddress()
                                    : "N/A"
                    );
        }
    }

    private NetworkInterfaceScanner() {
    }

    public static List<InterfaceInfo> findIpv4Interfaces()
            throws SocketException {

        List<InterfaceInfo> result = new ArrayList<>();

        for (NetworkInterface networkInterface :
                Collections.list(NetworkInterface.getNetworkInterfaces())) {

            if (!networkInterface.isUp()) {
                continue;
            }

            if (networkInterface.isLoopback()) {
                continue;
            }

            for (InterfaceAddress interfaceAddress :
                    networkInterface.getInterfaceAddresses()) {

                if (!(interfaceAddress.getAddress()
                        instanceof Inet4Address ipv4)) {
                    continue;
                }

                InetAddress broadcastAddress =
                        interfaceAddress.getBroadcast();

                Inet4Address broadcast =
                        broadcastAddress instanceof Inet4Address
                                ? (Inet4Address) broadcastAddress
                                : null;

                result.add(new InterfaceInfo(
                        networkInterface.getName(),
                        networkInterface.getDisplayName(),
                        ipv4,
                        broadcast,
                        interfaceAddress.getNetworkPrefixLength()
                ));
            }
        }

        return result;
    }

    public static void main(String[] args) throws Exception {
        for (InterfaceInfo info : findIpv4Interfaces()) {
            System.out.println(info);
        }
    }
}