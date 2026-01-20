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

import java.util.HashSet;
import java.util.List;
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
        if (show.isFirstTimeExecution()){
            show.setFirstTimeExecution(false);
            sendNumber = 2;
        }

        if (sendNumber<=0)
            return;

        sendNumber--;
        final QLCScene scene = show.getFunction();
        log.info("Sending "+ show.getId()+ "\t" +scene.getType() + "\t"+scene.getName());

        if (scene.getLedPoints()!=null && !scene.getLedPoints().isEmpty()) {
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
