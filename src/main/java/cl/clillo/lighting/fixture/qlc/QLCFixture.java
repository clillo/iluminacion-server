package cl.clillo.lighting.fixture.qlc;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Map;

@Getter
@Setter
@ToString
public class QLCFixture {

    public enum ChannelType {GLOBAL_DIMMER, DIMMER, STROBE, COLOR_WHEEL, GOBO_WHEEL, PRISM_ROTATION ;

        public static ChannelType of(String name){
            if ("dimmer".equalsIgnoreCase(name))
                return DIMMER;

            return null;
        }
    };

    private String manufacturer;
    private String model;
    private String mode;
    private int id;
    private String name;
    private int universe;
    private int address;
    private int channels;
    private QLCFixtureModel fixtureModel;

    QLCFixture(String manufacturer, String model, String mode, int id, String name, int universe, int address, int channels, QLCFixtureModel fixtureModel) {
        this.manufacturer = manufacturer;
        this.model = model;
        this.mode = mode;
        this.id = id;
        this.name = name;
        this.universe = universe;
        this.address = address;
        this.channels = channels;
        this.fixtureModel = fixtureModel;
    }

    public int getChannel(ChannelType channelType){
        for (int i=0; i<fixtureModel.getTypeChannels().length; i++)
            if (fixtureModel.getTypeChannels()[i]==channelType)
                return i+1;

        return -1;
    }

    public int getDMXChannel(ChannelType channelType){
        return getChannel(channelType) + address - 1;
    }

    public static QLCFixtureBuilder builder() {
        return new QLCFixtureBuilder();
    }


    public static class QLCFixtureBuilder {
        private String manufacturer;
        private String model;
        private String mode;
        private int id;
        private String name;
        private int universe;
        private int address;
        private int channels;
        private QLCFixtureModel fixtureModel;

        QLCFixtureBuilder() {
        }

        public QLCFixtureBuilder manufacturer(String manufacturer) {
            this.manufacturer = manufacturer;
            return this;
        }

        public QLCFixtureBuilder model(String model) {
            this.model = model;
            return this;
        }

        public QLCFixtureBuilder mode(String mode) {
            this.mode = mode;
            return this;
        }

        public QLCFixtureBuilder id(int id) {
            this.id = id;
            return this;
        }

        public QLCFixtureBuilder name(String name) {
            this.name = name;
            return this;
        }

        public QLCFixtureBuilder universe(int universe) {
            this.universe = universe;
            return this;
        }

        public QLCFixtureBuilder address(int address) {
            this.address = address;
            return this;
        }

        public QLCFixtureBuilder channels(int channels) {
            this.channels = channels;
            return this;
        }

        public QLCFixtureBuilder fixtureModel(QLCFixtureModel fixtureModel) {
            this.fixtureModel = fixtureModel;
            return this;
        }

        public QLCFixture build(final Map<String, QLCFixtureModel> fixtureModelMap) {
            final QLCFixtureModel qlcFixtureModel = fixtureModelMap.get(manufacturer+"."+model);
            fixtureModel(qlcFixtureModel);

            final QLCFixture qlcFixture;

            if (qlcFixtureModel!=null && qlcFixtureModel.isRobotic())
                qlcFixture = new QLCRoboticFixture(this.manufacturer, this.model, this.mode, this.id, this.name, this.universe, this.address, this.channels, this.fixtureModel);
            else
                qlcFixture = new QLCFixture(this.manufacturer, this.model, this.mode, this.id, this.name, this.universe, this.address, this.channels, this.fixtureModel);

            return qlcFixture;
        }

        public QLCFixture build(final Node fixtureNode, final Map<String, QLCFixtureModel> fixtureModelMap) {
            final NodeList list = fixtureNode.getChildNodes();
            for (int temp = 0; temp < list.getLength(); temp++) {
                Node node = list.item(temp);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) node;
                    if ("Manufacturer".equals(eElement.getNodeName()))
                        manufacturer(eElement.getTextContent());
                    if ("Name".equals(eElement.getNodeName()))
                        name(eElement.getTextContent());
                    if ("Model".equals(eElement.getNodeName()))
                        model(eElement.getTextContent());
                    if ("Mode".equals(eElement.getNodeName()))
                        mode(eElement.getTextContent());
                    if ("ID".equals(eElement.getNodeName()))
                        id(Integer.parseInt(eElement.getTextContent()));
                    if ("Universe".equals(eElement.getNodeName()))
                        universe(Integer.parseInt(eElement.getTextContent()));
                    if ("Address".equals(eElement.getNodeName()))
                        address(Integer.parseInt(eElement.getTextContent()));
                    if ("Channels".equals(eElement.getNodeName()))
                        channels(Integer.parseInt(eElement.getTextContent()));
                }
            }

            return build(fixtureModelMap);
        }
    }
}
