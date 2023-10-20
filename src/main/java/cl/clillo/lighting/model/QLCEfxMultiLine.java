package cl.clillo.lighting.model;

import cl.clillo.lighting.config.FixtureListBuilder;
import cl.clillo.lighting.repository.XMLParser;
import cl.clillo.lighting.utils.MultiLineScrambler;
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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@ToString
@Getter
@Setter
public class QLCEfxMultiLine extends QLCEfx{

    private RealPoint leftUp;
    private RealPoint rightDown;
    private Random random = new Random();

    private List<RealPoint> realPoints;
    private int nodePos = 0;
    private int nodeDelta = 1;

    private final int loops;
    private final MultiLineScrambler.Type scramblerType;

    public QLCEfxMultiLine(final int id, final String scramblerType, final String name, final String path,
                           final QLCDirection direction, final QLCRunOrder runOrder, final List<QLCStep> qlcStepList,
                           final QLCScene boundScene, List<QLCEfxFixtureData> fixtureList, int loops, MultiLineScrambler.Type type1) {
        super(id, scramblerType, name, path, direction, runOrder,qlcStepList, boundScene,  fixtureList);
        this.loops = loops;
        this.scramblerType = type1;
    }

    public void updateParameters(final List<RealPoint> realPoints){
        this.realPoints = realPoints;
        setNodes(buildNodes());
    }

    public void updateParameters(final RealPoint leftUp, final RealPoint rightDown){
        this.leftUp = leftUp;
        this.rightDown = rightDown;
        this.realPoints = new ArrayList<>();

        final MultiLineScrambler multiLineScrambler = new MultiLineScrambler(leftUp, rightDown);
        this.realPoints = multiLineScrambler.buildRealPoints(loops, scramblerType);

        setNodes(buildNodes());
    }

    protected List<QLCEfxPosition> buildPositions(){
        if (realPoints==null || realPoints.size()==0)
            return List.of();

        final List<QLCEfxPosition> positions = new ArrayList<>();

        RealPoint p1 = realPoints.get(0);
        RealPoint initialPoint = p1;

        for(int i=1; i<realPoints.size(); i++) {
            positions.addAll(buildPositions(p1, realPoints.get(i), nodes));
            p1 = realPoints.get(i);
        }

        positions.addAll(buildPositions(p1, initialPoint, nodes));

        return positions;
    }

    private List<QLCEfxPosition> buildPositions(final RealPoint a, final RealPoint b, final List<QLCExecutionNode> nodes){
        final List<QLCEfxPosition> positions = new ArrayList<>();

        int index=0;
        for (double time=0; time<=1; time+=0.01)
            positions.add(QLCEfxPosition.builder()
                    .index(index++)
                    .x(a.getX() + (int) (time * (b.getX() - a.getX())))
                    .y(a.getY() + (int) (time * (b.getY() - a.getY())))
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
        writeElementsFixture(out, getFixtureList());

        out.writeStartElement("dimensions");

        out.writeStartElement("leftUp");
            out.writeStartElement("x");
            out.writeCharacters(String.valueOf(leftUp.getX()));
            out.writeEndElement();
            out.writeStartElement("y");
            out.writeCharacters(String.valueOf(leftUp.getY()));
            out.writeEndElement();
        out.writeEndElement();
        out.writeStartElement("rightDown");
            out.writeStartElement("x");
            out.writeCharacters(String.valueOf(rightDown.getX()));
            out.writeEndElement();
            out.writeStartElement("y");
            out.writeCharacters(String.valueOf(rightDown.getY()));
            out.writeEndElement();
        out.writeEndElement();

        out.writeEndElement();

    }

    public static QLCEfxMultiLine read(final FixtureListBuilder fixtureListBuilder, final File file) throws ParserConfigurationException, IOException, SAXException {
        final Document doc = XMLParser.getDocument(file);
        final QLCElement function = QLCElement.read(doc);

        final QLCEfxMultiLine qlcEfxMultiLine = new QLCEfxMultiLine(function.getId(), function.getType(),
                function.getName(),function.getPath(),null,null,null,null,
                new ArrayList<>(), XMLParser.getPathInt(doc, "/doc/config/loops"),
                MultiLineScrambler.Type.of( XMLParser.getPathString(doc, "/doc/config/type")));

        Node common = doc.getElementsByTagName("fixtures").item(0);
        NodeList list = common.getChildNodes();
        for (int temp = 0; temp < list.getLength(); temp++) {
            Node node = list.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                qlcEfxMultiLine.getFixtureList().add(buildFixtureData(fixtureListBuilder, node));
            }
        }

        qlcEfxMultiLine.updateParameters(
                RealPoint.builder()
                        .x(XMLParser.getPathDouble(doc, "/doc/dimensions/leftUp/x"))
                        .y(XMLParser.getPathDouble(doc, "/doc/dimensions/leftUp/y"))
                        .build(),
                RealPoint.builder()
                        .x(XMLParser.getPathDouble(doc, "/doc/dimensions/rightDown/x"))
                        .y(XMLParser.getPathDouble(doc, "/doc/dimensions/rightDown/y"))
                        .build());

        return qlcEfxMultiLine;
    }

}
