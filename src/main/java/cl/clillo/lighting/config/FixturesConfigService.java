package cl.clillo.lighting.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Servicio para gestionar la configuración de fixtures desde YAML.
 */
@Slf4j
public class FixturesConfigService {
    
    private static final String CONFIG_FILE = "fixtures-config.yaml";
    private static final String RESOURCE_PATH = "fixtures-config.yaml";
    
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
    
    /**
     * Obtiene la ruta del archivo de recursos en el sistema de archivos.
     * Intenta encontrar src/main/resources/fixtures-config.yaml
     */
    private File getResourceFile() {
        try {
            URL resourceUrl = getClass().getClassLoader().getResource(RESOURCE_PATH);
            if (resourceUrl != null) {
                // Si es un archivo (no JAR), devolver el File directamente
                if ("file".equals(resourceUrl.getProtocol())) {
                    try {
                        return new File(resourceUrl.toURI());
                    } catch (URISyntaxException e) {
                        log.warn("Error al convertir URL a File: {}", e.getMessage());
                    }
                }
            }
            
            // Fallback: intentar construir la ruta relativa desde user.dir
            String userDir = System.getProperty("user.dir");
            Path resourcePath = Paths.get(userDir, "src", "main", "resources", CONFIG_FILE);
            File file = resourcePath.toFile();
            if (file.exists()) {
                return file;
            }
        } catch (Exception e) {
            log.warn("Error al obtener archivo de recursos: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * Carga la configuración desde el archivo YAML.
     * Intenta cargar desde src/main/resources, si no existe crea uno allí.
     */
    public void loadConfig() {
        try {
            // Intentar cargar desde el archivo de recursos
            File resourceFile = getResourceFile();
            if (resourceFile != null && resourceFile.exists()) {
                config = yamlMapper.readValue(resourceFile, FixturesConfig.class);
                configFile = resourceFile;
                log.info("Configuración cargada desde: {}", resourceFile.getAbsolutePath());
                return;
            }
            
            // Si no existe como archivo, intentar desde classpath (puede estar en JAR)
            var resource = getClass().getClassLoader().getResourceAsStream(RESOURCE_PATH);
            if (resource != null) {
                config = yamlMapper.readValue(resource, FixturesConfig.class);
                log.info("Configuración cargada desde classpath: {}", RESOURCE_PATH);
                
                // Intentar guardar en src/main/resources si es posible
                resourceFile = getResourceFile();
                if (resourceFile != null) {
                    // Crear directorio si no existe
                    resourceFile.getParentFile().mkdirs();
                    configFile = resourceFile;
                    saveConfig();
                } else {
                    // Fallback: guardar en user.dir
                    configFile = new File(System.getProperty("user.dir"), CONFIG_FILE);
                    saveConfig();
                }
            } else {
                log.warn("No se encontró el archivo de configuración. Creando uno por defecto.");
                config = new FixturesConfig();
                
                // Intentar crear en src/main/resources
                resourceFile = getResourceFile();
                if (resourceFile != null) {
                    resourceFile.getParentFile().mkdirs();
                    configFile = resourceFile;
                } else {
                    configFile = new File(System.getProperty("user.dir"), CONFIG_FILE);
                }
                saveConfig();
            }
        } catch (IOException e) {
            log.error("Error al cargar configuración de fixtures", e);
            config = new FixturesConfig();
            // Fallback: usar user.dir
            configFile = new File(System.getProperty("user.dir"), CONFIG_FILE);
        }
    }
    
    /**
     * Guarda la configuración en el archivo YAML.
     * Guarda en el mismo lugar desde donde se cargó.
     */
    public void saveConfig() throws IOException {
        if (configFile == null) {
            // Si no hay archivo definido, intentar usar el de recursos
            File resourceFile = getResourceFile();
            if (resourceFile != null) {
                resourceFile.getParentFile().mkdirs();
                configFile = resourceFile;
            } else {
                configFile = new File(System.getProperty("user.dir"), CONFIG_FILE);
            }
        }
        
        yamlMapper.writeValue(configFile, config);
        log.info("Configuración guardada en: {}", configFile.getAbsolutePath());
        
        // Recargar la configuración en memoria para asegurar sincronización
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

