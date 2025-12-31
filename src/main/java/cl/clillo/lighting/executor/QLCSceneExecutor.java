package cl.clillo.lighting.executor;

import cl.clillo.lighting.external.dmx.Dmx;
import cl.clillo.lighting.fixture.qlc.QLCFixture;
import cl.clillo.lighting.model.ColorsCatalog;
import cl.clillo.lighting.model.LedPoint;
import cl.clillo.lighting.model.QLCPoint;
import cl.clillo.lighting.model.QLCScene;
import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.model.ShowCollection;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@Slf4j
public class QLCSceneExecutor extends AbstractExecutor {

    private final Dmx dmx = Dmx.getInstance();
    private final Show show;
    private int sendNumber;

    public QLCSceneExecutor(final Show show) {
        super(show, List.of());
        this.show = show;
    }

    @Override
    public void executeDefaultScheduler() {
        final QLCScene scene = show.getFunction();
        
        // Detectar escena de colores aleatorios BeeEye - generar colores en la primera ejecución
        if ("beeeye.random.colors".equals(scene.getSubType())) {
            if (show.isFirstTimeExecution()) {
                generateRandomColorsForBeeEye(scene);
                show.setFirstTimeExecution(false);
                sendNumber = 2;
            }
        } else if (show.isFirstTimeExecution()){
            show.setFirstTimeExecution(false);
            sendNumber = 2;
        }

        if (sendNumber<=0)
            return;

        sendNumber--;
        log.info("Sending "+ show.getId()+ "\t" +scene.getType() + "\t"+scene.getName());

        // Detectar escena de colores aleatorios BeeEye
        if ("beeeye.random.colors".equals(scene.getSubType())) {
            executeRandomColorsBeeEye(scene);
        } else if (scene.getLedPoints()!=null && !scene.getLedPoints().isEmpty()) {
            // Ejecuta LedPoints: para cada fixture involucrado en la escena, envía RGBW de cada LED del anillo
            final Set<QLCFixture> fixtures = collectFixtures(scene);
            for (QLCFixture fixture : fixtures) {
                if (log.isDebugEnabled()) {
                    log.debug("LedPoints for fixture id={} name={} addr={} universe={}",
                            fixture.getId(), fixture.getName(), fixture.getAddress(), fixture.getUniverse());
                }
                for (LedPoint lp : scene.getLedPoints()) {
                    int ledIndex = Math.max(0, lp.getId());
                    int base = 21 + ledIndex * 4; // R,G,B,W relativos al fixture
                    int chR = fixture.getDMXChannel(base + 0);
                    int chG = fixture.getDMXChannel(base + 1);
                    int chB = fixture.getDMXChannel(base + 2);
                    int chW = fixture.getDMXChannel(base + 3);
                    // Resolver por catálogo si hay nombre; si no, usar los valores del LedPoint
                    int r, g, b, w;
                    if (lp.getName()!=null && !lp.getName().isBlank()) {
                        final ColorsCatalog.ColorEntry ce = ColorsCatalog.get(lp.getName());
                        if (ce == null) {
                            log.warn("Color '{}' not found in catalog; using LedPoint RGBW as fallback", lp.getName());
                            r = clamp(lp.getR()); g = clamp(lp.getG()); b = clamp(lp.getB()); w = clamp(lp.getW());
                        } else {
                            r = clamp(ce.getR()); g = clamp(ce.getG()); b = clamp(ce.getB()); w = clamp(ce.getW());
                        }
                    } else {
                        r = clamp(lp.getR()); g = clamp(lp.getG()); b = clamp(lp.getB()); w = clamp(lp.getW());
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("  LED {} ({}) -> R:{}@{} G:{}@{} B:{}@{} W:{}@{}",
                                ledIndex, lp.getName(),
                                r, chR, g, chG, b, chB, w, chW);
                    }
                    dmx.send(fixture.getUniverse(), chR, r);
                    dmx.send(fixture.getUniverse(), chG, g);
                    dmx.send(fixture.getUniverse(), chB, b);
                    dmx.send(fixture.getUniverse(), chW, w);
                }
            }
        } else {
            // Fallback al comportamiento clásico con QLCPoint
            for (QLCPoint qlcPoint: scene.getQlcPointList())
                dmx.send(qlcPoint);
        }

        if (scene.isTotalBlackout()){
            for (Show show: ShowCollection.getInstance().getShowList())
                show.setExecuting(false);
        }

        show.setNextExecutionTime(System.currentTimeMillis() + 100);
    }

