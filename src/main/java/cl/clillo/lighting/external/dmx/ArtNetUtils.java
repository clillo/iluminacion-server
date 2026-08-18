package cl.clillo.lighting.external.dmx;

public class ArtNetUtils {

    private String localArtNetAddress;
    private String artNetNodeAddress;

    public void detectArtNet() {
        try {
            for (var iface : NetworkInterfaceScanner.findIpv4Interfaces()) {
                var nodes = ArtNetDiscovery.discover(iface,500);

                if (!nodes.isEmpty()) {
                    localArtNetAddress = iface.address().getHostAddress();
                    artNetNodeAddress = nodes.getFirst().getHostAddress();

                    System.out.println("Art-Net detectado:");
                    System.out.println("  Local : " + localArtNetAddress);
                    System.out.println("  Nodo  : " + artNetNodeAddress);

                    return;
                }
            }

            System.err.println("No se encontró ningún nodo Art-Net");

        } catch (Exception e) {
            throw new RuntimeException("Error detectando Art-Net", e);
        }
    }

    public String getArtNetNodeAddress() {
        return artNetNodeAddress;
    }

    public String getLocalArtNetAddress() {
        return localArtNetAddress;
    }

    public static void main(String[] args) {
        ArtNetUtils artNetUtils = new ArtNetUtils();
        artNetUtils.detectArtNet();
    }
}
