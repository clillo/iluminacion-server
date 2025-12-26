package cl.clillo.lighting.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Servicio para generar el mapa DMX combinando la configuración de fixtures y tipos.
 */
@Slf4j
public class DmxMapService {

    private static final class InstanceHolder {
        private static final DmxMapService instance = new DmxMapService();
    }

    public static DmxMapService getInstance() {
        return InstanceHolder.instance;
    }

    private DmxMapService() {
    }

    /**
     * Representa una entrada en el mapa DMX.
     */
    @Data
    public static class DmxChannelEntry {
        private int universe;
        private int channel;
        private String fixtureName;
        private int fixtureId;
        private String fixtureModel;
        private int channelIndex; // Índice del canal dentro del fixture (0-based)
        private String channelName; // Nombre del canal si está disponible
    }

    /**
     * Genera el mapa completo de canales DMX.
     * @return Lista de entradas ordenadas por universo y canal
     */
    public List<DmxChannelEntry> generateDmxMap() {
        List<DmxChannelEntry> map = new ArrayList<>();
        
        FixturesConfigService fixturesConfigService = FixturesConfigService.getInstance();
        FixturesTypesConfigService typesConfigService = FixturesTypesConfigService.getInstance();
        
        FixturesConfig fixturesConfig = fixturesConfigService.getConfig();
        if (fixturesConfig == null || fixturesConfig.getFixtures() == null) {
            log.warn("No hay configuración de fixtures disponible");
            return map;
        }

        for (FixtureConfig fixtureConfig : fixturesConfig.getFixtures()) {
            // Solo procesar fixtures activos
            if (!fixtureConfig.isActivo()) {
                continue;
            }

            // Obtener el tipo de fixture para saber cuántos canales tiene
            FixtureTypeConfig fixtureType = typesConfigService.getFixtureType(fixtureConfig.getModel());
            if (fixtureType == null) {
                log.warn("No se encontró tipo de fixture para modelo: {}", fixtureConfig.getModel());
                continue;
            }

            int channelCount = fixtureType.getChannelCount() != null 
                    ? fixtureType.getChannelCount() 
                    : (fixtureType.getChannels() != null ? fixtureType.getChannels().size() : 0);

            if (channelCount == 0) {
                log.warn("Fixture {} tiene 0 canales", fixtureConfig.getName());
                continue;
            }

            // La dirección en el YAML es 1-based, pero DMX también es 1-based
            int startAddress = fixtureConfig.getAddress();
            int universe = fixtureConfig.getUniverse();
            List<String> channelNames = fixtureType.getChannels();

            // Crear una entrada para cada canal del fixture
            for (int i = 0; i < channelCount; i++) {
                DmxChannelEntry entry = new DmxChannelEntry();
                entry.setUniverse(universe);
                entry.setChannel(startAddress + i); // DMX es 1-based
                entry.setFixtureName(fixtureConfig.getName());
                entry.setFixtureId(fixtureConfig.getId());
                entry.setFixtureModel(fixtureConfig.getModel());
                entry.setChannelIndex(i);
                
                // Agregar nombre del canal si está disponible
                if (channelNames != null && i < channelNames.size() && channelNames.get(i) != null) {
                    entry.setChannelName(channelNames.get(i));
                } else {
                    entry.setChannelName("Channel " + (i + 1));
                }
                
                map.add(entry);
            }
        }

        // Ordenar por universo y luego por canal
        map.sort(Comparator
                .comparing(DmxChannelEntry::getUniverse)
                .thenComparing(DmxChannelEntry::getChannel));

        return map;
    }

    /**
     * Obtiene el mapa DMX agrupado por universo.
     * @return Mapa donde la clave es el número de universo y el valor es la lista de canales
     */
    public Map<Integer, List<DmxChannelEntry>> getDmxMapByUniverse() {
        Map<Integer, List<DmxChannelEntry>> mapByUniverse = new TreeMap<>();
        List<DmxChannelEntry> allEntries = generateDmxMap();

        for (DmxChannelEntry entry : allEntries) {
            mapByUniverse.computeIfAbsent(entry.getUniverse(), k -> new ArrayList<>()).add(entry);
        }

        return mapByUniverse;
    }

    /**
     * Genera el mapa DMX completo incluyendo canales vacíos.
     * Retorna un mapa donde la clave es el número de canal (1-512) y el valor es un mapa
     * de universo -> entrada DMX (o null si el canal está vacío).
     * @param maxChannels Número máximo de canales por universo (por defecto 512)
     * @return Mapa de canal -> universo -> entrada DMX
     */
    public Map<Integer, Map<Integer, DmxChannelEntry>> generateFullDmxMap(int maxChannels) {
        Map<Integer, Map<Integer, DmxChannelEntry>> fullMap = new TreeMap<>();
        
        // Inicializar todos los canales como vacíos
        FixturesConfigService fixturesConfigService = FixturesConfigService.getInstance();
        FixturesConfig fixturesConfig = fixturesConfigService.getConfig();
        int maxUniverses = fixturesConfig != null ? fixturesConfig.getMaxUniverses() : 2;
        
        for (int channel = 1; channel <= maxChannels; channel++) {
            Map<Integer, DmxChannelEntry> universeMap = new TreeMap<>();
            for (int universe = 1; universe <= maxUniverses; universe++) {
                universeMap.put(universe, null); // Canal vacío
            }
            fullMap.put(channel, universeMap);
        }
        
        // Llenar con los canales usados
        List<DmxChannelEntry> usedChannels = generateDmxMap();
        for (DmxChannelEntry entry : usedChannels) {
            if (entry.getChannel() <= maxChannels) {
                fullMap.get(entry.getChannel()).put(entry.getUniverse(), entry);
            }
        }
        
        return fullMap;
    }

    /**
     * Genera el mapa DMX completo con 512 canales por defecto.
     */
    public Map<Integer, Map<Integer, DmxChannelEntry>> generateFullDmxMap() {
        return generateFullDmxMap(512);
    }
}

