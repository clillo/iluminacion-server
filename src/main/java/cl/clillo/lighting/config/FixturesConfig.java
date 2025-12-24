package cl.clillo.lighting.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuración completa de todos los fixtures.
 */
@Data
public class FixturesConfig {
    @JsonProperty("maxUniverses")
    private int maxUniverses = 2; // Por defecto 2 universos
    
    @JsonProperty("fixtures")
    private List<FixtureConfig> fixtures = new ArrayList<>();
}

