package cl.clillo.lighting.model;

import cl.clillo.lighting.config.FixtureListBuilder;
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
import java.io.File;
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

    public static QLCScene read(final FixtureListBuilder fixtureListBuilder, final File file) throws ParserConfigurationException, IOException, SAXException {
        final Document doc = XMLParser.getDocument(file);
        final QLCElement function = QLCElement.read(doc);

        final List<QLCPoint> qlcPointList = new ArrayList<>();
        final QLCScene scene = new QLCScene(function.getId(), function.getType(), function.getName(),function.getPath(), qlcPointList);
        scene.setBlackout(function.isBlackout());

        Node common = doc.getElementsByTagName("points").item(0);
        NodeList list = common.getChildNodes();
        for (int temp = 0; temp < list.getLength(); temp++) {
            Node node = list.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                final QLCPoint point = QLCPoint.build(fixtureListBuilder, node);
                if (point!=null)
                    qlcPointList.add(point);
            }
        }

        return scene;
    }

    protected void writeElements(final XMLStreamWriter out) throws XMLStreamException {
        super.writeElements(out);
        QLCPoint.write(out, qlcPointList);
    }

}
