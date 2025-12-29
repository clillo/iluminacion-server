package cl.clillo.lighting.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

/**
 * Servicio para gestionar la configuración externa (ArtNet, etc.) desde YAML.
 */
@Slf4j
public class ExternalConfigService {

    private static final String EXTERNAL_CONFIG_YAML = "external-config.yaml";

    private final ObjectMapper yamlMapper;
    private ExternalConfig config;
    private File configFile;
    
    private static final class InstanceHolder {
        private static final ExternalConfigService instance = new ExternalConfigService();
    }
    
    public static ExternalConfigService getInstance() {
        return InstanceHolder.instance;
    }
    
    private ExternalConfigService() {
        yamlMapper = new ObjectMapper(new YAMLFactory());
        loadConfig();
    }
    

    private File getResourceFile() {
        try {
            File file = new File(EXTERNAL_CONFIG_YAML);
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
                config = yamlMapper.readValue(resourceFile, ExternalConfig.class);
                configFile = resourceFile;
                log.info("Configuración externa cargada desde: {}", resourceFile.getAbsolutePath());
            } else {
                // Si no existe, crear configuración por defecto
                config = new ExternalConfig();
                log.warn("No se encontró el archivo de configuración externa: {}. Usando valores por defecto.", EXTERNAL_CONFIG_YAML);
            }
        } catch (IOException e) {
            log.error("Error al cargar configuración externa", e);
            config = new ExternalConfig();
        }
    }
    
    /**
     * Guarda la configuración en el archivo YAML.
     */
    public void saveConfig() throws IOException {
        if (configFile == null) {
            configFile = new File(EXTERNAL_CONFIG_YAML);
        }
        
        yamlMapper.writeValue(configFile, config);
        log.info("Configuración externa guardada en: {}", configFile.getAbsolutePath());
        
        loadConfig();
    }
    
    /**
     * Obtiene la configuración actual.
     */
    public ExternalConfig getConfig() {
        return config;
    }
    
    /**
     * Obtiene la IP de ArtNet.
     */
    public String getArtNetIpAddress() {
        if (config != null && config.getArtNet() != null && config.getArtNet().getIpAddress() != null) {
            return config.getArtNet().getIpAddress();
        }
        return "192.168.255.255"; // Broadcast por defecto
    }
    
    /**
     * Actualiza la IP de ArtNet.
     */
    public void setArtNetIpAddress(String ipAddress) throws IOException {
        if (config == null) {
            config = new ExternalConfig();
        }
        if (config.getArtNet() == null) {
            config.setArtNet(new ExternalConfig.ArtNetConfig());
        }
        config.getArtNet().setIpAddress(ipAddress);
        saveConfig();
    }
}

