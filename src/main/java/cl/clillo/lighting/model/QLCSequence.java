package cl.clillo.lighting.model;

import cl.clillo.lighting.config.FixtureListBuilder;
import cl.clillo.lighting.executor.IOS2LEventListener;
import cl.clillo.lighting.repository.XMLParser;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ToString
@Slf4j
public class QLCSequence extends QLCFunction implements Sequenceable{

    private static final int MIN_STEP_DURATION = 10; // in millis

    private QLCDirection direction;
    private QLCRunOrder runOrder;
    private final List<QLCStep> qlcStepList;
    private final List<QLCStep> qlcStepWithoutFade;
    private final QLCScene boundScene;
    private final QLCSpeed qlcSpeed;
    private final Set<Integer> dimmerChannelSet;
    private IOS2LEventListener.Type vdjType;
    @Getter
    private Boolean useRandomColors = false;
    @Getter
    private List<String> randomColorPool;
    // Representación opcional a nivel LED (no persistida en XML antiguo)
    private final List<LedPoint> ledPoints = new ArrayList<>();

    public QLCSequence(final int id, final String type, final String name, final String path,
                       final QLCDirection direction, final QLCRunOrder runOrder, final List<QLCStep> qlcStepList,
                       final QLCScene boundScene, final QLCSpeed qlcSpeed) {
        super(id, type, name, path);

        this.direction = direction;
        this.runOrder = runOrder;
        this.boundScene = boundScene;
        this.qlcSpeed = qlcSpeed;

        dimmerChannelSet = new HashSet<>();

        if (boundScene != null)
            for (QLCPoint boundPoint : boundScene.getQlcPointList()) {
                final String operationalBoundId = boundPoint.getOperationalId();
                for (QLCStep step : qlcStepList) {
                    boolean found = false;
                    for (QLCPoint stepPoint : step.getPointList()) {
                        if (stepPoint.getOperationalId().equals(operationalBoundId))
                            found = true;
                    }
                    if (!found)
                        step.getPointList().add(boundPoint);

                }
            }

        for (QLCStep step : qlcStepList) {
            if (step.getFadeIn() == 0)
                step.setFadeIn(qlcSpeed.getFadeIn());
            if (step.getFadeOut() == 0)
                step.setFadeOut(qlcSpeed.getFadeOut());
            if (step.getHold() == 0)
                step.setHold(qlcSpeed.getDuration() - qlcSpeed.getFadeIn());
        }

        this.qlcStepList = new ArrayList<>();
        this.qlcStepWithoutFade = new ArrayList<>();

        for (QLCStep step : qlcStepList) {
            buildFakeSteps(step);
            qlcStepWithoutFade.add(step);
        }
    }

    public void setVdjType(IOS2LEventListener.Type vdjType) {
        this.vdjType = vdjType;
    }

    @Override
    public void setDirection(QLCDirection direction) {
        this.direction = direction;
    }

    public void setRunOrder(QLCRunOrder runOrder) {
        this.runOrder = runOrder;
    }

    @Override
    public QLCDirection getDirection() {
        return direction;
    }

    public List<QLCStep> getQlcStepList() {
        return qlcStepList;
    }

    @Override
    public QLCRunOrder getRunOrder() {
        return runOrder;
    }

    private void buildFakeSteps(final QLCStep step){
        boolean includeOriginalStep = false;
        if (step.getFadeIn() == 0 && step.getFadeOut() == 0) {
            this.qlcStepList.add(step);
            includeOriginalStep = true;
        }

        if (step.getFadeIn()!=0){
            int numberOfFakeSteps = step.getFadeIn()/MIN_STEP_DURATION;
            int deltaDimmer = 255 / numberOfFakeSteps;
            int valueDimmer = 0;

            for (int i=0; i<numberOfFakeSteps; i++) {
                this.qlcStepList.add(QLCStep.builder()
                        .id(this.qlcStepList.size()+1)
                        .fadeIn(0)
                        .fadeOut(0)
                        .hold(MIN_STEP_DURATION)
                        .pointList(i==0?step.replaceDimmerValue(valueDimmer):step.onlyDimmerValue(valueDimmer))
                        .buildFake());
                valueDimmer+=deltaDimmer;
            }
        }

        if (!includeOriginalStep)
            this.qlcStepList.add(step);

        if (step.getFadeOut()!=0){
            int numberOfFakeSteps = step.getFadeOut()/MIN_STEP_DURATION;
            int deltaDimmer = 255 / numberOfFakeSteps;
            int valueDimmer = 255;

            for (int i=0; i<numberOfFakeSteps; i++) {
                this.qlcStepList.add(QLCStep.builder()
                        .id(this.qlcStepList.size()+1)
                        .fadeIn(0)
                        .fadeOut(0)
                        .hold(MIN_STEP_DURATION)
                        .pointList(i==0?step.replaceDimmerValue(valueDimmer):step.onlyDimmerValue(valueDimmer))
                        .buildFake());
                valueDimmer-=deltaDimmer;
            }
        }
    }

