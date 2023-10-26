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
import java.util.Map;

@ToString
public class QLCChaser extends QLCFunction {

    private final List<QLCChaserStep> chaserSteps;
    private final QLCDirection direction;
    private final QLCRunOrder runOrder;
    private final QLCSpeed qlcSpeed;

    public QLCChaser(final int id, final String type, final String name, final String path, final QLCDirection direction,
                     final QLCRunOrder runOrder, final QLCSpeed qlcSpeed, final List<QLCChaserStep> chaserSteps) {
        super(id, type, name, path);
        this.direction = direction;
        this.runOrder = runOrder;
        this.qlcSpeed = qlcSpeed;
        this.chaserSteps = chaserSteps;
    }

    public String toSmallString() {
        StringBuilder sb = new StringBuilder(super.toSmallString());

        for (QLCChaserStep scene : chaserSteps)
            sb.append('{').append(scene.getId()).append('}');
        return sb.toString();
    }

    protected void writeElements(final XMLStreamWriter out) throws XMLStreamException {
        super.writeElements(out);
        out.writeStartElement("functions");
        for (QLCChaserStep step : chaserSteps) {

            out.writeStartElement("function");
            out.writeAttribute("id", String.valueOf(step.getCollection().getId()));
            out.writeAttribute("fadeIn", String.valueOf(step.getFadeIn()));
            out.writeAttribute("hold", String.valueOf(step.getHold()));
            out.writeAttribute("fadeOut", String.valueOf(step.getFadeOut()));
            out.writeEndElement();
        }
        out.writeEndElement();
    }

    public static QLCChaser read(final Map<Integer, QLCFunction> functionMap, final File file) throws ParserConfigurationException, IOException, SAXException {
        final Document doc = XMLParser.getDocument(file);
        final QLCElement function = QLCElement.read(doc);
        final List<QLCChaserStep> qlcFunctionList = new ArrayList<>();

        for(Node node: XMLParser.getNodeList(doc.getFirstChild(), "functions"))
            if (node.hasAttributes()) {
                final int fixtureId = XMLParser.getIntAttributeValue(node, "id");
                QLCFunction qlcFunction = functionMap.get(fixtureId);
                qlcFunctionList.add(QLCChaserStep.builder()
                                .fadeIn(XMLParser.getIntAttributeValue(node, "fadeIn"))
                                .hold(XMLParser.getIntAttributeValue(node, "hold"))
                                .fadeOut(XMLParser.getIntAttributeValue(node, "fadeOut"))
                        .collection(qlcFunction)
                        .build());
            }

        return new QLCChaser(5000+function.getId(), function.getType(), function.getName(), function.getPath(), null, null, null, qlcFunctionList);
    }
}