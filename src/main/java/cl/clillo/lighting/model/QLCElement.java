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
    protected boolean blackout;
    private boolean totalBlackout;

    public QLCElement(int id, String type, String name, String path) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.path = path;
    }

    public static QLCElement read(final Document doc) {
        final Node common = doc.getElementsByTagName("common").item(0);

        final QLCElement qlcElement = new QLCElement(XMLParser.getNodeInt(common, "id"), XMLParser.getNodeString(common, "type"),
                XMLParser.getNodeString(common, "name"), XMLParser.getNodeString(common, "path"));
        qlcElement.blackout = XMLParser.getNodeBoolean(common, "blackout");
        qlcElement.totalBlackout = "TotalBlackout".equalsIgnoreCase(XMLParser.getNodeString(common, "system"));

        return qlcElement;
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

    public boolean isBlackout() {
        return blackout;
    }

    public void setBlackout(boolean blackout) {
        this.blackout = blackout;
    }

    protected int[] getDimmerChannels(){
        return new int[0];
    }

    public boolean isTotalBlackout() {
        return totalBlackout;
    }

    public void setTotalBlackout(boolean totalBlackout) {
        this.totalBlackout = totalBlackout;
    }
}
