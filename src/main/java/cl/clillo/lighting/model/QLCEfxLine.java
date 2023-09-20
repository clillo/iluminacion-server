package cl.clillo.lighting.model;

import cl.clillo.lighting.config.FixtureListBuilder;
import cl.clillo.lighting.repository.XMLParser;
import lombok.Getter;
import lombok.Setter;
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
@Setter
public class QLCEfxLine extends QLCEfx{

    private double originX;
    private double originY;
    private double destinyX;
    private double destinyY;

    private int nodePos = 0;
    private int nodeDelta = 1;

    public QLCEfxLine(final int id, final String type, final String name, final String path,
                      final QLCDirection direction, final QLCRunOrder runOrder, final List<QLCStep> qlcStepList,
                      final QLCScene boundScene, List<QLCEfxFixtureData> fixtureList) {
        super(id, type, name, path, direction, runOrder,qlcStepList, boundScene,  fixtureList);
        nodePos = 0;
    }

    public void updateParameters(final double originX, final double originY, final double destinyX, final double destinyY){
        this.originX = originX;
        this.originY = originY;
        this.destinyX = destinyX;
        this.destinyY = destinyY;

        setNodes(buildNodes());
    }

    protected List<QLCEfxPosition> buildPositions(){
        final List<QLCEfxPosition> positions = new ArrayList<>();

        int index=0;
        for (double time=0; time<=1; time+=0.01)
            positions.add(QLCEfxPosition.builder()
                    .index(index++)
                    .x(originX + (int) (time * (destinyX - originX)))
                    .y(originY + (int) (time * (destinyY - originY)))
                    .build());

        return positions;

    }

    @Override
    public QLCExecutionNode nextNode() {
        nodePos+=nodeDelta;
        if (nodePos<0){
            nodePos=1;
            nodeDelta=1;
        }
        if (nodePos>=nodes.size()){
            nodePos=nodes.size()-2;
            nodeDelta=-1;
        }
        return nodes.get(nodePos);
    }

    protected void writeElements(final XMLStreamWriter out) throws XMLStreamException {
        super.writeElements(out);
        writeElementsFixture(out, getFixtureList());

        out.writeStartElement("points");

        out.writeStartElement("origin");
            out.writeStartElement("x");
            out.writeCharacters(String.valueOf(originX));
            out.writeEndElement();
            out.writeStartElement("y");
            out.writeCharacters(String.valueOf(originY));
            out.writeEndElement();
        out.writeEndElement();
        out.writeStartElement("destiny");
            out.writeStartElement("x");
            out.writeCharacters(String.valueOf(destinyX));
            out.writeEndElement();
            out.writeStartElement("y");
            out.writeCharacters(String.valueOf(destinyY));
            out.writeEndElement();
        out.writeEndElement();

        out.writeEndElement();

    }

    public static QLCEfxLine read(final FixtureListBuilder fixtureListBuilder, final String file) throws ParserConfigurationException, IOException, SAXException {
        final Document doc = XMLParser.getDocument(file);
        final QLCElement function = QLCElement.read(doc);

        final QLCEfxLine qlcEfxMultiLine = new QLCEfxLine(function.getId(), function.getType(), function.getName(),function.getPath(),null,null,null,null, new ArrayList<>());

        Node common = doc.getElementsByTagName("fixtures").item(0);
        NodeList list = common.getChildNodes();
        for (int temp = 0; temp < list.getLength(); temp++) {
            Node node = list.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                qlcEfxMultiLine.getFixtureList().add(buildFixtureData(fixtureListBuilder, node));
            }
        }

        qlcEfxMultiLine.updateParameters(
                XMLParser.getPathDouble(doc, "/doc/points/origin/x"),
                XMLParser.getPathDouble(doc, "/doc/points/origin/y"),
                XMLParser.getPathDouble(doc, "/doc/points/destiny/x"),
                XMLParser.getPathDouble(doc, "/doc/points/destiny/y"));

        return qlcEfxMultiLine;
    }

}
