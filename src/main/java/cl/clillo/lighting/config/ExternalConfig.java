package cl.clillo.lighting.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Configuración de parámetros externos (ArtNet, etc.)
 */
@Data
public class ExternalConfig {
    @JsonProperty("artNet")
    private ArtNetConfig artNet = new ArtNetConfig();

    @Data
    public static class ArtNetConfig {
        @JsonProperty("ipAddress")
        private String ipAddress = "192.168.255.255"; // Broadcast por defecto
    }
}

