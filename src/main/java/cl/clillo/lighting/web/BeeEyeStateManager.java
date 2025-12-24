package cl.clillo.lighting.web;

import cl.clillo.lighting.external.dmx.Dmx;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestiona el estado de todos los BeeEye escuchando los valores DMX.
 */
@Slf4j
public class BeeEyeStateManager implements Dmx.DmxListener {
    
    private final Map<String, BeeEyeState> beeEyes = new ConcurrentHashMap<>();
    private boolean initialized = false;
    
    public BeeEyeStateManager() {
        initializeBeeEyes();
    }
    
    /**
     * Inicializa los 4 BeeEye con sus canales base.
     * Canales base: 21, 70, 120, 170 (según BeeEyeDemo)
     */
    private void initializeBeeEyes() {
        if (initialized) return;
        
        // Configuración de los 4 BeeEye según BeeEyeDemo
        beeEyes.put("BeeEye 1", new BeeEyeState("BeeEye 1", 21, 1));
        beeEyes.put("BeeEye 2", new BeeEyeState("BeeEye 2", 70, 1));
        beeEyes.put("BeeEye 3", new BeeEyeState("BeeEye 3", 120, 1));
        beeEyes.put("BeeEye 4", new BeeEyeState("BeeEye 4", 170, 1));
        
        // Registrar como listener de DMX
        Dmx.getInstance().addListener(this);
        initialized = true;
        log.info("BeeEyeStateManager initialized with {} BeeEye fixtures", beeEyes.size());
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


