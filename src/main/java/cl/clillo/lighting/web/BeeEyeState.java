package cl.clillo.lighting.web;

import lombok.Getter;

/**
 * Estado de un fixture BeeEye.
 * Mantiene los valores RGBW de los 7 LEDs (6 en anillo + 1 central).
 */
@Getter
public class BeeEyeState {
    private static final int LED_COUNT = 7;
    
    private final String name;
    private final int baseChannel;
    private final int universe;
    
    private final int[] red = new int[LED_COUNT];
    private final int[] green = new int[LED_COUNT];
    private final int[] blue = new int[LED_COUNT];
    private final int[] white = new int[LED_COUNT];
    
    public BeeEyeState(String name, int baseChannel, int universe) {
        this.name = name;
        this.baseChannel = baseChannel;
        this.universe = universe;
    }
    
    /**
     * Actualiza un valor DMX.
     * Mapeo: desde baseChannel, cada LED ocupa 4 canales en orden R,G,B,W.
     * LED i empieza en baseChannel + i*4.
     */
    public void setValue(int channel, int value) {
        int offset = channel - baseChannel;
        if (offset < 0) return;
        int totalChannels = LED_COUNT * 4;
        if (offset >= totalChannels) return;
        int ledIndex = offset / 4;
        int component = offset % 4; // 0=R,1=G,2=B,3=W
        int v = clamp255(value);
        
        switch (component) {
            case 0: red[ledIndex] = v; break;
            case 1: green[ledIndex] = v; break;
            case 2: blue[ledIndex] = v; break;
            case 3: white[ledIndex] = v; break;
        }
    }
    
    /**
     * Obtiene el color RGB visible mezclando el canal blanco.
     */
    public int[] getDisplayColor(int index) {
        if (index < 0 || index >= LED_COUNT) {
            return new int[]{0, 0, 0};
        }
        int r = clamp255(red[index] + white[index]);
        int g = clamp255(green[index] + white[index]);
        int b = clamp255(blue[index] + white[index]);
        return new int[]{r, g, b};
    }
    
    /**
     * Obtiene el estado completo como un mapa para JSON.
     */
    public java.util.Map<String, Object> toMap() {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("name", name);
        map.put("baseChannel", baseChannel);
        map.put("universe", universe);
        
        java.util.List<java.util.Map<String, Object>> leds = new java.util.ArrayList<>();
        for (int i = 0; i < LED_COUNT; i++) {
            java.util.Map<String, Object> led = new java.util.HashMap<>();
            int[] color = getDisplayColor(i);
            led.put("index", i);
            led.put("r", color[0]);
            led.put("g", color[1]);
            led.put("b", color[2]);
            led.put("red", red[i]);
            led.put("green", green[i]);
            led.put("blue", blue[i]);
            led.put("white", white[i]);
            leds.add(led);
        }
        map.put("leds", leds);
        
        return map;
    }
    
    private static int clamp255(int v) {
        if (v < 0) return 0;
        return Math.min(v, 255);
    }
}


