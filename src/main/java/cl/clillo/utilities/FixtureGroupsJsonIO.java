package cl.clillo.utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistencia JSON para grupos de fixtures con submaster.
 *
 * {"groups":[{"name":"Front","submaster":0.85,"channels":[1,2]}]}
 */
public class FixtureGroupsJsonIO {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public void write(File file, List<FixtureGroup> groups) throws Exception {
        ObjectNode root = MAPPER.createObjectNode();
        ArrayNode groupsNode = MAPPER.createArrayNode();
        for (FixtureGroup group : groups) {
            ObjectNode groupNode = MAPPER.createObjectNode();
            groupNode.put("name", group.name());
            groupNode.put("submaster", group.submaster());
            ArrayNode channels = MAPPER.createArrayNode();
            for (Integer channel : group.channels())
                channels.add(channel);
            groupNode.set("channels", channels);
            groupsNode.add(groupNode);
        }
        root.set("groups", groupsNode);
        MAPPER.writeValue(file, root);
    }

    public List<FixtureGroup> read(File file) throws Exception {
        JsonNode root = MAPPER.readTree(file);
        JsonNode groupsNode = root.get("groups");
        if (groupsNode == null || !groupsNode.isArray())
            throw new IllegalArgumentException("Documento sin groups");

        List<FixtureGroup> out = new ArrayList<>();
        for (JsonNode groupNode : groupsNode) {
            FixtureGroup group = new FixtureGroup(groupNode.path("name").asText(""));
            group.setSubmaster(groupNode.path("submaster").asDouble(1.0));
            JsonNode channels = groupNode.get("channels");
            if (channels != null && channels.isArray()) {
                for (JsonNode channel : channels)
                    group.addChannel(channel.asInt());
            }
            out.add(group);
        }
        return out;
    }
}
