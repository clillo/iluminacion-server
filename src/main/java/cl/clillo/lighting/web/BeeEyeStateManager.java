package cl.clillo.lighting.web;

import cl.clillo.lighting.config.FixtureConfig;
import cl.clillo.lighting.config.FixturesConfigService;
import cl.clillo.lighting.external.dmx.Dmx;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestiona el estado de todos los BeeEye escuchando los valores DMX.
 * Lee la configuración desde FixturesConfigService.
 */
@Slf4j
public class BeeEyeStateManager implements Dmx.DmxListener {
    
    private final Map<String, BeeEyeState> beeEyes = new ConcurrentHashMap<>();
    private boolean initialized = false;
    
    public BeeEyeStateManager() {
        initializeBeeEyes();
    }
    
    /**
     * Inicializa los BeeEye desde la configuración.
     * Los LEDs empiezan en el canal 21 relativo al fixture (address + 21).
     */
    private void initializeBeeEyes() {
        if (initialized) return;
        
        try {
            FixturesConfigService configService = FixturesConfigService.getInstance();
            List<FixtureConfig> fixtures = configService.getConfig().getFixtures();
            
            for (FixtureConfig fixture : fixtures) {
                // Filtrar solo fixtures BeeEye
                if ("Moving Head Bee Eye".equals(fixture.getModel()) && fixture.isActivo()) {
                    // El canal base para los LEDs es address + 21
                    // (los primeros 20 canales son para pan/tilt/etc)
                    int baseChannel = fixture.getAddress() + 21;
                    int universe = fixture.getUniverse();
                    
                    beeEyes.put(fixture.getName(), new BeeEyeState(
                        fixture.getName(),
                        baseChannel,
                        universe
                    ));
                    
                    log.debug("Initialized BeeEye: {} - universe: {}, address: {}, baseChannel: {}",
                            fixture.getName(), universe, fixture.getAddress(), baseChannel);
                }
            }
            
            // Registrar como listener de DMX
            Dmx.getInstance().addListener(this);
            initialized = true;
            log.info("BeeEyeStateManager initialized with {} BeeEye fixtures", beeEyes.size());
        } catch (Exception e) {
            log.error("Error initializing BeeEyeStateManager", e);
            initialized = true; // Evitar reintentos infinitos
        }
    }
    
    @Override
    public void onDmxValueSent(int universe, int dmxChannel, int value) {
        // Actualizar todos los BeeEye que correspondan a este universo
        for (BeeEyeState beeEye : beeEyes.values()) {
            if (beeEye.getUniverse() == universe) {
                beeEye.setValue(dmxChannel, value);
            }
        }
    }
    
    /**
     * Obtiene el estado de todos los BeeEye.
     */
    public List<Map<String, Object>> getAllStates() {
        List<Map<String, Object>> states = new ArrayList<>();
        for (BeeEyeState beeEye : beeEyes.values()) {
            states.add(beeEye.toMap());
        }
        return states;
    }
    
    /**
     * Obtiene el estado de un BeeEye específico por nombre.
     */
    public BeeEyeState getBeeEye(String name) {
        return beeEyes.get(name);
    }
}


