package cl.clillo.lighting.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Servicio para gestionar la configuración de red, especialmente interfaces cableadas.
 */
@Slf4j
public class NetworkConfigService {

    private static final class InstanceHolder {
        private static final NetworkConfigService instance = new NetworkConfigService();
    }

    public static NetworkConfigService getInstance() {
        return InstanceHolder.instance;
    }

    private NetworkConfigService() {
    }

    @Data
    public static class NetworkInterfaceInfo {
        private String name;
        private String displayName;
        private String ipAddress;
        private String subnetMask;
        private boolean isUp;
        private boolean isLoopback;
        private String macAddress;
        private int interfaceNumber; // Para ordenamiento numérico (en0 -> 0, en10 -> 10)
    }

    /**
     * Obtiene todas las interfaces de red cableadas (Ethernet) con sus IPs IPv4.
     * En Mac, las interfaces cableadas suelen ser "en0", "en1", etc.
     * Filtra interfaces que:
     * - No sean loopback
     * - Estén activas (up)
     * - Tengan una IP IPv4 asignada
     * - Preferiblemente sean interfaces Ethernet (nombre empieza con "en" en Mac)
     */
    public List<NetworkInterfaceInfo> getWiredNetworkInterfaces() {
        List<NetworkInterfaceInfo> interfaces = new ArrayList<>();

        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();

                try {
                    // Filtrar interfaces que no están activas o son loopback
                    if (!ni.isUp() || ni.isLoopback()) {
                        continue;
                    }

                    // En Mac, las interfaces cableadas suelen ser "en0", "en1", etc.
                    // También pueden ser "eth0", "eth1" en otros sistemas
                    String name = ni.getName();

                    // Obtener IPs IPv4 y máscaras de subred de esta interfaz
                    List<InterfaceAddress> interfaceAddresses = ni.getInterfaceAddresses();
                    
                    for (InterfaceAddress ifAddr : interfaceAddresses) {
                        InetAddress address = ifAddr.getAddress();
                        if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
                            NetworkInterfaceInfo info = new NetworkInterfaceInfo();
                            info.setName(name);
                            info.setDisplayName(getDisplayName(ni));
                            info.setIpAddress(address.getHostAddress());
                            
                            // Obtener máscara de subred
                            short prefixLength = ifAddr.getNetworkPrefixLength();
                            String subnetMask = prefixLengthToSubnetMask(prefixLength);
                            info.setSubnetMask(subnetMask);
                            
                            info.setUp(ni.isUp());
                            info.setLoopback(ni.isLoopback());
                            
                            // Extraer número de interfaz para ordenamiento (en0 -> 0, en10 -> 10)
                            int interfaceNum = extractInterfaceNumber(name);
                            info.setInterfaceNumber(interfaceNum);
                            
                            byte[] mac = ni.getHardwareAddress();
                            if (mac != null) {
                                StringBuilder macStr = new StringBuilder();
                                for (int i = 0; i < mac.length; i++) {
                                    if (i > 0) macStr.append(":");
                                    macStr.append(String.format("%02X", mac[i]));
                                }
                                info.setMacAddress(macStr.toString());
                            }

                            interfaces.add(info);
                        }
                    }
                } catch (SocketException e) {
                    log.warn("Error al obtener información de la interfaz {}: {}", ni.getName(), e.getMessage());
                }
            }

            // Ordenar: primero interfaces cableadas (en*), luego otras, ordenadas numéricamente
            interfaces.sort((a, b) -> {
                boolean aWired = a.getName().startsWith("en") || a.getName().startsWith("eth");
                boolean bWired = b.getName().startsWith("en") || b.getName().startsWith("eth");
                
                if (aWired && !bWired) return -1;
                if (!aWired && bWired) return 1;
                
                // Si ambas son cableadas o ambas no, ordenar numéricamente
                if (aWired && bWired) {
                    return Integer.compare(a.getInterfaceNumber(), b.getInterfaceNumber());
                }
                
                return a.getName().compareTo(b.getName());
            });

        } catch (SocketException e) {
            log.error("Error al obtener interfaces de red", e);
        }

        return interfaces;
    }

    /**
     * Obtiene un nombre más legible para la interfaz.
     */
    private String getDisplayName(NetworkInterface ni) {
        String name = ni.getName();
        String displayName = ni.getDisplayName();
        
        // En Mac, mejorar los nombres de las interfaces
        if (name.startsWith("en")) {
            if (name.equals("en0")) {
                return "Ethernet (en0)";
            } else if (name.equals("en1")) {
                return "Ethernet (en1)";
            } else {
                return "Ethernet (" + name + ")";
            }
        } else if (name.startsWith("eth")) {
            return "Ethernet (" + name + ")";
        } else if (name.startsWith("wlan") || name.startsWith("wlp")) {
            return "WiFi (" + name + ")";
        }
        
        return displayName != null && !displayName.isEmpty() ? displayName : name;
    }

    /**
     * Extrae el número de la interfaz para ordenamiento numérico.
     * Ejemplo: "en0" -> 0, "en10" -> 10, "eth1" -> 1
     */
    private int extractInterfaceNumber(String interfaceName) {
        // Extraer números del final del nombre
        String numbers = interfaceName.replaceAll("[^0-9]", "");
        if (numbers.isEmpty()) {
            return Integer.MAX_VALUE; // Si no hay números, poner al final
        }
        try {
            return Integer.parseInt(numbers);
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    /**
     * Convierte el prefix length (CIDR) a máscara de subred.
     * Ejemplo: 24 -> 255.255.255.0
     */
    private String prefixLengthToSubnetMask(short prefixLength) {
        int mask = 0xffffffff << (32 - prefixLength);
        return String.format("%d.%d.%d.%d",
                (mask >>> 24) & 0xff,
                (mask >>> 16) & 0xff,
                (mask >>> 8) & 0xff,
                mask & 0xff);
    }
}

