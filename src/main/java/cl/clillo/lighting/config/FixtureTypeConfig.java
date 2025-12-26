package cl.clillo.lighting.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuración de un tipo de fixture (modelo).
 */
@Data
@NoArgsConstructor
public class FixtureTypeConfig {
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("manufacturer")
    private String manufacturer;
    
    @JsonProperty("model")
    private String model;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("robotic")
    private boolean robotic = false;
    
    @JsonProperty("channelCount")
    private Integer channelCount;
    
    @JsonProperty("channels")
    private List<String> channels = new ArrayList<>();
    
    /**
     * Obtiene el array de canales como String[].
     * Si no hay canales definidos pero hay channelCount, crea un array vacío del tamaño especificado.
     */
    public String[] getChannelsArray() {
        if (channels != null && !channels.isEmpty()) {
            return channels.toArray(new String[0]);
        } else if (channelCount != null) {
            return new String[channelCount];
        }
        return new String[0];
    }
}

