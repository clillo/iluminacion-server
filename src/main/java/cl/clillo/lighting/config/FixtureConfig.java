package cl.clillo.lighting.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuración de un fixture individual.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FixtureConfig {
    @JsonProperty("id")
    private int id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("type")
    private String type; // "simple", "robotic", "simple-robotic"
    
    @JsonProperty("model")
    private String model; // "Laser", "beam+spot", "Moving Head 2", "Spider", "RGBW", "Derby", "Moving Head", "Moving Head Bee Eye"
    
    @JsonProperty("universe")
    private int universe = 1; // Por defecto universo 1
    
    @JsonProperty("address")
    private int address; // Dirección DMX (1-based)
    
    @JsonProperty("manufacturer")
    private String manufacturer;
    
    @JsonProperty("mode")
    private String mode;
    
    @JsonProperty("activo")
    private boolean activo = true; // Por defecto activo
    
    @JsonProperty("sceneIdOn")
    private Integer sceneIdOn; // ID de la escena para encender el fixture (opcional)
    
    @JsonProperty("sceneIdOff")
    private Integer sceneIdOff; // ID de la escena para apagar el fixture (opcional)
}

