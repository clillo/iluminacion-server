package cl.clillo.lighting.model;

import cl.clillo.lighting.config.FixtureListBuilder;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ToString
@Getter
@Slf4j
public class QLCScene extends QLCFunction{

    private final List<QLCPoint> qlcPointList;
    private final List<LedPoint> ledPoints = new ArrayList<>();
    @Getter
    private final QLCEfxScene qlcEfxScene;
    @Getter
    private final boolean initEventTrigger;
    @Getter
    private final String subType;

    public QLCScene(final int id, final String type, final String name, final String path, final List<QLCPoint> qlcPointList, boolean initEventTrigger) {
        this(id, type, name, path, qlcPointList, initEventTrigger, null);
    }

    public QLCScene(final int id, final String type, final String name, final String path, final List<QLCPoint> qlcPointList, boolean initEventTrigger, final String subType) {
        super(id, type, name, path);
        this.qlcPointList = qlcPointList;
        this.subType = subType;
        if (isEfx()) {
            final List<QLCEfxFixtureData> fixtureList = new ArrayList<>();
            for (QLCPoint qlcPoint: qlcPointList ){
                QLCEfxFixtureData fixtureData = QLCEfxFixtureData.builder().fixture(qlcPoint.getFixture())
                        .startOffset(0)
                        .reverse(false)
                        .build();

                boolean exist = false;
                for (QLCEfxFixtureData qlcEfxFixtureData: fixtureList)
                    if (qlcEfxFixtureData.getRoboticFixture().getId()==fixtureData.getRoboticFixture().getId()) {
                        exist = true;
                        break;
                    }

                if (!exist)
                    fixtureList.add(fixtureData);
            }
            qlcEfxScene = new QLCEfxScene(id, type, "scene: " + id, path, qlcPointList, fixtureList);

        }else
            qlcEfxScene = null;

        this.initEventTrigger = initEventTrigger;
    }

    public String toSmallString(){
        StringBuilder sb = new StringBuilder(super.toSmallString());

        for (QLCPoint point: qlcPointList)
            sb.append('{').append(point.getFixture().getId()).append(',')
                    .append(point.getChannel()).append(',')
                    .append(point.getData()).append('}');
        return sb.toString();
    }

    private boolean isEfx(){
        for(QLCPoint qlcPoint: qlcPointList)
            if (!qlcPoint.isMovement())
                return false;

        return true;
    }

    /**
     * Lee una escena desde JSON: formato channel-points (migrado desde XML)
     * o formato BeeEye (applyToFixtures + leds).
     */
    public static QLCScene readFromJson(final FixtureListBuilder fixtureListBuilder, final File file) throws IOException {
        final JsonNode root = ConfigJsonIO.readTree(file);
        if (!ConfigJsonIO.isBeeEyeEffect(root))
            return readFromChannelJson(fixtureListBuilder, root);

        final ObjectMapper mapper = ConfigJsonIO.mapper();
        final EffectJson effect = mapper.treeToValue(root, EffectJson.class);

        if (!"Scene".equalsIgnoreCase(effect.type)) {
            throw new IllegalArgumentException("JSON file is not a Scene (type=" + effect.type + ")");
        }

        final List<QLCPoint> qlcPointList = new ArrayList<>();
        final List<LedPoint> ledPointsList = new ArrayList<>();

        // Cargar catálogo de colores
        final Map<String, ColorsCatalog.ColorEntry> palette = ColorsCatalog.getAll();
        final int ledCount = effect.ledCount > 0 ? effect.ledCount : 6;
        final List<Integer> fixtures = effect.applyToFixtures != null ? effect.applyToFixtures : new ArrayList<>();

        // Usar el primer step si existe
        if (effect.steps != null && !effect.steps.isEmpty()) {
            final EffectStepJson step = effect.steps.get(0);
            final Map<Integer, ColorsCatalog.ColorEntry> perLed = new HashMap<>();

            // Procesar leds con colores por nombre o RGBW directo
            if (step.leds != null) {
                for (LedSpecJson ledSpec : step.leds) {
                    if (ledSpec == null) continue;
                    int idx = Math.max(0, ledSpec.led);
                    ColorsCatalog.ColorEntry color;
                    if (ledSpec.color != null && !ledSpec.color.isBlank()) {
                        color = palette.get(ledSpec.color);
                        if (color == null) {
                            log.warn("Color '{}' not found in palette; defaulting to off at LED {}", ledSpec.color, idx);
                            color = createColorEntry(0, 0, 0, 0);
                        }
                    } else {
                        color = createColorEntry(
                            safe(ledSpec.r),
                            safe(ledSpec.g),
                            safe(ledSpec.b),
                            safe(ledSpec.w)
                        );
                    }
                    if (color != null) {
                        perLed.put(idx, color);
                    }
                }
            }

            // Procesar ledPoints directos
            if (step.ledPoints != null) {
                for (LedPoint p : step.ledPoints) {
                    if (p == null) continue;
                    int idx = Math.max(0, p.getId());
                    ColorsCatalog.ColorEntry color = createColorEntry(
                        safe(p.getR()),
                        safe(p.getG()),
                        safe(p.getB()),
                        safe(p.getW())
                    );
                    if (color != null) {
                        perLed.put(idx, color);
                    }
                    ledPointsList.add(p);
                }
            }

            // Convertir a QLCPoint para cada fixture y LED
            for (Integer fixtureId : fixtures) {
                final cl.clillo.lighting.fixture.qlc.QLCFixture fixture = fixtureListBuilder.getFixture(fixtureId);
                if (fixture == null) {
                    log.warn("Fixture {} not found, skipping", fixtureId);
                    continue;
                }

                for (int led = 0; led < ledCount; led++) {
                    ColorsCatalog.ColorEntry c = perLed.getOrDefault(led, createColorEntry(0, 0, 0, 0));
                    if (c == null) {
                        c = createColorEntry(0, 0, 0, 0);
                    }
                    // Canal base para LED: 21 + 4 * led
                    // Canales relativos: base+0=R, base+1=G, base+2=B, base+3=W
                    int baseChannel = 21 + 4 * led;
                    qlcPointList.add(QLCPoint.buildRawPoint(fixture, baseChannel + 0, c.getR()));
                    qlcPointList.add(QLCPoint.buildRawPoint(fixture, baseChannel + 1, c.getG()));
                    qlcPointList.add(QLCPoint.buildRawPoint(fixture, baseChannel + 2, c.getB()));
                    qlcPointList.add(QLCPoint.buildRawPoint(fixture, baseChannel + 3, c.getW()));
                }
            }
        }

        Collections.sort(qlcPointList);
        final String path = effect.path != null && !effect.path.isBlank() ? effect.path : "Moving Head Bee Eye Color";
        final QLCScene scene = new QLCScene(effect.id, "Scene", effect.name, path, qlcPointList, false, null);
        scene.setLedPoints(ledPointsList);
        return scene;
    }

