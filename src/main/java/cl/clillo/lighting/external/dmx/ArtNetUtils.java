package cl.clillo.lighting.external.dmx;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
public final class ArtNetUtils {

    private static final int DISCOVERY_TIMEOUT_MS = 500;

    public record Detection(NetworkInterfaceScanner.InterfaceInfo networkInterface, InetAddress nodeAddress) {

        public String localAddress() {
            return networkInterface.address().getHostAddress();
        }
    }

    private ArtNetUtils() {
    }

    public static Optional<Detection> detect(final String preferredLocalAddress) {
        for (NetworkInterfaceScanner.InterfaceInfo networkInterface : interfacesInPriorityOrder(preferredLocalAddress)) {
            final List<InetAddress> nodes = discover(networkInterface);
            if (!nodes.isEmpty())
                return Optional.of(new Detection(networkInterface, nodes.getFirst()));
        }
        return Optional.empty();
    }

    public static boolean isReachable(final Detection detection) {
        return discover(detection.networkInterface()).contains(detection.nodeAddress());
    }

    private static List<NetworkInterfaceScanner.InterfaceInfo> interfacesInPriorityOrder(final String preferredLocalAddress) {
        final List<NetworkInterfaceScanner.InterfaceInfo> interfaces;
        try {
            interfaces = new ArrayList<>(NetworkInterfaceScanner.findIpv4Interfaces());
        } catch (SocketException e) {
            log.warn("Could not list network interfaces", e);
            return List.of();
        }
        interfaces.sort(Comparator.comparing(networkInterface ->
                !networkInterface.address().getHostAddress().equals(preferredLocalAddress)));
        return interfaces;
    }

    private static List<InetAddress> discover(final NetworkInterfaceScanner.InterfaceInfo networkInterface) {
        try {
            return ArtNetDiscovery.discover(networkInterface, DISCOVERY_TIMEOUT_MS);
        } catch (Exception e) {
            log.debug("Art-Net discovery failed on {}", networkInterface.interfaceName(), e);
            return List.of();
        }
    }

    public static void main(String[] args) {
        System.out.println(detect(null)
                .map(detection -> "Local: " + detection.localAddress() + "  Nodo: " + detection.nodeAddress().getHostAddress())
                .orElse("No se encontró ningún nodo Art-Net"));
    }
}
