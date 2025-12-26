package cl.clillo.lighting.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para gestionar la configuración de tipos de fixtures desde YAML.
 */
@Slf4j
public class FixturesTypesConfigService {

    private static final String FIXTURES_TYPES_CONFIG_YAML = "fixtures-types-config.yaml";

    private final ObjectMapper yamlMapper;
    private FixturesTypesConfig config;
    private Map<String, FixtureTypeConfig> fixtureTypesMap; // Cache para búsqueda rápida por nombre
    
    private static final class InstanceHolder {
        private static final FixturesTypesConfigService instance = new FixturesTypesConfigService();
    }
    
    public static FixturesTypesConfigService getInstance() {
        return InstanceHolder.instance;
    }
    
    private FixturesTypesConfigService() {
        yamlMapper = new ObjectMapper(new YAMLFactory());
        loadConfig();
    }
    

    private File getResourceFile() {
        try {
            File file = new File(FIXTURES_TYPES_CONFIG_YAML);
            if (file.exists()) {
                return file;
            }
        } catch (Exception e) {
            log.warn("Error al obtener archivo de recursos: {}", e.getMessage());
        }
        return null;
    }
    
    public void loadConfig() {
        try {
            File resourceFile = getResourceFile();
            if (resourceFile != null && resourceFile.exists()) {
                config = yamlMapper.readValue(resourceFile, FixturesTypesConfig.class);
                buildCache();
                log.info("Configuración de tipos de fixtures cargada desde: {}", resourceFile.getAbsolutePath());
            } else {
                log.warn("No se encontró el archivo de configuración: {}", FIXTURES_TYPES_CONFIG_YAML);
                config = new FixturesTypesConfig();
                fixtureTypesMap = new HashMap<>();
            }
        } catch (IOException e) {
            log.error("Error al cargar configuración de tipos de fixtures", e);
            config = new FixturesTypesConfig();
            fixtureTypesMap = new HashMap<>();
        }
    }
    
    private void buildCache() {
        fixtureTypesMap = new HashMap<>();
        if (config != null && config.getFixtureTypes() != null) {
            for (FixtureTypeConfig fixtureType : config.getFixtureTypes()) {
                if (fixtureType.getName() != null) {
                    fixtureTypesMap.put(fixtureType.getName().toLowerCase(), fixtureType);
                }
            }
        }
    }
    
    /**
     * Obtiene la configuración actual.
     */
    public FixturesTypesConfig getConfig() {
        return config;
    }
    
    /**
     * Obtiene un tipo de fixture por nombre (case-insensitive).
     */
    public FixtureTypeConfig getFixtureType(String name) {
        if (name == null || fixtureTypesMap == null) {
            return null;
        }
        return fixtureTypesMap.get(name.toLowerCase());
    }
}

