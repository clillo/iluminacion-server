package cl.clillo.lighting.model;

import cl.clillo.lighting.config.FixtureListBuilder;
import cl.clillo.lighting.fixture.qlc.QLCFixture;
import cl.clillo.lighting.fixture.qlc.QLCRoboticFixture;
import cl.clillo.lighting.midi.RoboticNotifiable;
import cl.clillo.lighting.repository.XMLParser;
import lombok.Getter;
import lombok.ToString;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ToString
@Getter
public class QLCScene extends QLCFunction{

    private final List<QLCPoint> qlcPointList;

    public QLCScene(final int id, final String type, final String name, final String path, final List<QLCPoint> qlcPointList) {
        super(id, type, name, path);
        this.qlcPointList = qlcPointList;
    }

    public String toSmallString(){
        StringBuilder sb = new StringBuilder(super.toSmallString());

        for (QLCPoint point: qlcPointList)
            sb.append('{').append(point.getFixture().getId()).append(',')
                    .append(point.getChannel()).append(',')
                    .append(point.getData()).append('}');
        return sb.toString();
    }

    public static QLCScene build(final int id, final List<QLCPoint> qlcPointList){
        String type = "type";
        String path = "path";

        return new QLCScene(id, type, "scene: "+id, path, qlcPointList);
    }

    public static QLCScene read(final FixtureListBuilder fixtureListBuilder, final String file) throws ParserConfigurationException, IOException, SAXException {
        final Document doc = XMLParser.getDocument(file);
        final QLCElement function = QLCElement.read(doc);

        final List<QLCPoint> qlcPointList = new ArrayList<>();
        final QLCScene scene = new QLCScene(function.getId(), function.getType(), function.getName(),function.getPath(), qlcPointList);

        Node common = doc.getElementsByTagName("points").item(0);
        NodeList list = common.getChildNodes();
        for (int temp = 0; temp < list.getLength(); temp++) {
            Node node = list.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                qlcPointList.add(buildPoint(fixtureListBuilder, node));
            }
        }

        return scene;
    }

    protected static QLCPoint buildPoint(final FixtureListBuilder fixtureListBuilder, final Node node){
        final boolean isRobotic = XMLParser.getNodeBoolean(node, "fixture-robotic");
        if (!isRobotic) {
            System.out.println("Fixture must be robotic type");
            System.exit(0);
        }
        return QLCPoint.buildRoboticPoint(fixtureListBuilder.getFixture(
                XMLParser.getNodeInt(node, "fixture")),
                QLCFixture.ChannelType.of(XMLParser.getNodeString(node, "type")),
                XMLParser.getNodeInt(node, "value"));
    }

    protected void writePoints(final XMLStreamWriter out, final List<QLCPoint> points) throws XMLStreamException {
        out.writeStartElement("points");
        for (QLCPoint data: points) {
            out.writeStartElement("point");
                out.writeStartElement("type");
                out.writeCharacters(String.valueOf(data.getChannelType()));
                out.writeEndElement();
                out.writeStartElement("fixture-robotic");
                out.writeCharacters(String.valueOf(data.getFixture() instanceof QLCRoboticFixture));
                out.writeEndElement();
                out.writeStartElement("fixture");
                out.writeCharacters(String.valueOf(data.getFixture().getId()));
                out.writeEndElement();
                out.writeStartElement("value");
                out.writeCharacters(String.valueOf(data.getData()));
                out.writeEndElement();
            out.writeEndElement();
        }
        out.writeEndElement();

    }

    protected void writeElements(final XMLStreamWriter out) throws XMLStreamException {
        super.writeElements(out);
        writePoints(out, qlcPointList);
    }

}
