package cl.clillo.lighting.fixture.qlc;

import cl.clillo.lighting.model.QLCModel;
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
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
public class QLCFixtureModel {

    private String manufacturer;
    private String model;
    private String type;
    private String[] channels;
    private QLCFixture.ChannelType[] typeChannels;

    private boolean robotic;

    QLCFixtureModel(String manufacturer, String model, String type, String[] channels, boolean robotic) {
        this.manufacturer = manufacturer;
        this.model = model;
        this.type = type;
        this.channels = channels;
        this.robotic = robotic;

        typeChannels = new QLCFixture.ChannelType[channels.length];
        for (int i=0; i<channels.length; i++)
            typeChannels[i]=QLCFixture.ChannelType.of(channels[i]);

    }

    public static QLCFixtureModelBuilder builder() {
        return new QLCFixtureModelBuilder();
    }

    public static class QLCFixtureModelBuilder {
        private String manufacturer;
        private String model;
        private String type;
        private String[] channels;
        private boolean robotic;

        QLCFixtureModelBuilder() {
        }

        public QLCFixtureModelBuilder manufacturer(String manufacturer) {
            this.manufacturer = manufacturer;
            return this;
        }

        public QLCFixtureModelBuilder model(String model) {
            this.model = model;
            return this;
        }

        public QLCFixtureModelBuilder type(String type) {
            this.type = type;
            return this;
        }

        public QLCFixtureModelBuilder channels(String[] channels) {
            this.channels = channels;
            return this;
        }

        public QLCFixtureModelBuilder robotic(boolean robotic) {
            this.robotic = robotic;
            return this;
        }

        public QLCFixtureModel build(final File inputFile) throws IOException, SAXException, ParserConfigurationException {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(inputFile);

            Node engine = doc.getElementsByTagName("FixtureDefinition").item(0);

            NodeList list = engine.getChildNodes();

            for (int temp = 0; temp < list.getLength(); temp++) {
                Node node = list.item(temp);
                if (node.getNodeType() != Node.ELEMENT_NODE)
                    continue;

                Element eElement = (Element) node;
                if ("Manufacturer".equals(eElement.getNodeName()))
                    manufacturer(eElement.getTextContent());
                if ("Model".equals(eElement.getNodeName()))
                    model(eElement.getTextContent());
                if ("Type".equals(eElement.getNodeName())) {
                    String typeStr = eElement.getTextContent();
                    type(typeStr);
                    robotic(typeStr!=null && typeStr.toLowerCase().startsWith("moving"));
                }
                if ("Mode".equals(eElement.getNodeName())) {
                    channels(getFixtureMode(node));
                }
            }

            return build();
        }

        public QLCFixtureModel build() throws IOException, SAXException, ParserConfigurationException {
            return new QLCFixtureModel(this.manufacturer, this.model, this.type, this.channels, this.robotic);
        }

        private String [] getFixtureMode(final Node fixtureNode) {
            final NodeList list = fixtureNode.getChildNodes();
            final Map<Integer, String> channels = new HashMap<>();

            for (int temp = 0; temp < list.getLength(); temp++) {
                Node node = list.item(temp);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) node;

                    if ("Channel".equals(eElement.getNodeName()))
                        channels.put(QLCModel.getAttributeInt(node, "Number"), node.getTextContent());
                }
            }

            String [] output = new String[channels.size()];
            for (int i=0; i< channels.size(); i++)
                output[i] = channels.get(i);

            return output;
        }


    }
}
