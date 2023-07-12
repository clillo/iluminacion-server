package cl.clillo.lighting.model;

import cl.clillo.lighting.dmx.Dmx;
import cl.clillo.lighting.utils.ScreenPoint;

import java.util.List;

public class QLCExecutionNode {

    private int id;
    private final Dmx dmx = Dmx.getInstance();
    private final int[] channel;
    private final int[] data;
    private final long holdTime;

    private final ScreenPoint[] screenPoints;

    public QLCExecutionNode(final int[] channel, final int[] data, final long holdTime, final ScreenPoint[] screenPoints) {
        this.screenPoints = screenPoints;
        if (channel.length != data.length && data.length != screenPoints.length)
            throw new RuntimeException("channel, screenPoints and data has different size");

        this.channel = channel;
        this.data = data;
        this.holdTime = holdTime;
    }

    public static QLCExecutionNodeBuilder builder() {
        return new QLCExecutionNodeBuilder();
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

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public ScreenPoint[] getScreenPoints() {
        return screenPoints;
    }

    public void send() {
        for (int i = 0; i < channel.length; i++)
            dmx.send(channel[i], data[i]);

    }

    public static class QLCExecutionNodeBuilder {
        private int[] channel;
        private int[] data;
        private ScreenPoint[] screenPoints;
        private long holdTime;

        QLCExecutionNodeBuilder() {
        }

        public QLCExecutionNodeBuilder channel(List<int[]> channel) {
            int n=0;
            for (int[] ints : channel) n += ints.length;

            this.channel = new int[n];
            n=0;
            for (int[] ints : channel)
                for (int anInt : ints) this.channel[n++] = anInt;

            return this;
        }

        public QLCExecutionNodeBuilder channel(int[] channel) {
            this.channel = channel;
            return this;
        }

        public QLCExecutionNodeBuilder data(List<int[]> data) {
            int n=0;
            for (int[] ints : data) n += ints.length;

            this.data = new int[n];
            n=0;
            for (int[] ints : data)
                for (int anInt : ints) this.data[n++] = anInt;

            return this;
        }

        public QLCExecutionNodeBuilder data(final int[] data) {
            this.data = data;
            return this;
        }

        public QLCExecutionNodeBuilder holdTime(final long holdTime) {
            this.holdTime = holdTime;
            return this;
        }

        public QLCExecutionNodeBuilder screenPoints(final ScreenPoint[] screenPoints) {
            this.screenPoints = screenPoints;
            return this;
        }

        public QLCExecutionNode build() {
            return new QLCExecutionNode(this.channel, this.data, this.holdTime, this.screenPoints);
        }

    }
}
