package cl.clillo.lighting.model;

import cl.clillo.lighting.fixture.qlc.QLCFixture;
import cl.clillo.lighting.repository.XMLParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.List;

public class QLCElement {

    protected final int id;
    protected final String type;
    protected final String name;
    protected final String path;

    public QLCElement(int id, String type, String name, String path) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.path = path;
    }

    public static QLCElement read(final Document doc) {
        Node common = doc.getElementsByTagName("common").item(0);

        return new QLCElement(XMLParser.getNodeInt(common, "id"), XMLParser.getNodeString(common, "type"),
                XMLParser.getNodeString(common, "name"), XMLParser.getNodeString(common, "path"));
    }

    protected void writeFixtures(final XMLStreamWriter out, final List<QLCFixture> fixtureList) throws XMLStreamException {
        out.writeStartElement("fixtures");
        for (QLCFixture data: fixtureList) {
            out.writeStartElement("fixture");
                out.writeStartElement("id");
                out.writeCharacters(String.valueOf(data.getId()));
                out.writeEndElement();
            out.writeEndElement();
        }
        out.writeEndElement();

    }

    public int getId() {
        return this.id;
    }

    public String getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    public String getPath() {
        return this.path;
    }
}