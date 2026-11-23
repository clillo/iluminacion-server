package cl.clillo.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;

public class SnapshotJsonWriter {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    static void write(File file, DmxModel model, SnapshotMeta meta,
                      int fixtureId, boolean fixtureRobotic, int startChannel, int dimmerChannelIndex) throws Exception {

        if (file == null) throw new IllegalArgumentException("file is null");
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("No se pudo crear el directorio: " + parent);
        }

        ObjectNode root = MAPPER.createObjectNode();
        root.put("id", Integer.parseInt(meta.id));
        root.put("type", meta.type);
        root.put("path", meta.path);
        root.put("name", meta.name);

        ArrayNode points = MAPPER.createArrayNode();
        for (int i = 0; i < model.size(); i++) {
            int value = model.get(i);
            ObjectNode point = MAPPER.createObjectNode();
            point.put("fixtureRobotic", fixtureRobotic);
            point.put("fixture", fixtureId);
            point.put("value", value);
            if (i == dimmerChannelIndex)
                point.put("channelType", "DIMMER");
            else
                point.put("channel", startChannel + i);
            points.add(point);
        }
        root.set("points", points);
        MAPPER.writeValue(file, root);
    }
}
