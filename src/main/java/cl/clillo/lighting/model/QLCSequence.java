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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ToString
@Getter
public class QLCSequence extends QLCFunction{

    private final QLCDirection direction;
    private final QLCRunOrder runOrder;
    private final List<QLCStep> qlcStepList;
    private final QLCScene boundScene;
    private final QLCSpeed qlcSpeed;
    private final Set<Integer> dimmerChannelSet;

    public QLCSequence(final int id, final String type, final String name, final String path,
                       final QLCDirection direction, final QLCRunOrder runOrder, final List<QLCStep> qlcStepList,
                       final QLCScene boundScene, final QLCSpeed qlcSpeed) {
        super(id, type, name, path);

        this.direction = direction;
        this.runOrder = runOrder;
        this.boundScene = boundScene;
        this.qlcSpeed = qlcSpeed;

        dimmerChannelSet = new HashSet<>();

        if (boundScene != null)
            for (QLCPoint boundPoint : boundScene.getQlcPointList()) {
                final String operationalBoundId = boundPoint.getOperationalId();
                for (QLCStep step : qlcStepList) {
                    boolean found = false;
                    for (QLCPoint stepPoint : step.getPointList()) {
                        if (stepPoint.getOperationalId().equals(operationalBoundId))
                            found = true;
                    }
                    if (!found)
                        step.getPointList().add(boundPoint);

                }
            }

        final Set<QLCFixture> fixtures = new HashSet<>();

        for (QLCStep step : qlcStepList) {
            if (step.getFadeIn() == 0)
                step.setFadeIn(qlcSpeed.getFadeIn());
            if (step.getFadeOut() == 0)
                step.setFadeOut(qlcSpeed.getFadeOut());
            if (step.getHold() == 0)
                step.setHold(qlcSpeed.getDuration() - qlcSpeed.getFadeIn());
         //  for (QLCPoint point : step.getPointList())
           //     fixtures.add(point.getFixture());
        }
/*
        for (QLCFixture fixture : fixtures) {
            if (fixture != null && fixture.getDMXChannel(QLCFixture.ChannelType.DIMMER) >= 0)
                dimmerChannelSet.add(fixture.getDMXChannel(QLCFixture.ChannelType.DIMMER));
        }
*/
        this.qlcStepList = new ArrayList<>();

        for (QLCStep step : qlcStepList)
            buildFakeSteps(step);

     //   if (this.id==25)
       //     System.out.println("AQUI");
    }

    private static final int MIN_STEP_DURATION = 10; // in millis

    private void buildFakeSteps(final QLCStep step){
        if (step.getFadeIn() == 0 && step.getFadeOut() == 0)
            this.qlcStepList.add(step);

        if (step.getFadeIn()!=0){
            int numberOfFakeSteps = step.getFadeIn()/MIN_STEP_DURATION;
            //if (numberOfFakeSteps==0)
              //  System.out.println("AQUI");
            int deltaDimmer = 255 / numberOfFakeSteps;
            int valueDimmer = 0;

            for (int i=0; i<numberOfFakeSteps; i++) {
                this.qlcStepList.add(QLCStep.builder()
                        .id(this.qlcStepList.size()+1)
                        .fadeIn(0)
                        .fadeOut(0)
                        .hold(MIN_STEP_DURATION)
                        .pointList(i==0?step.replaceDimmerValue(valueDimmer):step.onlyDimmerValue(valueDimmer))
                        .buildFake());
                valueDimmer+=deltaDimmer;
            }
        }

        this.qlcStepList.add(step);

        if (step.getFadeOut()!=0){
            int numberOfFakeSteps = step.getFadeOut()/MIN_STEP_DURATION;
            int deltaDimmer = 255 / numberOfFakeSteps;
            int valueDimmer = 255;

            for (int i=0; i<numberOfFakeSteps; i++) {
                this.qlcStepList.add(QLCStep.builder()
                        .id(this.qlcStepList.size()+1)
                        .fadeIn(0)
                        .fadeOut(0)
                        .hold(MIN_STEP_DURATION)
                        .pointList(i==0?step.replaceDimmerValue(valueDimmer):step.onlyDimmerValue(valueDimmer))
                        .buildFake());
                valueDimmer-=deltaDimmer;
            }
        }
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
            if (step instanceof QLCFakeStep)
                continue;
            out.writeStartElement("step");
            out.writeAttribute("id", String.valueOf(step.getId()));
            out.writeAttribute("fadeIn", String.valueOf(step.getFadeIn()));
            out.writeAttribute("hold", String.valueOf(step.getHold()));
            out.writeAttribute("fadeOut", String.valueOf(step.getFadeOut()));

            QLCPoint.write(out, step.getPointList());

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

        final Node common = doc.getElementsByTagName("steps").item(0);
        final NodeList list = common.getChildNodes();

        for (int temp = 0; temp < list.getLength(); temp++) {
            Node node = list.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                final QLCStep step = buildStep(fixtureListBuilder, node);
                if (step!=null) {
                    qlcStepList.add(step);
                }
            }
        }

        return new QLCSequence(function.getId(), function.getType(), function.getName(),
                function.getPath(), direction, runOrder, qlcStepList, null, qlcSpeed);
    }

    protected static QLCStep buildStep(final FixtureListBuilder fixtureListBuilder, final Node node){
        final List<QLCPoint> points = new ArrayList<>();
        QLCStep.QLCStepBuilder qlcStep = QLCStep.builder()
                .fadeIn(XMLParser.getIntAttributeValue(node, "fadeIn"))
                .hold(XMLParser.getIntAttributeValue(node, "hold"))
                .fadeOut(XMLParser.getIntAttributeValue(node, "fadeOut"))
                .id(XMLParser.getIntAttributeValue(node, "id"))
                .pointList(points);

        for(Node pointXML: XMLParser.getNodeList(node, "points")){
            final QLCPoint point = QLCPoint.build(fixtureListBuilder, pointXML);
            if (point!=null)
                points.add(point);

        }

        return qlcStep.build();
    }

    protected int[] getDimmerChannels(){
        final int [] dimmerChannels = new int[dimmerChannelSet.size()];
        int i=0;
        for (int channel: dimmerChannelSet)
            dimmerChannels[i++]= channel;

        return dimmerChannels;
    }
}
