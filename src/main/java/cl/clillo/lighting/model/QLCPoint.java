package cl.clillo.lighting.model;

import cl.clillo.lighting.fixture.qlc.QLCFixture;
import cl.clillo.lighting.fixture.qlc.QLCRoboticFixture;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QLCPoint {

    private QLCFixture fixture;
    private int channel;
    private int dmxChannel;
    private int data;
    private final QLCFixture.ChannelType channelType;

    public String getOperationalId(){
        return fixture.getId()+"."+channel;
    }

    public String toString() {
        return "QLCPoint(fixture=" + this.getFixture().getId() + ", channel=" + this.getChannel() + ", dmxChannel=" + this.getDmxChannel() + ", data=" + this.getData() + ")";
    }

    public static QLCPoint buildRoboticPoint(final QLCRoboticFixture fixture, final QLCFixture.ChannelType channelType, final int data){
        int channel = fixture.getChannel(channelType);
        int dmxChannel = fixture.getDMXChannel(channelType);
        return new QLCPoint(fixture, channel, dmxChannel, data, channelType);
    }

    public static QLCPoint buildRawPoint(final QLCFixture fixture, final int channel, final int data){
        int dmxChannel = fixture.getDMXChannel(channel);
        return new QLCPoint(fixture, channel, dmxChannel, data, QLCFixture.ChannelType.RAW);
    }
}
