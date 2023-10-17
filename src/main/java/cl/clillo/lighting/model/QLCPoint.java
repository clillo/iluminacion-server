package cl.clillo.lighting.model;

import cl.clillo.lighting.fixture.qlc.QLCFixture;
import cl.clillo.lighting.fixture.qlc.QLCRoboticFixture;
import lombok.Getter;

@Getter
public class QLCPoint {

    private final QLCFixture fixture;
    private final int channel;
    private final int dmxChannel;
    private final int data;
    private final QLCFixture.ChannelType channelType;

    QLCPoint(QLCFixture fixture, int channel, int dmxChannel, int data, QLCFixture.ChannelType channelType) {
        this.fixture = fixture;
        this.channel = channel;
        this.dmxChannel = dmxChannel;
        this.data = data;
        this.channelType = channelType;
    }

    public static QLCPointBuilder builder() {
        return new QLCPointBuilder();
    }

    public String getOperationalId() {
        return fixture.getId() + "." + channel;
    }

    public String toString() {
        return "QLCPoint(fixture=" + this.getFixture().getId() + ",  dmxChannel=" + this.getDmxChannel() + ", data=" + this.getData() + ")";
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
        if (fixture == null)
            System.exit(0);
        int channel = fixture.getChannel(channelType);
        int dmxChannel = fixture.getDMXChannel(channelType);
        return new QLCPoint(fixture, channel, dmxChannel, data, channelType);
    }

    public static QLCPoint buildRawPoint(final QLCFixture fixture, final int channel, final int data) {
        if (fixture == null)
            System.exit(0);
        int dmxChannel = fixture.getDMXChannel(channel);
        return new QLCPoint(fixture, channel, dmxChannel, data, QLCFixture.ChannelType.RAW);
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

        public String toString() {
            return "QLCPoint.QLCPointBuilder(fixture=" + this.fixture + ", channel=" + this.channel + ", dmxChannel=" + this.dmxChannel + ", data=" + this.data + ", channelType=" + this.channelType + ")";
        }
    }
}
