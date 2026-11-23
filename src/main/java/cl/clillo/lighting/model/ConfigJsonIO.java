package cl.clillo.lighting.model;

import cl.clillo.lighting.config.FixtureListBuilder;
import cl.clillo.lighting.fixture.qlc.QLCFixture;
import cl.clillo.lighting.fixture.qlc.QLCRoboticFixture;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public final class ConfigJsonIO {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private ConfigJsonIO() {
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    public static JsonNode readTree(final File file) throws IOException {
        return MAPPER.readTree(file);
    }

    public static void write(final File file, final ObjectNode root) throws IOException {
        file.getParentFile().mkdirs();
        MAPPER.writeValue(file, root);
    }

    public static ObjectNode createObject() {
        return MAPPER.createObjectNode();
    }

    public static ArrayNode createArray() {
        return MAPPER.createArrayNode();
    }

    public static void writeCommon(final ObjectNode root, final QLCElement element) {
        root.put("id", element.getId());
        root.put("type", element.getType());
        root.put("name", element.getName());
        root.put("path", element.getPath());
        if (element.isBlackout())
            root.put("blackout", true);
        if (element.isTotalBlackout())
            root.put("totalBlackout", true);
        if (element.isInitEventTrigger())
            root.put("initTrigger", true);
    }

    public static QLCPoint buildPoint(final FixtureListBuilder fixtures, final JsonNode node) {
        if (node == null || node.isNull())
            return null;

        final int fixtureId = node.path("fixture").asInt(-1);
        final int value = node.path("value").asInt(node.path("data").asInt(0));
        final boolean robotic = node.path("fixtureRobotic").asBoolean(false)
                || node.path("fixture-robotic").asBoolean(false);

        if (!robotic) {
            final int channel = node.path("channel").asInt(-1);
            if (channel < 0)
                return null;
            return QLCPoint.buildRawPoint(fixtures.getFixture(fixtureId), channel, value);
        }

        final QLCRoboticFixture fixture = fixtures.getFixture(fixtureId);
        if (fixture == null) {
            log.warn("Robotic point without fixture: {}", fixtureId);
            return null;
        }
        final String channelTypeName = text(node, "channelType", text(node, "type", null));
        final QLCFixture.ChannelType channelType = QLCFixture.ChannelType.of(channelTypeName);
        return QLCPoint.buildRoboticPoint(fixture, channelType, value);
    }

    public static List<QLCPoint> buildPoints(final FixtureListBuilder fixtures, final JsonNode pointsNode) {
        final List<QLCPoint> points = new ArrayList<>();
        if (pointsNode == null || !pointsNode.isArray())
            return points;
        for (JsonNode pointNode : pointsNode) {
            final QLCPoint point = buildPoint(fixtures, pointNode);
            if (point != null)
                points.add(point);
        }
        return points;
    }

    public static void writePoints(final ArrayNode array, final List<QLCPoint> points) {
        if (points == null)
            return;
        for (QLCPoint point : points)
            array.add(writePoint(point));
    }

    public static ObjectNode writePoint(final QLCPoint point) {
        final ObjectNode node = createObject();
        node.put("fixture", point.getFixture().getId());
        node.put("value", point.getData());
        if (point.getFixture() instanceof QLCRoboticFixture)
            node.put("fixtureRobotic", true);
        if (point.getChannelType() != null && point.getChannelType() != QLCFixture.ChannelType.RAW)
            node.put("channelType", point.getChannelType().name());
        else
            node.put("channel", point.getChannel());
        return node;
    }

    public static List<QLCEfxFixtureData> buildFixtures(final FixtureListBuilder fixtures, final JsonNode fixturesNode) {
        final List<QLCEfxFixtureData> list = new ArrayList<>();
        if (fixturesNode == null || !fixturesNode.isArray())
            return list;
        for (JsonNode fixtureNode : fixturesNode) {
            list.add(QLCEfxFixtureData.builder()
                    .fixture(fixtures.getFixture(fixtureNode.path("id").asInt(-1)))
                    .startOffset(fixtureNode.path("offset").asDouble(0))
                    .reverse(fixtureNode.path("reverse").asBoolean(false))
                    .build());
        }
        return list;
    }

    public static void writeFixtures(final ArrayNode array, final List<QLCEfxFixtureData> fixtures) {
        if (fixtures == null)
            return;
        for (QLCEfxFixtureData data : fixtures) {
            final ObjectNode node = createObject();
            final int id = data.getRoboticFixture() != null
                    ? data.getRoboticFixture().getId()
                    : data.getFixture().getId();
            node.put("id", id);
            node.put("offset", data.getStartOffset());
            node.put("reverse", data.isReverse());
            array.add(node);
        }
    }

    public static String text(final JsonNode node, final String field, final String defaultValue) {
        final JsonNode value = node.get(field);
        if (value == null || value.isNull())
            return defaultValue;
        final String text = value.asText();
        return text == null || text.isBlank() ? defaultValue : text;
    }

    public static boolean isBeeEyeEffect(final JsonNode root) {
        return root.has("applyToFixtures") || root.has("ledCount")
                || (root.has("steps") && root.path("steps").isArray()
                && root.path("steps").size() > 0
                && root.path("steps").get(0).has("leds"));
    }
}
