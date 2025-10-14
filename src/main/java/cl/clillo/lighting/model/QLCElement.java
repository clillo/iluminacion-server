package cl.clillo.lighting.model;

import cl.clillo.lighting.fixture.qlc.QLCFixture;
import cl.clillo.lighting.repository.XMLParser;
import lombok.Getter;
import lombok.Setter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.List;

public class QLCElement {

    @Getter
    protected final int id;
    @Getter
    protected final String type;
    @Getter
    protected final String name;
    @Getter
    protected final String path;
    @Setter
    @Getter
    protected boolean blackout;
    @Setter
    @Getter
    private boolean totalBlackout;
    private boolean initEventTrigger;

    public QLCElement(int id, String type, String name, String path) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.path = path;
        this.initEventTrigger = false;
    }

    public static QLCElement read(final Document doc) {
        final Node common = doc.getElementsByTagName("common").item(0);

        final QLCElement qlcElement = new QLCElement(XMLParser.getNodeInt(common, "id"), XMLParser.getNodeString(common, "type"),
                XMLParser.getNodeString(common, "name"), XMLParser.getNodeString(common, "path"));
        qlcElement.blackout = XMLParser.getNodeBoolean(common, "blackout");
        qlcElement.totalBlackout = "TotalBlackout".equalsIgnoreCase(XMLParser.getNodeString(common, "system"));
        qlcElement.setInitEventTrigger("true".equalsIgnoreCase(XMLParser.getNodeString(common, "initTrigger")));

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

    protected int[] getDimmerChannels(){
        return new int[0];
    }

    public boolean isInitEventTrigger() {
        return initEventTrigger;
    }

    public void setInitEventTrigger(boolean initEventTrigger) {
        this.initEventTrigger = initEventTrigger;
    }
}