    /**
     * Ejecuta una escena BeeEye con colores aleatorios.
     * Envía los colores aleatorios generados previamente.
     */
    private void executeRandomColorsBeeEye(QLCScene scene) {
        // Enviar los colores generados
        final List<QLCFixture> fixturesList = new ArrayList<>(collectFixtures(scene));
        if (scene.getLedPoints() == null || scene.getLedPoints().isEmpty()) {
            log.warn("No LedPoints available for random colors scene");
            return;
        }
        
        // Los LedPoints están ordenados: fixture 0 (LEDs 0-6), fixture 1 (LEDs 0-6), etc.
        // Cada LedPoint tiene un ID que es el índice del LED dentro del fixture (0-6)
        int ledPointIndex = 0;
        for (int fixtureIndex = 0; fixtureIndex < fixturesList.size(); fixtureIndex++) {
            QLCFixture fixture = fixturesList.get(fixtureIndex);
            if (log.isDebugEnabled()) {
                log.debug("Random colors for fixture id={} name={} addr={} universe={}",
                        fixture.getId(), fixture.getName(), fixture.getAddress(), fixture.getUniverse());
            }
            
            // Enviar colores para los 7 LEDs de este fixture
            for (int led = 0; led < 7; led++) {
                LedPoint lp = null;
                if (ledPointIndex < scene.getLedPoints().size()) {
                    lp = scene.getLedPoints().get(ledPointIndex);
                    ledPointIndex++;
                }
                
                if (lp == null) {
                    // Si no hay LedPoint para este LED, usar valores 0
                    lp = LedPoint.builder().id(led).r(0).g(0).b(0).w(0).build();
                }
                
                int base = 21 + led * 4; // R,G,B,W relativos al fixture
                int chR = fixture.getDMXChannel(base + 0);
                int chG = fixture.getDMXChannel(base + 1);
                int chB = fixture.getDMXChannel(base + 2);
                int chW = fixture.getDMXChannel(base + 3);
                
                int r = clamp(lp.getR());
                int g = clamp(lp.getG());
                int b = clamp(lp.getB());
                int w = clamp(lp.getW());
                
                if (log.isDebugEnabled()) {
                    log.debug("  LED {} ({}) -> R:{}@{} G:{}@{} B:{}@{} W:{}@{}",
                            led, lp.getName(), r, chR, g, chG, b, chB, w, chW);
                }
                dmx.send(fixture.getUniverse(), chR, r);
                dmx.send(fixture.getUniverse(), chG, g);
                dmx.send(fixture.getUniverse(), chB, b);
                dmx.send(fixture.getUniverse(), chW, w);
            }
        }
    }

    /**
     * Genera colores aleatorios para cada LED de cada fixture BeeEye.
     * Los colores se eligen aleatoriamente del catálogo de colores (excluyendo "off").
     * Se recalcula cada vez que se inicia la escena.
     */
    private void generateRandomColorsForBeeEye(QLCScene scene) {
        final Map<String, ColorsCatalog.ColorEntry> palette = ColorsCatalog.getAll();
        final List<String> colorNames = new ArrayList<>();
        
        // Filtrar colores disponibles (excluir "off")
        for (String colorName : palette.keySet()) {
            if (!"off".equals(colorName)) {
                colorNames.add(colorName);
            }
        }
        
        if (colorNames.isEmpty()) {
            log.warn("No colors available in catalog for random selection");
            return;
        }
        
        final Random random = new Random();
        final Set<QLCFixture> fixtures = collectFixtures(scene);
        final List<LedPoint> newLedPoints = new ArrayList<>();
        
        // Generar colores aleatorios para cada fixture y cada LED (7 LEDs por fixture)
        // Los LedPoints se generan en orden: fixture 0 (LEDs 0-6), fixture 1 (LEDs 0-6), etc.
        for (QLCFixture fixture : fixtures) {
            for (int led = 0; led < 7; led++) {
                // Elegir un color aleatorio del catálogo
                String randomColorName = colorNames.get(random.nextInt(colorNames.size()));
                ColorsCatalog.ColorEntry color = palette.get(randomColorName);
                
                if (color != null) {
                    LedPoint lp = LedPoint.builder()
                            .id(led) // ID del LED dentro del fixture (0-6)
                            .name(randomColorName)
                            .r(color.getR())
                            .g(color.getG())
                            .b(color.getB())
                            .w(color.getW())
                            .build();
                    newLedPoints.add(lp);
                    
                    if (log.isDebugEnabled()) {
                        log.debug("Generated random color for fixture {} LED {}: {} (R:{}, G:{}, B:{}, W:{})",
                                fixture.getId(), led, randomColorName,
                                color.getR(), color.getG(), color.getB(), color.getW());
                    }
                }
            }
        }
        
        // Actualizar los LedPoints de la escena
        scene.setLedPoints(newLedPoints);
        log.info("Generated {} random colors for {} fixtures (recalculated on scene start)", newLedPoints.size(), fixtures.size());
    }

    private static Set<QLCFixture> collectFixtures(QLCScene scene) {
        final Set<QLCFixture> fixtures = new HashSet<>();
        for (QLCPoint p : scene.getQlcPointList()) {
            if (p.getFixture()!=null) fixtures.add(p.getFixture());
        }
        return fixtures;
    }

    private static int clamp(int v) {
        if (v < 0) return 0;
        return Math.min(v, 255);
    }
}
