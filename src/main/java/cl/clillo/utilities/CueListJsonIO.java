package cl.clillo.utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Lectura y escritura de CueList en JSON.
 *
 * {"name":"...","loop":true,"cues":[{"id":"...","name":"...","fadeMs":0,"holdMs":0,"channels":{"21":255}}]}
 */
public class CueListJsonIO {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public void write(File file, CueList list) throws Exception {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("name", list.name());
        root.put("loop", list.loop());
        ArrayNode cues = MAPPER.createArrayNode();
        for (Cue cue : list.cues()) {
            ObjectNode cueNode = MAPPER.createObjectNode();
            cueNode.put("id", cue.id());
            cueNode.put("name", cue.name());
            cueNode.put("fadeMs", cue.fadeMs());
            cueNode.put("holdMs", cue.holdMs());
            ObjectNode channels = MAPPER.createObjectNode();
            for (Map.Entry<Integer, Integer> entry : cue.channelValues().entrySet())
                channels.put(String.valueOf(entry.getKey()), entry.getValue());
            cueNode.set("channels", channels);
            cues.add(cueNode);
        }
        root.set("cues", cues);
        MAPPER.writeValue(file, root);
    }

    public CueList read(File file) throws Exception {
        JsonNode root = MAPPER.readTree(file);
        CueList list = new CueList(root.path("name").asText(""), root.path("loop").asBoolean(false));
        JsonNode cues = root.get("cues");
        if (cues == null || !cues.isArray())
            return list;

        for (JsonNode cueNode : cues) {
            Map<Integer, Integer> values = new HashMap<>();
            JsonNode channels = cueNode.get("channels");
            if (channels != null && channels.isObject()) {
                channels.fields().forEachRemaining(entry ->
                        values.put(Integer.parseInt(entry.getKey()), entry.getValue().asInt(0)));
            }
            list.addCue(new Cue(
                    cueNode.path("id").asText(""),
                    cueNode.path("name").asText(""),
                    values,
                    cueNode.path("fadeMs").asInt(0),
                    cueNode.path("holdMs").asInt(0)));
        }
        return list;
    }
}
