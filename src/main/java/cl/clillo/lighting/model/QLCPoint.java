package cl.clillo.lighting.model;

import cl.clillo.lighting.config.FixtureListBuilder;
import cl.clillo.lighting.fixture.qlc.QLCFixture;
import cl.clillo.lighting.fixture.qlc.QLCRoboticFixture;
import cl.clillo.lighting.repository.XMLParser;
import lombok.Getter;
import org.w3c.dom.Node;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Collections;
import java.util.List;

@Getter
public class QLCPoint implements Comparable<QLCPoint>{

    private final QLCFixture fixture;
    private final int channel;
    private final int dmxChannel;
    private int data;
    private final QLCFixture.ChannelType channelType;

    QLCPoint(QLCFixture fixture, int channel, int dmxChannel, int data, QLCFixture.ChannelType channelType) {
        this.fixture = fixture;
        this.channel = channel;
        this.dmxChannel = dmxChannel;
        this.data = data;

  //      if (dmxChannel==241)
   //         System.out.println(122);

        if (channelType==null && fixture instanceof QLCRoboticFixture) {
            QLCRoboticFixture qlcRoboticFixture = (QLCRoboticFixture)fixture;
            channelType = qlcRoboticFixture.getChannelType(dmxChannel);
        }

        if (channelType==null && fixture!=null) {
            channelType = fixture.getChannel(channel);

        }
        this.channelType = channelType;
    }

    public void setData(int data) {
        this.data = data;
    }

    public static QLCPointBuilder builder() {
        return new QLCPointBuilder();
    }

    public String getOperationalId() {
        return fixture.getId() + "." + channel;
    }

    public String toString() {
        return "QLCPoint(fixture=" + (this.getFixture()!=null?this.getFixture().getId():-1) + ",  dmxChannel=" + this.getDmxChannel() + ", data=" + this.getData() + ")";
    }

    public QLCPoint replaceDimmerValue(final int value){
        if (fixture==null)
            return null;

        int dmxChannel = fixture.getDMXChannel(QLCFixture.ChannelType.DIMMER, QLCFixture.ChannelType.MASTER_DIMMER);

        if (dmxChannel==-1)
            return null;

        if (this.dmxChannel!=dmxChannel)
            return null;

        return QLCPoint.builder()
                .fixture(fixture)
                .dmxChannel(dmxChannel)
                .channel(channel)
                .data(value)
                .build();

    }

    public static QLCPoint buildRoboticPoint(final QLCRoboticFixture fixture, final QLCFixture.ChannelType channelType, final int data) {
        int channel = fixture.getChannel(channelType) - 1;
        int dmxChannel = fixture.getDMXChannel(channelType);
        return new QLCPoint(fixture, channel, dmxChannel, data, channelType);
    }

    public static QLCPoint buildRawPoint(final QLCFixture fixture, final int channel, final int data) {
        if (fixture == null) {
           return null;
        }
        int dmxChannel = fixture.getDMXChannel(channel);
    //    if (dmxChannel==399 || dmxChannel==409 || dmxChannel==419 || dmxChannel==429)
      //      return null;

        return new QLCPoint(fixture, channel, dmxChannel, data, QLCFixture.ChannelType.RAW);
    }

    public void write(final XMLStreamWriter out) throws XMLStreamException {
        out.writeStartElement("point");

        if (fixture instanceof QLCRoboticFixture)
            out.writeAttribute("fixture-robotic", "true");

        out.writeAttribute("fixture", String.valueOf(fixture.getId()));

        if (channelType!=null && channelType!= QLCFixture.ChannelType.RAW)
            out.writeAttribute("type",String.valueOf(channelType));
        else
            out.writeAttribute("channel", String.valueOf(channel));

        out.writeAttribute("value", String.valueOf(data));

        out.writeEndElement();
    }

    public static void write(final XMLStreamWriter out, final List<QLCPoint> qlcPointList) throws XMLStreamException {
        out.writeStartElement("points");
        Collections.sort(qlcPointList);
        for (QLCPoint point: qlcPointList)
            point.write(out);

        out.writeEndElement();

    }

    public boolean isMovement(){
        return channelType == QLCFixture.ChannelType.PAN ||
                channelType == QLCFixture.ChannelType.PAN_FINE ||
                channelType == QLCFixture.ChannelType.TILT ||
                channelType == QLCFixture.ChannelType.TILT_FINE;
    }

    @Override
    public int compareTo(QLCPoint o) {
        if (fixture.getId()<o.getFixture().getId())
            return -1;

        if (fixture.getId()>o.getFixture().getId())
            return 1;

        return channel - o.channel;
    }

    protected static QLCPoint build(final int functionId, final FixtureListBuilder fixtureListBuilder, final Node node){
        final boolean isRobotic = XMLParser.getBoolean(node, "fixture-robotic");
        int fixtureId = XMLParser.getInt(node, "fixture");
        int value = XMLParser.getInt(node, "value");

        if (value==-1)
            value = XMLParser.getInt(node, "data");

        if (!isRobotic) {
            int channel = XMLParser.getInt(node, "channel");
            if (value==-1 && channel==-1)
                return null;
            return QLCPoint.buildRawPoint(fixtureListBuilder.getFixture(fixtureId), channel, value);
        }

        final QLCRoboticFixture qlcFixture = fixtureListBuilder.getFixture(fixtureId);
        if (qlcFixture==null){

            System.out.println("Robotic point without fixture: "+ fixtureId);
            return null;
            //System.exit(0);

        }

        final String nodeTypeStr = XMLParser.getStringAttributeValue(node, "type");
        final QLCFixture.ChannelType channelType = QLCFixture.ChannelType.of(nodeTypeStr);
        return QLCPoint.buildRoboticPoint(qlcFixture,
                channelType,
                value);
    }

    public static class QLCPointBuilder {
        private QLCFixture fixture;
        private int channel;
        private int dmxChannel;
        private int data;
        private QLCFixture.ChannelType channelType;

        QLCPointBuilder() {
        }

        public QLCPointBuilder fixture(QLCFixture fixture) {
            this.fixture = fixture;
            return this;
        }

        public QLCPointBuilder channel(int channel) {

            this.channel = channel;
            return this;
        }

        public QLCPointBuilder dmxChannel(int dmxChannel) {
            if (dmxChannel==-1)
                System.out.println("ERROR: dmxChannel null");
            this.dmxChannel = dmxChannel;
            return this;
        }

        public QLCPointBuilder data(int data) {
            if (data==-1)
                System.out.println("ERROR: data null");
            this.data = data;
            return this;
        }

        public QLCPointBuilder channelType(QLCFixture.ChannelType channelType) {
            this.channelType = channelType;
            return this;
        }

        public QLCPoint build() {
            return new QLCPoint(this.fixture, this.channel, this.dmxChannel, this.data, this.channelType);
        }
    }
}
