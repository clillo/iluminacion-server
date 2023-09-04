package cl.clillo.lighting.model;

import cl.clillo.lighting.config.FixtureListBuilder;
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
import java.util.Random;

@ToString
@Getter
@Setter
public class QLCEfxSpline extends QLCEfx{

    private RealPoint leftUp;
    private RealPoint rightDown;
    private Random random = new Random();

    private List<RealPoint> realPoints;
    private int nodePos = 0;
    private int nodeDelta = 1;

    public QLCEfxSpline(final int id, final String type, final String name, final String path,
                        final QLCDirection direction, final QLCRunOrder runOrder, final List<QLCStep> qlcStepList,
                        final QLCScene boundScene, List<QLCEfxFixtureData> fixtureList) {
        super(id, type, name, path, direction, runOrder,qlcStepList, boundScene,  fixtureList);

    }

    public void updateParameters(final List<RealPoint> realPoints){
        this.realPoints = realPoints;
        setNodes(buildNodes());
    }

    protected List<QLCEfxPosition> buildPositions(){
        if (realPoints==null || realPoints.size()==0)
            return List.of();

        final List<QLCEfxPosition> positions = new ArrayList<>();

        double[] x = new double[realPoints.size()+1];
        double[] y = new double[realPoints.size()+1];

        for(int i=0; i<realPoints.size(); i++) {
            x[i] = realPoints.get(i).getX();
            y[i] = realPoints.get(i).getY();
        }

        x[realPoints.size()] = realPoints.get(0).getX();
        y[realPoints.size()] = realPoints.get(0).getY();

        final Spline2D spline2D = new Spline2D(x, y);

        int index=0;
        for (double time=0; time<1; time+=0.01)
            positions.add(QLCEfxPosition.builder()
                    .index(index++)
                    .x(spline2D.getPoint(time)[0])
                    .y(spline2D.getPoint(time)[1])
                    .build());

        return positions;
    }

    @Override
    public QLCExecutionNode nextNode() {
        if (nodes.isEmpty())
            return null;
        nodePos++;
        if (nodePos>=nodes.size()){
            nodePos=0;
        }
        return nodes.get(nodePos);
    }

    protected void writeElements(final XMLStreamWriter out) throws XMLStreamException {
        super.writeElements(out);
        writeElementsFixture(out, getFixtureList());

        out.writeStartElement("realPoints");

        for (RealPoint data: realPoints) {
            out.writeStartElement("point");

            out.writeStartElement("x");
            out.writeCharacters(String.valueOf(data.getX()));
            out.writeEndElement();
            out.writeStartElement("y");
            out.writeCharacters(String.valueOf(data.getY()));
            out.writeEndElement();
            out.writeEndElement();
        }
        out.writeEndElement();

    }

    public static QLCEfxSpline read(final FixtureListBuilder fixtureListBuilder, final String file) throws ParserConfigurationException, IOException, SAXException {
        final Document doc = getDocument(file);
        final QLCFunction function = QLCFunction.read(doc);

        final QLCEfxSpline efxSpline = new QLCEfxSpline(function.getId(), function.getType(), function.getName(),function.getPath(),null,null,null,null, new ArrayList<>());

        Node common = doc.getElementsByTagName("fixtures").item(0);
        NodeList list = common.getChildNodes();
        for (int temp = 0; temp < list.getLength(); temp++) {
            Node node = list.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                efxSpline.getFixtureList().add(buildFixtureData(fixtureListBuilder, node));
            }
        }

        final Node realPointsNode = doc.getElementsByTagName("realPoints").item(0);

        final List<RealPoint> realPoints = new ArrayList<>();

        final NodeList listRealPoint = realPointsNode.getChildNodes();
        for (int temp = 0; temp < listRealPoint.getLength(); temp++) {
            Node node = listRealPoint.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                realPoints.add( RealPoint.builder()
                        .x(getNodeDouble(node, "x"))
                        .y(getNodeDouble(node, "y")).build()
                );
            }
        }

        efxSpline.updateParameters(realPoints);
        return efxSpline;
    }

}
