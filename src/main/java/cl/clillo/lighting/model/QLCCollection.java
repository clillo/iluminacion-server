package cl.clillo.lighting.model;

import cl.clillo.lighting.repository.XMLParser;
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

    public List<Show> getShowList() {
        return showList;
    }

    public void addShow(Show show){
        showList.add(show);

    }
}