    private static int safe(Integer v) {
        return v == null ? 0 : Math.max(0, Math.min(255, v));
    }

    private static ColorsCatalog.ColorEntry createColorEntry(int r, int g, int b, int w) {
        // Usar Jackson para crear ColorEntry desde un Map
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final Map<String, Integer> map = new HashMap<>();
            map.put("r", r);
            map.put("g", g);
            map.put("b", b);
            map.put("w", w);
            return mapper.convertValue(map, ColorsCatalog.ColorEntry.class);
        } catch (Exception e) {
            log.error("Error creating ColorEntry", e);
            // Fallback: retornar null y manejar en el código que lo usa
            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class EffectJson {
        public int id;
        public String name;
        public String type = "Scene";
        public String path;
        public List<Integer> applyToFixtures;
        public int ledCount = 6;
        public List<EffectStepJson> steps = new ArrayList<>();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class EffectStepJson {
        public int index;
        public List<LedSpecJson> leds;
        public List<LedPoint> ledPoints;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class LedSpecJson {
        public int led;
        public String color;
        public Integer r;
        public Integer g;
        public Integer b;
        public Integer w;
    }

    private static QLCScene readFromChannelJson(final FixtureListBuilder fixtureListBuilder, final JsonNode root) {
        final List<QLCPoint> qlcPointList = ConfigJsonIO.buildPoints(fixtureListBuilder, root.get("points"));
        Collections.sort(qlcPointList);
        final String subType = ConfigJsonIO.text(root, "subType", null);
        final QLCScene scene = new QLCScene(
                root.path("id").asInt(),
                ConfigJsonIO.text(root, "type", "Scene"),
                ConfigJsonIO.text(root, "name", ""),
                ConfigJsonIO.text(root, "path", ""),
                qlcPointList,
                root.path("initTrigger").asBoolean(false),
                subType);
        scene.setBlackout(root.path("blackout").asBoolean(false));
        scene.setTotalBlackout(root.path("totalBlackout").asBoolean(false));
        return scene;
    }

    @Override
    protected void writeJson(final ObjectNode root) {
        super.writeJson(root);
        if (subType != null && !subType.isEmpty())
            root.put("subType", subType);
        final ArrayNode points = ConfigJsonIO.createArray();
        ConfigJsonIO.writePoints(points, qlcPointList);
        root.set("points", points);
    }

    /**
     * Reemplaza la lista de LedPoint en memoria (no se escribe en XML).
     */
    public void setLedPoints(final List<LedPoint> points){
        this.ledPoints.clear();
        if (points!=null)
            this.ledPoints.addAll(points);
    }

    /**
     * Agrega un LedPoint a la lista en memoria (no se escribe en XML).
     */
    public void addLedPoint(final LedPoint point){
        if (point!=null)
            this.ledPoints.add(point);
    }

    @Override
    public void setShow(Show show) {
        super.setShow(show);
        if (qlcEfxScene!=null)
            qlcEfxScene.setShow(getShow());
    }

    @Override
    public void setTotalBlackout(boolean totalBlackout) {
        super.setTotalBlackout(totalBlackout);
        if (!totalBlackout)
            return;

        this.qlcPointList.clear();
        this.qlcPointList.addAll(ShowCollection.getInstance().getBlackoutPointList());
    }

}
