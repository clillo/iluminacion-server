package cl.clillo.lighting.model;

import cl.clillo.lighting.repository.XMLParser;
import lombok.ToString;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ToString
public class Chaser extends QLCFunction implements Sequenceable{

    private final List<ChaserStep> chaserSteps;
    private QLCDirection direction;
    private final QLCRunOrder runOrder;
    private int qlcSpeed;
    private Show blackoutShow;

    public Chaser(final int id, final String type, final String name, final String path, final QLCDirection direction,
                  final QLCRunOrder runOrder, final int qlcSpeed, final List<ChaserStep> chaserSteps) {
        super(id, type, name, path);
        this.direction = direction;
        this.runOrder = runOrder;
        this.qlcSpeed = qlcSpeed;
        this.chaserSteps = chaserSteps;
    }

    public String toSmallString() {
        StringBuilder sb = new StringBuilder(super.toSmallString());

        for (ChaserStep scene : chaserSteps)
            sb.append('{').append(scene.getId()).append('}');
        return sb.toString();
    }

    protected void writeElements(final XMLStreamWriter out) throws XMLStreamException {
        super.writeElements(out);
        out.writeStartElement("blackout");
        out.writeAttribute("id", String.valueOf(blackoutShow.getId()));
        out.writeEndElement();

        out.writeStartElement("properties");
            out.writeStartElement("runOrder");
            out.writeCharacters(String.valueOf(runOrder));
            out.writeEndElement();
            out.writeStartElement("direction");
            out.writeCharacters(String.valueOf(direction));
            out.writeEndElement();
            out.writeStartElement("speed");
            out.writeCharacters(String.valueOf(qlcSpeed));
            out.writeEndElement();
        out.writeEndElement();

        out.writeStartElement("shows");
        for (ChaserStep step : chaserSteps) {

            out.writeStartElement("show");
            if (step.getShow()!=null) {
                out.writeAttribute("id", String.valueOf(step.getShow().getId()));
                out.writeAttribute("fadeIn", String.valueOf(step.getFadeIn()));
                out.writeAttribute("hold", String.valueOf(step.getHold()));
                out.writeAttribute("fadeOut", String.valueOf(step.getFadeOut()));
            }
            out.writeEndElement();
        }
        out.writeEndElement();
    }

    public static Chaser read(final ShowCollection collection, final File file) throws ParserConfigurationException, IOException, SAXException {
        final Document doc = XMLParser.getDocument(file);
        final QLCElement function = QLCElement.read(doc);
        final List<ChaserStep> qlcFunctionList = new ArrayList<>();
        final Node blackout = XMLParser.getNode(doc.getFirstChild(), "blackout");
        Show blackoutShow = null;
        if (blackout!=null && blackout.hasAttributes()) {
            final int showId = XMLParser.getIntAttributeValue(blackout, "id");
            blackoutShow = collection.getShow(showId);
        }

        QLCDirection qlcDirection = QLCDirection.FORWARD;
        QLCRunOrder qlcRunOrder = QLCRunOrder.RANDOM;
        int qlcSpeed = 1;
        for(Node node: XMLParser.getNodeList(doc.getFirstChild(), "properties")){

            if ("direction".equalsIgnoreCase(node.getNodeName()))
                qlcDirection = QLCDirection.valueOf(node.getTextContent());

            if ("runOrder".equalsIgnoreCase(node.getNodeName()))
                qlcRunOrder = QLCRunOrder.valueOf(node.getTextContent());

            if ("speed".equalsIgnoreCase(node.getNodeName()))
                qlcSpeed = Integer.parseInt(node.getTextContent());

        }

        for(Node node: XMLParser.getNodeList(doc.getFirstChild(), "shows"))
            if (node.hasAttributes()) {
                final int showId = XMLParser.getIntAttributeValue(node, "id");
                final Show show = collection.getShow(showId);
                if (show==null){
                    System.out.println("Show no existe: " + showId);
                  //  System.exit(0);
                }

                qlcFunctionList.add(ChaserStep.builder()
                                .id(showId)
                                .fadeIn(XMLParser.getIntAttributeValue(node, "fadeIn"))
                                .hold(XMLParser.getIntAttributeValue(node, "hold"))
                                .fadeOut(XMLParser.getIntAttributeValue(node, "fadeOut"))
                        .show(show)
                        .build());
            }

        final Chaser chaser = new Chaser(function.getId(), function.getType(), function.getName(),
                function.getPath(), qlcDirection, qlcRunOrder, qlcSpeed, qlcFunctionList);
        chaser.setBlackoutShow(blackoutShow);
        return chaser;
    }

    public List<ChaserStep> getChaserSteps() {
        return chaserSteps;
    }

    @Override
    public QLCDirection getDirection() {
        return direction;
    }

    @Override
    public QLCRunOrder getRunOrder() {
        return runOrder;
    }

    @Override
    public void setDirection(QLCDirection direction) {
        this.direction = direction;
    }

    @Override
    public int getSpeed() {
        return qlcSpeed;
    }

    @Override
    public void setSpeed(int speed) {
        this.qlcSpeed = speed;
    }

    public void setBlackoutShow(Show blackoutShow) {
        this.blackoutShow = blackoutShow;
    }

    public Show getBlackoutShow() {
        return blackoutShow;
    }
}