    public String toSmallString(){
        StringBuilder sb = new StringBuilder(super.toSmallString());
        sb.append(direction).append('\t');
        sb.append(runOrder).append('\t');

        for (QLCStep step: qlcStepList) {
            sb.append('[').append('{')
                    .append(step.getFadeIn()).append(',')
                    .append(step.getHold()).append(',')
                    .append(step.getFadeOut()).append(',')
                    .append('}')
                    .append('{');

            for (QLCPoint point: step.getPointList())
                sb.append('{')
                        .append(point.getDmxChannel()).append(',')
                        .append(point.getData())
                        .append('}');

            sb.append('}').append(']');
        }

        return sb.toString();
    }

    protected void writeElements(final XMLStreamWriter out) throws XMLStreamException {
        super.writeElements(out);
        out.writeStartElement("behaviour");
        out.writeAttribute("order", String.valueOf(runOrder));
        out.writeAttribute("direction", String.valueOf(direction));
        out.writeAttribute("vdjType", String.valueOf(vdjType));
        out.writeEndElement();
        writeSteps(out);
    }

    protected void writeSteps(final XMLStreamWriter out) throws XMLStreamException {
        out.writeStartElement("steps");
        for (QLCStep step: qlcStepList) {
            if (step instanceof QLCFakeStep)
                continue;
            out.writeStartElement("step");
            out.writeAttribute("id", String.valueOf(step.getId()));
            out.writeAttribute("fadeIn", String.valueOf(step.getFadeIn()));
            out.writeAttribute("hold", String.valueOf(step.getHold()));
            out.writeAttribute("fadeOut", String.valueOf(step.getFadeOut()));

            QLCPoint.write(out, step.getPointList());

            out.writeEndElement();
        }
        out.writeEndElement();

    }

    public static QLCSequence read(final FixtureListBuilder fixtureListBuilder, final File file) throws ParserConfigurationException, IOException, SAXException {
        final Document doc = XMLParser.getDocument(file);
        final QLCElement function = QLCElement.read(doc);

        final List<QLCStep> qlcStepList = new ArrayList<>();

        final Node behaviour = doc.getElementsByTagName("behaviour").item(0);

        QLCDirection direction = QLCDirection.FORWARD;
        QLCRunOrder runOrder = QLCRunOrder.LOOP;
        IOS2LEventListener.Type vdjType = IOS2LEventListener.Type.UNIVERSAL;
        if (behaviour!=null) {
            direction = QLCDirection.valueOf(XMLParser.getStringAttributeValue(behaviour, "direction"));
            runOrder = QLCRunOrder.valueOf(XMLParser.getStringAttributeValue(behaviour, "order"));
            if (XMLParser.getStringAttributeValue(behaviour, "vdjType")!=null)
                vdjType = IOS2LEventListener.Type.valueOf(XMLParser.getStringAttributeValue(behaviour, "vdjType"));
        }

        final QLCSpeed qlcSpeed = QLCSpeed.builder().build();

        final Node common = doc.getElementsByTagName("steps").item(0);
        final NodeList list = common.getChildNodes();

        for (int temp = 0; temp < list.getLength(); temp++) {
            Node node = list.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                final QLCStep step = buildStep(fixtureListBuilder, node);
                if (step!=null) {
                    qlcStepList.add(step);
                }
            }
        }

