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

    public enum ChannelType {MASTER_DIMMER, DIMMER, STROBE,
        COLOR_WHEEL, GOBO_WHEEL, PRISM_ROTATION, RAW, PAN, PAN_FINE, TILT, TILT_FINE, AUTO, GOBO_CRISTAL, GOBO_SHAKE,
        GOBO_INDEX, GOBO_NORMAL_SHAKE ;

        public static ChannelType of(String name){
            if ("color wheel".equalsIgnoreCase(name) || "color_wheel".equalsIgnoreCase(name))
                return COLOR_WHEEL;
            if ("pan".equalsIgnoreCase(name))
                return PAN;
            if ("tilt".equalsIgnoreCase(name))
                return TILT;
            if ("pan_fine".equalsIgnoreCase(name) || "pan fine".equalsIgnoreCase(name))
                return PAN_FINE;
            if ("tilt_fine".equalsIgnoreCase(name) || "tilt fine".equalsIgnoreCase(name))
                return TILT_FINE;
            if ("dimmer".equalsIgnoreCase(name))
                return DIMMER;
            if ("master dimmer".equalsIgnoreCase(name) || "master_dimmer".equalsIgnoreCase(name))
                return DIMMER;
            if ("strobe".equalsIgnoreCase(name))
                return STROBE;
            if ("gobo wheel".equalsIgnoreCase(name) || "gobo_wheel".equalsIgnoreCase(name))
                return ChannelType.GOBO_WHEEL;
            if ("gobo cristal".equalsIgnoreCase(name) || "gobo_cristal".equalsIgnoreCase(name))
                return ChannelType.GOBO_CRISTAL;
            if ("gobo shake".equalsIgnoreCase(name) || "gobo_shake".equalsIgnoreCase(name))
                return ChannelType.GOBO_SHAKE;
            if ("gobo normal shake".equalsIgnoreCase(name) || "gobo_normal_shake".equalsIgnoreCase(name))
                return ChannelType.GOBO_NORMAL_SHAKE;
            if ("gobo index".equalsIgnoreCase(name) || "gobo_index".equalsIgnoreCase(name))
                return ChannelType.GOBO_INDEX;
            if ("auto".equalsIgnoreCase(name))
                return ChannelType.AUTO;
            if ("prism rotation".equalsIgnoreCase(name) || "prism_rotation".equalsIgnoreCase(name))
                return ChannelType.PRISM_ROTATION;
            return RAW;
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

    public ChannelType getChannel(int channel){
        if (fixtureModel==null)
            return null;

        for (int i=0; i<fixtureModel.getChannels().length; i++){
            if (channel!=i)
                continue;
            String strChannel = fixtureModel.getChannels()[i];
            return ChannelType.of(strChannel);
        }

        return null;
    }

    public int getDMXDimmerChannel(){
        if (id==14 || id==15 || id==16 || id==17)
            return getDMXChannel(ChannelType.DIMMER) - 2;

        if (id==9 || id==12)
            return getDMXChannel(ChannelType.DIMMER) - 2;

        if (id==8 || id==10 || id==11 || id==18)
            return getDMXChannel(ChannelType.DIMMER) - 2;


        return getDMXChannel(ChannelType.DIMMER, ChannelType.MASTER_DIMMER) + 2;
    }


    public int getDMXChannel(ChannelType channelType){
        return getChannel(channelType) + address - 1;
    }

    public int getDMXChannel(ChannelType channelType, ChannelType channelType2){
        if (getChannel(channelType)>0)
            return getChannel(channelType) + address - 1;

        return getChannel(channelType2) + address - 1;
    }

    public int getDMXChannel(int channel){
        return channel + address;
    }

    public QLCFixtureModel getFixtureModel() {
        return fixtureModel;
    }

    public static QLCFixtureBuilder builder() {
        return new QLCFixtureBuilder();
    }

    public static QLCFixture build(final int id, final int dmxAddress, final QLCFixtureModel fixtureModel){
        String manufacturer = "manufacturer";
        String model = "model";
        String mode = "mode";
        int universe = 0;

        return new QLCFixture(manufacturer, model, mode, id, "fixture: "+id, universe, dmxAddress-1,
                fixtureModel.getChannels().length, fixtureModel);
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
