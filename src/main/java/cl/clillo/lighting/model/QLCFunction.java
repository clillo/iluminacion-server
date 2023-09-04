package cl.clillo.lighting.model;

import cl.clillo.lighting.config.FixtureListBuilder;
import cl.clillo.lighting.config.QLCReader;
import cl.clillo.lighting.fixture.qlc.QLCRoboticFixture;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.web.client.HttpServerErrorException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class QLCFunction {

    private int id;
    private String type;
    private String name;
    private String path;

    QLCFunction(int id, String type, String name, String path) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.path = path;
    }

    public String toSmallString(){
        return String.valueOf(this.getId()) + '\t' +
                this.getPath() + '.' + this.getName() + '\t';
    }

    public static QLCFunctionBuilder builder() {
        return new QLCFunctionBuilder();
    }

    protected void writeElements(final XMLStreamWriter out) throws XMLStreamException {
        out.writeStartElement("common");
        out.writeStartElement("id");
        out.writeCharacters(String.valueOf(id));
        out.writeEndElement();
        out.writeStartElement("type");
        out.writeCharacters(String.valueOf(type));
        out.writeEndElement();
        out.writeStartElement("path");
        out.writeCharacters(String.valueOf(path));
        out.writeEndElement();
        out.writeStartElement("name");
        out.writeCharacters(name);
        out.writeEndElement();
        out.writeEndElement();
    }

    protected void writeElementsFixture(final XMLStreamWriter out, final List<QLCEfxFixtureData> fixtureDataList) throws XMLStreamException {
        out.writeStartElement("fixtures");
        for (QLCEfxFixtureData data: fixtureDataList) {
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

    }

    protected static Document getDocument(final String file) throws ParserConfigurationException, IOException, SAXException {
        File inputFile = new File(QLCReader.repoBase + file);
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        return docBuilder.parse(inputFile);
    }

    public void writeToConfigFile(){
        OutputStream outputStream;
        try {
            final String name = this.getClass().getSimpleName() + "." +this.name;
            outputStream = new FileOutputStream(QLCReader.repoBase  + "/" + name + ".xml");
            XMLStreamWriter out = XMLOutputFactory.newInstance().createXMLStreamWriter(
                    new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));

            out.writeStartDocument();
            out.writeStartElement("doc");

            out.writeStartElement("title");
            out.writeCharacters(name);
            out.writeEndElement();

            writeElements(out);

            out.writeEndElement();
            out.writeEndDocument();

            out.close();
        } catch (IOException | XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    protected static String getPathString(final Document doc, final String path){
        XPath xPath = XPathFactory.newInstance().newXPath();
        Node node = null;
        try {
            node = (Node) xPath.compile(path).evaluate(doc, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }

        if (node != null) {
            return node.getTextContent();
        }

        return null;
    }

    protected static String getNodeString(final Node node, final String name){
        NodeList list = node.getChildNodes();
        for (int temp = 0; temp < list.getLength(); temp++) {
            Node data = list.item(temp);
            if (data.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) data;
                if(eElement.getNodeName().equals(name))
                    return eElement.getTextContent();

            }
        }
        return null;
    }

    protected static int getNodeInt(final Node node, final String name){
        String value = getNodeString(node, name);
        return (value==null?-1: Integer.parseInt(value));
    }

    protected static int getPathInt(final Document doc, final String path){
        String value = getPathString(doc, path);
        return (value==null?-1: Integer.parseInt(value));
    }

    protected static double getNodeDouble(final Node node, final String name){
        String value = getNodeString(node, name);
        return (value==null?-1: Double.parseDouble(value));
    }

    protected static double getPathDouble(final Document doc, final String path){
        String value = getPathString(doc, path);
        return (value==null?-1: Double.parseDouble(value));
    }

    protected static boolean getPathBoolean(final Document doc, final String path){
        String value = getPathString(doc, path);
        return ("true".equalsIgnoreCase(value));
    }

    protected static boolean getNodeBoolean(final Node node, final String name){
        String value = getNodeString(node, name);
        return ("true".equalsIgnoreCase(value));
    }

    public static QLCFunction read(final Document doc){
        Node common = doc.getElementsByTagName("common").item(0);

        return new QLCFunction(getNodeInt(common, "id"), getNodeString(common, "type"),
                getNodeString(common ,"name"), null);
    }

    protected static QLCEfxFixtureData buildFixtureData(final FixtureListBuilder fixtureListBuilder, final Node node){
        return QLCEfxFixtureData.builder().fixture(fixtureListBuilder
                        .getFixture(getNodeInt(node, "id")))
                .startOffset(getNodeDouble(node, "offset"))
                .reverse(getNodeBoolean(node, "reverse"))
                .build();
    }

    public static class QLCFunctionBuilder {
        private int id;
        private String type;
        private String name;
        private String path;
        private QLCDirection direction;
        private QLCRunOrder runOrder;
        private QLCScene boundScene;
        private QLCAlgorithm algorithm;
        private final List<QLCPoint> qlcPointList = new ArrayList<>();
        private final List<QLCFunction> qlcFunctionList = new ArrayList<>();
        private final List<QLCStep> qlcStepList = new ArrayList<>();
        private final List<QLCEfxFixtureData> roboticFixtureList = new ArrayList<>();

        QLCFunctionBuilder() {
        }

        public QLCFunctionBuilder id(final int id) {
            this.id = id;
            return this;
        }

        public QLCFunctionBuilder type(final String type) {
            this.type = type;
            return this;
        }

        public QLCFunctionBuilder name(final String name) {
            this.name = name;
            return this;
        }

        public QLCFunctionBuilder path(final String path) {
            this.path = path;
            return this;
        }

        public QLCFunctionBuilder boundScene(final QLCScene boundScene) {
            this.boundScene = boundScene;
            return this;
        }

        public void direction(final QLCDirection direction) {
            this.direction = direction;
        }

        public void algorithm(final QLCAlgorithm algorithm) {
            this.algorithm = algorithm;
        }

        public void runOrder(final QLCRunOrder runOrder){
            this.runOrder = runOrder;
        }

        public void addFunctionList(final QLCFunction function) {
            this.qlcFunctionList.add(function);
        }

        public void addPointList(final QLCPoint fixture) {
            this.qlcPointList.add(fixture);
        }

        public QLCFunctionBuilder addStepList(final QLCStep step) {
            this.qlcStepList.add(step);
            return this;
        }

        public QLCFunctionBuilder addFixture(final QLCRoboticFixture fixture) {
            this.roboticFixtureList.add(QLCEfxFixtureData.builder().fixture(fixture).build());
            return this;
        }

        public QLCFunction build() {
            if ("Scene".equalsIgnoreCase(type))
                return new QLCScene(this.id, this.type, this.name, this.path, this.qlcPointList);

            if ("Collection".equalsIgnoreCase(type))
                return new QLCCollection(this.id, this.type, this.name, this.path, this.qlcFunctionList);

            if ("Sequence".equalsIgnoreCase(type))
                return new QLCSequence(this.id, this.type, this.name, this.path, this.direction, this.runOrder,
                        this.qlcStepList, boundScene);

            if ("EFX".equalsIgnoreCase(type)){

                if ( QLCAlgorithm.CIRCLE==algorithm)
                    return new QLCEfxCircle(this.id, this.type, this.name, this.path, this.direction, this.runOrder,
                            this.qlcStepList, boundScene, roboticFixtureList);

                if ( QLCAlgorithm.LINE==algorithm)
                    return new QLCEfxLine(this.id, this.type, this.name, this.path, this.direction, this.runOrder,
                            this.qlcStepList, boundScene, roboticFixtureList);
            }
            return new QLCFunction(this.id, this.type, this.name, this.path);
        }

    }
}
