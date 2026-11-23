package cl.clillo.lighting.model;

import cl.clillo.lighting.repository.XMLParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.ToString;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
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

    protected void writeElements(final XMLStreamWriter out) throws XMLStreamException {
        super.writeElements(out);
        out.writeStartElement("shows");
        for (Show show : showList) {

            out.writeStartElement("show");
            out.writeAttribute("id", String.valueOf(show.getId()));
            out.writeAttribute("name", String.valueOf(show.getName()));
            out.writeEndElement();
        }
        out.writeEndElement();
    }

    public static QLCCollection read(final ShowCollection collection, final File file) throws ParserConfigurationException, IOException, SAXException {
        final Document doc = XMLParser.getDocument(file);
        final QLCElement function = QLCElement.read(doc);
        final List<Show> shows = new ArrayList<>();

        for(Node node: XMLParser.getNodeList(doc.getFirstChild(), "shows"))
            if (node.hasAttributes()) {
                final int showId = XMLParser.getIntAttributeValue(node, "id");
                final Show show = collection.getShow(showId);
                if (show!=null)
                    shows.add(show);
            }

        return new QLCCollection(function.getId(), function.getType(), function.getName(), function.getPath(), shows);
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