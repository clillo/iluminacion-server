package cl.clillo.lighting.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.ToString;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ToString
public class QLCCollection extends QLCFunction {

    private final List<Show> showList;

    public QLCCollection(final int id, final String type, final String name, final String path, final List<Show> showList) {
        super(id, type, name, path);
        this.showList = showList;
    }

    public String toSmallString() {
        StringBuilder sb = new StringBuilder(super.toSmallString());

        for (Show scene : showList)
            sb.append('{').append(scene.getId()).append('}');
        return sb.toString();
    }

    public static QLCCollection readFromJson(final ShowCollection collection, final File file) throws IOException {
        final JsonNode root = ConfigJsonIO.readTree(file);
        final List<Show> shows = new ArrayList<>();
        final JsonNode showsNode = root.get("shows");
        if (showsNode != null && showsNode.isArray()) {
            for (JsonNode showNode : showsNode) {
                final Show show = collection.getShow(showNode.path("id").asInt(-1));
                if (show != null)
                    shows.add(show);
            }
        }
        return new QLCCollection(
                root.path("id").asInt(),
                ConfigJsonIO.text(root, "type", "Collection"),
                ConfigJsonIO.text(root, "name", ""),
                ConfigJsonIO.text(root, "path", ""),
                shows);
    }

    @Override
    protected void writeJson(final ObjectNode root) {
        super.writeJson(root);
        final ArrayNode shows = ConfigJsonIO.createArray();
        for (Show show : showList) {
            final ObjectNode showNode = ConfigJsonIO.createObject();
            showNode.put("id", show.getId());
            showNode.put("name", show.getName());
            shows.add(showNode);
        }
        root.set("shows", shows);
    }

    public List<Show> getShowList() {
        return showList;
    }

    public void addShow(final Show show){
        for (Show iShow: showList)
            if(iShow.getId()==show.getId())
                return;
        showList.add(show);
    }
}