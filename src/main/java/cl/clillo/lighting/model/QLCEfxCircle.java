package cl.clillo.lighting.model;

import cl.clillo.lighting.config.FixtureListBuilder;
import cl.clillo.lighting.config.QLCReader;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ToString
@Getter
@Setter
public class QLCEfxCircle extends QLCEfx{

    private double centerX;
    private double centerY;
    private double width;
    private double height;
    private int nodePos = 0;

    public QLCEfxCircle(final int id, final String type, final String name, final String path,
                        final QLCDirection direction, final QLCRunOrder runOrder, final List<QLCStep> qlcStepList,
                        final QLCScene boundScene, final List<QLCEfxFixtureData> fixtureList) {
        super(id, type, name, path, direction, runOrder,qlcStepList, boundScene,  fixtureList);
    }

    public void updateParameters(final double centerX, final double centerY, final double width, final double height){
        this.centerX = centerX;
        this.centerY = centerY;
        this.width = width;
        this.height = height;

        setNodes(buildNodes());
    }

    protected List<QLCEfxPosition> buildPositions(){
        final List<QLCEfxPosition> positions = new ArrayList<>();

        for (int time=0; time<360; time+=1)
            positions.add(QLCEfxPosition.builder()
                            .index(time)
                            .x(centerX + (int) (Math.cos(Math.toRadians(time)) * width))
                            .y(centerY + (int) (Math.sin(Math.toRadians(time)) * height))
                    .build());

        return positions;

    }

    @Override
    public QLCExecutionNode nextNode() {
        nodePos++;
        if (nodePos>=nodes.size()){
            nodePos=0;
        }
        return nodes.get(nodePos);
    }

    protected void writeElements(final XMLStreamWriter out) throws XMLStreamException {
        super.writeElements(out);
        out.writeStartElement("fixtures");
        for (QLCEfxFixtureData data: getFixtureList()) {
            out.writeStartElement("fixture");

            out.writeStartElement("id");
            out.writeCharacters(String.valueOf(data.getFixture().getId()));
            out.writeEndElement();
            out.writeStartElement("offset");
            out.writeCharacters(String.valueOf(data.getStartOffset()));
            out.writeEndElement();
            out.writeStartElement("reverse");
            out.writeCharacters(String.valueOf(data.isReverse()));
            out.writeEndElement();
            out.writeEndElement();
        }
        out.writeEndElement();

        out.writeStartElement("dimensions");

        out.writeStartElement("centerX");
        out.writeCharacters(String.valueOf(centerX));
        out.writeEndElement();
        out.writeStartElement("centerY");
        out.writeCharacters(String.valueOf(centerY));
        out.writeEndElement();
        out.writeStartElement("width");
        out.writeCharacters(String.valueOf(width));
        out.writeEndElement();
        out.writeStartElement("height");
        out.writeCharacters(String.valueOf(height));
        out.writeEndElement();
        out.writeEndElement();

    }

    public static QLCEfxCircle read(final FixtureListBuilder fixtureListBuilder, final String file) throws ParserConfigurationException, IOException, SAXException {
        final Document doc = getDocument(file);
        final QLCFunction function = QLCFunction.read(doc);

        final QLCEfxCircle qlcEfxCircle = new QLCEfxCircle(function.getId(), function.getType(), function.getName(),function.getPath(),null,null,null,null, new ArrayList<>()); // 13 -circle

        Node common = doc.getElementsByTagName("fixtures").item(0);
        NodeList list = common.getChildNodes();
        for (int temp = 0; temp < list.getLength(); temp++) {
            Node node = list.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                qlcEfxCircle.getFixtureList().add(buildFixtureData(fixtureListBuilder, node));
            }
        }

        Node dimensions = doc.getElementsByTagName("dimensions").item(0);

        qlcEfxCircle.updateParameters(getNodeDouble(dimensions, "centerX"),
                getNodeDouble(dimensions, "centerY"),
                getNodeDouble(dimensions, "width"),
                getNodeDouble(dimensions, "height"));

        return qlcEfxCircle;
    }

    private static QLCEfxFixtureData buildFixtureData(final FixtureListBuilder fixtureListBuilder, final Node node){
       return QLCEfxFixtureData.builder().fixture(fixtureListBuilder
               .getFixture(getNodeInt(node, "id")))
               .startOffset(getNodeDouble(node, "offset"))
               .reverse(getNodeBoolean(node, "reverse"))
               .build();
    }
}
