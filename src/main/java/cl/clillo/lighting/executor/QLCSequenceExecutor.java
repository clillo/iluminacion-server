package cl.clillo.lighting.executor;

import cl.clillo.lighting.config.FixtureListBuilder;
import cl.clillo.lighting.external.dmx.Dmx;
import cl.clillo.lighting.model.ColorsCatalog;
import cl.clillo.lighting.model.QLCPoint;
import cl.clillo.lighting.model.QLCSequence;
import cl.clillo.lighting.model.QLCStep;
import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.model.ShowCollection;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
public class QLCSequenceExecutor extends AbstractExecutor {

    private final QLCSequence sequence;
    private final Random random = new Random();
    private final Dmx dmx = Dmx.getInstance();

    public QLCSequenceExecutor(final Show show) {
        super(show, ((QLCSequence)show.getFunction()).getQlcStepList());
        sequence = show.getFunction();
    }

    @Override
    public void executeDefaultScheduler() {

        preExecuteDefaultScheduler();

        final QLCStep step = sequence.getQlcStepList().get(actualStep);

   //     log.info("executing {} sequence {}: id [{}] step id[{}] Points: {}", show.getName(), actualStep, sequence.getId(), step.getId(), step.getPointList());
        log.info("executing {} sequence {}: id [{}] step id[{}] Direction: {} Run Order: {}", show.getName(), actualStep, sequence.getId(), step.getId(), sequence.getDirection(), sequence.getRunOrder());

        // Regenerar puntos aleatorios si es necesario
        if (sequence.getUseRandomColors() != null && sequence.getUseRandomColors() && !step.getRandomLedFixtures().isEmpty()) {
            regenerateRandomPoints(step);
        }

        postExecuteDefaultScheduler(step);

        show.setNextExecutionTime(System.currentTimeMillis() + step.getFadeIn() + step.getHold() + step.getFadeOut());
    }

    private void regenerateRandomPoints(QLCStep step) {
        if (sequence.getRandomColorPool() == null || sequence.getRandomColorPool().isEmpty()) {
            return;
        }

        final Map<String, ColorsCatalog.ColorEntry> palette = ColorsCatalog.getAll();
        final FixtureListBuilder fixtureListBuilder = ShowCollection.getInstance().getQlcModel();

        for (Map.Entry<Integer, Integer> entry : step.getRandomLedFixtures().entrySet()) {
            int ledIndex = entry.getKey();
            int fixtureId = entry.getValue();

            // Seleccionar color aleatorio
            String randomColorName = sequence.getRandomColorPool().get(random.nextInt(sequence.getRandomColorPool().size()));
            ColorsCatalog.ColorEntry color = palette.get(randomColorName);
            
            if (color == null) {
                log.warn("Random color '{}' not found in palette; defaulting to off for LED {} fixture {}", randomColorName, ledIndex, fixtureId);
                // Crear colorEntry con valores 0 usando ObjectMapper
                try {
                    final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    final java.util.Map<String, Integer> map = new java.util.HashMap<>();
                    map.put("r", 0);
                    map.put("g", 0);
                    map.put("b", 0);
                    map.put("w", 0);
                    color = mapper.convertValue(map, ColorsCatalog.ColorEntry.class);
                } catch (Exception e) {
                    log.error("Error creating default color entry", e);
                    continue;
                }
            } else {
                log.debug("Selected random color '{}' for LED {} fixture {} in step {}", randomColorName, ledIndex, fixtureId, step.getId());
            }

            // Obtener fixture
            final cl.clillo.lighting.fixture.qlc.QLCFixture fixture = fixtureListBuilder.getFixture(fixtureId);
            if (fixture == null) {
                log.warn("Fixture {} not found for random LED {}", fixtureId, ledIndex);
                continue;
            }

            // Generar puntos RGBW para este LED
            int baseChannel = 21 + 4 * ledIndex;
            List<QLCPoint> randomPoints = List.of(
                    QLCPoint.buildRawPoint(fixture, baseChannel + 0, color.getR()),
                    QLCPoint.buildRawPoint(fixture, baseChannel + 1, color.getG()),
                    QLCPoint.buildRawPoint(fixture, baseChannel + 2, color.getB()),
                    QLCPoint.buildRawPoint(fixture, baseChannel + 3, color.getW())
            );

            // Enviar puntos aleatorios directamente
            for (QLCPoint point : randomPoints) {
                if (point != null) {
                    dmx.send(point);
                }
            }
        }
    }

}
