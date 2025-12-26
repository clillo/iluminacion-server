package cl.clillo.lighting.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

/**
 * Servicio para gestionar la configuración de fixtures desde YAML.
 */
@Slf4j
public class FixturesConfigService {

    private static final String FIXTURES_CONFIG_YAML = "fixtures-config.yaml";

    private final ObjectMapper yamlMapper;
    private FixturesConfig config;
    private File configFile; // Archivo desde donde se carga/guarda
    
    private static final class InstanceHolder {
        private static final FixturesConfigService instance = new FixturesConfigService();
    }
    
    public static FixturesConfigService getInstance() {
        return InstanceHolder.instance;
    }
    
    private FixturesConfigService() {
        yamlMapper = new ObjectMapper(new YAMLFactory());
        loadConfig();
    }
    

    private File getResourceFile() {
        try {
            File file = new File(FIXTURES_CONFIG_YAML);
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
                config = yamlMapper.readValue(resourceFile, FixturesConfig.class);
                configFile = resourceFile;
                log.info("Configuración cargada desde: {}", resourceFile.getAbsolutePath());

            }
        } catch (IOException e) {
            log.error("Error al cargar configuración de fixtures", e);
        }
    }
    
    /**
     * Guarda la configuración en el archivo YAML.
     * Guarda en el mismo lugar desde donde se cargó.
     */
    public void saveConfig() throws IOException {

        yamlMapper.writeValue(configFile, config);
        log.info("Configuración guardada en: {}", configFile.getAbsolutePath());
        
        loadConfig();
    }
    
    /**
     * Obtiene la configuración actual.
     */
    public FixturesConfig getConfig() {
        return config;
    }
    
    /**
     * Actualiza la configuración de un fixture.
     */
    public void updateFixture(FixtureConfig fixtureConfig) throws IOException {
        boolean found = false;
        for (int i = 0; i < config.getFixtures().size(); i++) {
            if (config.getFixtures().get(i).getId() == fixtureConfig.getId()) {
                config.getFixtures().set(i, fixtureConfig);
                found = true;
                break;
            }
        }
        
        if (!found) {
            config.getFixtures().add(fixtureConfig);
        }
        
        saveConfig();
    }
    
    /**
     * Obtiene la configuración de un fixture por ID.
     */
    public FixtureConfig getFixture(int id) {
        return config.getFixtures().stream()
                .filter(f -> f.getId() == id)
                .findFirst()
                .orElse(null);
    }
}

