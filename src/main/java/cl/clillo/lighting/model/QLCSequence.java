package cl.clillo.lighting.model;

import cl.clillo.lighting.config.FixtureListBuilder;
import cl.clillo.lighting.fixture.qlc.QLCFixture;
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
public class QLCSequence extends QLCFunction{

    private final QLCDirection direction;
    private final QLCRunOrder runOrder;
    private final List<QLCStep> qlcStepList;
    private final QLCScene boundScene;
    private final QLCSpeed qlcSpeed;
    private Show show;

    public QLCSequence(final int id, final String type, final String name, final String path,
                       final QLCDirection direction, final QLCRunOrder runOrder, final List<QLCStep> qlcStepList,
                       final QLCScene boundScene, final QLCSpeed qlcSpeed) {
        super(id, type, name, path);
        this.qlcStepList = qlcStepList;
        this.direction = direction;
        this.runOrder = runOrder;
        this.boundScene = boundScene;
        this.qlcSpeed = qlcSpeed;

        if (boundScene!=null)
            for (QLCPoint boundPoint: boundScene.getQlcPointList()){
                final String operationalBoundId = boundPoint.getOperationalId();
                for (QLCStep step: qlcStepList){
                    boolean found = false;
                    for (QLCPoint stepPoint: step.getPointList()) {
                          if (stepPoint.getOperationalId().equals(operationalBoundId))
                              found = true;
                    }
                    if (!found)
                        step.getPointList().add(boundPoint);

                }
            }
        for (QLCStep step: qlcStepList) {
            if (step.getFadeIn()==0)
                step.setFadeIn(qlcSpeed.getFadeIn());
            if (step.getFadeOut()==0)
                step.setFadeOut(qlcSpeed.getFadeOut());
            if (step.getHold()==0)
                step.setHold(qlcSpeed.getDuration() - qlcSpeed.getFadeIn());
        }

    }

    public void setShow(Show show) {
        this.show = show;
    }

    public String toSmallString(){
        StringBuilder sb = new StringBuilder(super.toSmallString());
        sb.append(direction).append('\t');
        sb.append(runOrder).append('\t');

        for (QLCStep step: qlcStepList) {
            sb.append('[').append('{')
                    .append(step.getFadeIn()).append(',')
                    .append(step.getHold()).append(',')
                    .append(step.getFadeOut()).append(',')
                    .append('}')
                    .append('{');

            for (QLCPoint point: step.getPointList())
                sb.append('{')
                        .append(point.getDmxChannel()).append(',')
                        .append(point.getData())
                        .append('}');

            sb.append('}').append(']');
        }

        return sb.toString();
    }

    protected void writeElements(final XMLStreamWriter out) throws XMLStreamException {
        super.writeElements(out);
        writeSteps(out);
    }



    protected void writeSteps(final XMLStreamWriter out) throws XMLStreamException {
        out.writeStartElement("steps");
        for (QLCStep step: qlcStepList) {
            out.writeStartElement("step");
            out.writeStartElement("common");
            out.writeStartElement("id");
            out.writeCharacters(String.valueOf(step.getId()));
            out.writeEndElement();
                out.writeStartElement("fadeIn");
                    out.writeCharacters(String.valueOf(step.getFadeIn()));
                out.writeEndElement();

                out.writeStartElement("hold");
                    out.writeCharacters(String.valueOf(step.getHold()));
                out.writeEndElement();

                out.writeStartElement("fadeOut");
                    out.writeCharacters(String.valueOf(step.getFadeOut()));
                out.writeEndElement();
            out.writeEndElement();

            out.writeStartElement("points");
            for (QLCPoint point: step.getPointList()){
                out.writeStartElement("point");
                out.writeStartElement("fixture");
                out.writeCharacters(String.valueOf(point.getFixture().getId()));
                out.writeEndElement();

                out.writeStartElement("channel");
                out.writeCharacters(String.valueOf(point.getDmxChannel()));
                out.writeEndElement();

                out.writeStartElement("data");
                out.writeCharacters(String.valueOf(point.getData()));
                out.writeEndElement();
                out.writeEndElement();
            }
            out.writeEndElement();
            out.writeEndElement();
        }
        out.writeEndElement();

    }

    public static QLCSequence read(final FixtureListBuilder fixtureListBuilder, final File file) throws ParserConfigurationException, IOException, SAXException {
        final Document doc = XMLParser.getDocument(file);
        final QLCElement function = QLCElement.read(doc);

        final List<QLCStep> qlcStepList = new ArrayList<>();

        final QLCDirection direction = QLCDirection.FORWARD;
        final QLCRunOrder runOrder = QLCRunOrder.LOOP;
        final QLCSpeed qlcSpeed = QLCSpeed.builder().build();
        final QLCSequence sequence = new QLCSequence(function.getId(), function.getType(), function.getName(),
                function.getPath(), direction, runOrder, qlcStepList, null, qlcSpeed);

        final Node common = doc.getElementsByTagName("steps").item(0);
        final NodeList list = common.getChildNodes();
        for (int temp = 0; temp < list.getLength(); temp++) {
            Node node = list.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                final QLCStep step = buildStep(fixtureListBuilder, node);
                if (step!=null)
                    qlcStepList.add(step);
            }
        }

        return sequence;
    }

    protected static QLCStep buildStep(final FixtureListBuilder fixtureListBuilder, final Node node){
        final Node common = XMLParser.getNode(node, "common");
        final List<QLCPoint> points = new ArrayList<>();
        QLCStep.QLCStepBuilder qlcStep = QLCStep.builder()
                .fadeIn(XMLParser.getNodeInt(common, "fadeIn"))
                .hold(XMLParser.getNodeInt(common, "hold"))
                .fadeOut(XMLParser.getNodeInt(common, "fadeOut"))
                .id(XMLParser.getNodeInt(common, "id"))
                .pointList(points);

        for(Node point: XMLParser.getNodeList(node, "points")){
            points.add(QLCPoint.builder()
                            .data(XMLParser.getNodeInt(point, "data"))
                            .dmxChannel(XMLParser.getNodeInt(point, "channel"))
                            .fixture(fixtureListBuilder.getFixture(XMLParser.getNodeInt(point, "fixture")))
                    .build());
        }

        return qlcStep.build();
    }
}
