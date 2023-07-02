package cl.clillo.lighting.model;

import cl.clillo.lighting.dmx.Dmx;
import lombok.Builder;

@Builder
public class QLCExecutionNode {

    private final Dmx dmx = Dmx.getInstance();
    private final int[] channel;
    private final int[] data;
    private final long holdTime;

    public QLCExecutionNode(final int[] channel, final int[] data, final long holdTime) {
        if(channel.length != data.length)
            throw new RuntimeException("channel and data has different size");

        this.channel = channel;
        this.data = data;
        this.holdTime = holdTime;
    }

    public int[] getChannel() {
        return channel;
    }

    public int[] getData() {
        return data;
    }

    public long getHoldTime() {
        return holdTime;
    }

    public void send(){
        for (int i = 0; i < channel.length; i++)
            dmx.send(channel[i], data[i]);

    }
}