        final QLCSequence sequence = new QLCSequence(function.getId(), function.getType(), function.getName(),
                function.getPath(), direction, runOrder, qlcStepList, null, qlcSpeed);
        sequence.setVdjType(vdjType);
        return sequence;
    }

    /**
     * Lee una secuencia desde un archivo JSON (formato de efectos BeeEye).
     * El JSON debe tener: id, name, type="Sequence", path, order, direction, holdMs, applyToFixtures, ledCount, steps.
     * Cada step puede tener leds (con color o r/g/b/w) o ledPoints.
     */
    public static QLCSequence readFromJson(final FixtureListBuilder fixtureListBuilder, final File file) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final EffectJson effect = mapper.readValue(file, EffectJson.class);

        if (!"Sequence".equalsIgnoreCase(effect.type)) {
            throw new IllegalArgumentException("JSON file is not a Sequence (type=" + effect.type + ")");
        }

        final List<QLCStep> qlcStepList = new ArrayList<>();
        final Map<String, ColorsCatalog.ColorEntry> palette = ColorsCatalog.getAll();
        final int ledCount = effect.ledCount > 0 ? effect.ledCount : 6;
        final List<Integer> fixtures = effect.applyToFixtures != null ? effect.applyToFixtures : new ArrayList<>();

        // Procesar cada step
        if (effect.steps != null) {
            for (EffectStepJson stepJson : effect.steps) {
                final List<QLCPoint> stepPoints = new ArrayList<>();
                final Map<Integer, ColorsCatalog.ColorEntry> perLed = new HashMap<>();

                // Procesar leds con colores por nombre o RGBW directo
                if (stepJson.leds != null) {
                    for (LedSpecJson ledSpec : stepJson.leds) {
                        if (ledSpec == null) continue;
                        int idx = Math.max(0, ledSpec.led);
                        ColorsCatalog.ColorEntry color;
                        if (ledSpec.color != null && !ledSpec.color.isBlank()) {
                            // Si el color es "random", no procesar aquí (se manejará en el ejecutor)
                            if ("random".equalsIgnoreCase(ledSpec.color)) {
                                // No generar color aquí, se marcará para procesamiento en el ejecutor
                                continue;
                            }
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
                if (stepJson.ledPoints != null) {
                    for (LedPoint p : stepJson.ledPoints) {
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
                    }
                }

                // Convertir a QLCPoint para cada fixture y LED
                // Si activeFixture está definido, solo procesar ese fixture; si no, procesar todos
                List<Integer> fixturesToProcess = fixtures;
                if (stepJson.activeFixture != null && stepJson.activeFixture >= 0 && stepJson.activeFixture < fixtures.size()) {
                    // Solo procesar el fixture activo
                    fixturesToProcess = List.of(fixtures.get(stepJson.activeFixture));
                }

                QLCStep step = QLCStep.builder()
                        .id(stepJson.index)
                        .fadeIn(0)
                        .hold(effect.holdMs)
                        .fadeOut(0)
                        .pointList(new ArrayList<>())
                        .build();

                for (Integer fixtureId : fixturesToProcess) {
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
                        
                        // Verificar si este LED tiene color "random"
                        boolean isRandom = false;
                        if (stepJson.leds != null) {
                            for (LedSpecJson ledSpec : stepJson.leds) {
                                if (ledSpec != null && ledSpec.led == led && "random".equalsIgnoreCase(ledSpec.color)) {
                                    isRandom = true;
                                    // Almacenar información para regenerar en el ejecutor
                                    step.addRandomLed(led, fixtureId);
                                    break;
                                }
                            }
                        }
                        
                        // Si es random, no generar el punto ahora (se generará en el ejecutor)
                        if (!isRandom) {
                            // Canal base para LED: 21 + 4 * led
                            int baseChannel = 21 + 4 * led;
                            stepPoints.add(QLCPoint.buildRawPoint(fixture, baseChannel + 0, c.getR()));
                            stepPoints.add(QLCPoint.buildRawPoint(fixture, baseChannel + 1, c.getG()));
                            stepPoints.add(QLCPoint.buildRawPoint(fixture, baseChannel + 2, c.getB()));
                            stepPoints.add(QLCPoint.buildRawPoint(fixture, baseChannel + 3, c.getW()));
                        }
                    }
                }

                // Si activeFixture está definido, asegurar que los otros fixtures estén apagados
                if (stepJson.activeFixture != null && stepJson.activeFixture >= 0 && stepJson.activeFixture < fixtures.size()) {
                    for (int i = 0; i < fixtures.size(); i++) {
                        if (i != stepJson.activeFixture) {
                            Integer fixtureId = fixtures.get(i);
                            final cl.clillo.lighting.fixture.qlc.QLCFixture fixture = fixtureListBuilder.getFixture(fixtureId);
                            if (fixture != null) {
                                // Apagar todos los LEDs de este fixture
                                for (int led = 0; led < ledCount; led++) {
                                    int baseChannel = 21 + 4 * led;
                                    stepPoints.add(QLCPoint.buildRawPoint(fixture, baseChannel + 0, 0));
                                    stepPoints.add(QLCPoint.buildRawPoint(fixture, baseChannel + 1, 0));
                                    stepPoints.add(QLCPoint.buildRawPoint(fixture, baseChannel + 2, 0));
                                    stepPoints.add(QLCPoint.buildRawPoint(fixture, baseChannel + 3, 0));
                                }
                            }
                        }
                    }
                }

                Collections.sort(stepPoints);
                step.setPointList(stepPoints);
                qlcStepList.add(step);
            }
        }

        final QLCDirection direction = QLCDirection.valueOf(effect.direction != null ? effect.direction : "FORWARD");
        final QLCRunOrder runOrder = QLCRunOrder.valueOf(effect.order != null ? effect.order : "LOOP");
        final String path = effect.path != null && !effect.path.isBlank() ? effect.path : "Moving Head Bee Eye Sequence";
        final QLCSpeed qlcSpeed = QLCSpeed.builder().build();

        final QLCSequence sequence = new QLCSequence(effect.id, "Sequence", effect.name, path, direction, runOrder, qlcStepList, null, qlcSpeed);
        // Asignar información de aleatoriedad
        sequence.useRandomColors = effect.useRandomColors != null && effect.useRandomColors;
        if (sequence.useRandomColors) {
            if (effect.randomColorPool != null && !effect.randomColorPool.isEmpty()) {
                sequence.randomColorPool = effect.randomColorPool;
            } else {
                // Si no se especifica pool, usar todos los colores que terminan en ".full"
                sequence.randomColorPool = palette.keySet().stream()
                        .filter(name -> name.endsWith(".full"))
                        .collect(Collectors.toList());
            }
        }
        return sequence;
    }

    private static int safe(Integer v) {
        return v == null ? 0 : Math.max(0, Math.min(255, v));
    }

    private static ColorsCatalog.ColorEntry createColorEntry(int r, int g, int b, int w) {
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
            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class EffectJson {
        public int id;
        public String name;
        public String type = "Sequence";
        public String path;
        public String order = "LOOP";
        public String direction = "FORWARD";
        public int holdMs = 1000;
        public List<Integer> applyToFixtures;
        public int ledCount = 6;
        public Boolean useRandomColors = false;
        public List<String> randomColorPool;
        public List<EffectStepJson> steps = new ArrayList<>();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class EffectStepJson {
        public int index;
        public List<LedSpecJson> leds;
        public List<LedPoint> ledPoints;
        public Integer activeFixture; // Índice del fixture activo (0-based) en applyToFixtures
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

    protected static QLCStep buildStep(final FixtureListBuilder fixtureListBuilder, final Node node){
        final List<QLCPoint> points = new ArrayList<>();
        QLCStep.QLCStepBuilder qlcStep = QLCStep.builder()
                .fadeIn(XMLParser.getIntAttributeValue(node, "fadeIn"))
                .hold(XMLParser.getIntAttributeValue(node, "hold"))
                .fadeOut(XMLParser.getIntAttributeValue(node, "fadeOut"))
                .id(XMLParser.getIntAttributeValue(node, "id"))
                .pointList(points);

        for(Node pointXML: XMLParser.getNodeList(node, "points")){
            final QLCPoint point = QLCPoint.build(0, fixtureListBuilder, pointXML);
            if (point!=null)
                points.add(point);

        }

        return qlcStep.build();
    }

    protected int[] getDimmerChannels(){
        final int [] dimmerChannels = new int[dimmerChannelSet.size()];
        int i=0;
        for (int channel: dimmerChannelSet)
            dimmerChannels[i++]= channel;

        return dimmerChannels;
    }

    public List<QLCStep> getQlcStepWithoutFade() {
        return qlcStepWithoutFade;
    }

    @Override
    public int getSpeed() {
        return 1;
    }

    @Override
    public void setSpeed(int speed) {

    }

    public List<LedPoint> getLedPoints() {
        return ledPoints;
    }

    public void setLedPoints(final List<LedPoint> points){
        this.ledPoints.clear();
        if (points!=null)
            this.ledPoints.addAll(points);
    }

    public void addLedPoint(final LedPoint point){
        if (point!=null)
            this.ledPoints.add(point);
    }
}
