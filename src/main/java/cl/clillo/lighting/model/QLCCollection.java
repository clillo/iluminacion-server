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
public class QLCCollection extends QLCFunction {

    private final List<QLCFunction> qlcFunctionList;

    public QLCCollection(final int id, final String type, final String name, final String path, final List<QLCFunction> qlcFunctionList) {
        super(id, type, name, path);
        this.qlcFunctionList = qlcFunctionList;
    }

    public String toSmallString() {
        StringBuilder sb = new StringBuilder(super.toSmallString());

        for (QLCFunction scene : qlcFunctionList)
            sb.append('{').append(scene.getId()).append('}');
        return sb.toString();
    }

    protected void writeElements(final XMLStreamWriter out) throws XMLStreamException {
        super.writeElements(out);
        out.writeStartElement("functions");
        for (QLCFunction qlcFunction : qlcFunctionList) {

            out.writeStartElement("function");
            out.writeAttribute("id", String.valueOf(qlcFunction.getId()));
            out.writeAttribute("path", String.valueOf(qlcFunction.getPath()));
            out.writeAttribute("name", String.valueOf(qlcFunction.getName()));
            out.writeEndElement();
        }
        out.writeEndElement();
    }

    public static QLCCollection read(final Map<Integer, QLCFunction> functionMap, final File file) throws ParserConfigurationException, IOException, SAXException {
        final Document doc = XMLParser.getDocument(file);
        final QLCElement function = QLCElement.read(doc);
        final List<QLCFunction> qlcFunctionList = new ArrayList<>();

        for(Node node: XMLParser.getNodeList(doc.getFirstChild(), "functions"))
            if (node.hasAttributes()) {
                final int fixtureId = XMLParser.getIntAttributeValue(node, "id");
                QLCFunction qlcFunction = functionMap.get(fixtureId);
                if (qlcFunction!=null)
                    qlcFunctionList.add(qlcFunction);
            }

        return new QLCCollection(function.getId(), function.getType(), function.getName(), function.getPath(), qlcFunctionList);
    }

    public List<QLCFunction> getQlcFunctionList() {
        return qlcFunctionList;
    }

    public void addShow(Show show){
        qlcFunctionList.add(show.getFunction());

    }
}