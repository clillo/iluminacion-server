package cl.clillo.lighting.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuración de todos los tipos de fixtures.
 */
@Data
public class FixturesTypesConfig {
    @JsonProperty("fixtureTypes")
    private List<FixtureTypeConfig> fixtureTypes = new ArrayList<>();
}

