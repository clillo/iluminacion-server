package cl.clillo.lighting.model;

import cl.clillo.lighting.external.dmx.Dmx;
import cl.clillo.lighting.gui.ScreenPoint;

import java.util.List;

public class QLCExecutionNode {

    private int id;
    private final Dmx dmx = Dmx.getInstance();
    private final UniverseChannel[] channel;
    private final int[] data;
    private final long holdTime;

    private final ScreenPoint[] screenPoints;

    public QLCExecutionNode(final UniverseChannel[] channel, final int[] data, final long holdTime, final ScreenPoint[] screenPoints) {
        this.screenPoints = screenPoints;
        if (channel.length != data.length && data.length != screenPoints.length) {
            throw new RuntimeException("channel, screenPoints and data has different size");
        }

        this.channel = channel;
        this.data = data;
        this.holdTime = holdTime;

    }

    public static QLCExecutionNodeBuilder builder() {
        return new QLCExecutionNodeBuilder();
    }

    public void setId(int id) {
        this.id = id;
    }

    public ScreenPoint[] getScreenPoints() {
        return screenPoints;
    }

    public void send() {
        for (int i = 0; i < channel.length; i++)
            dmx.send(channel[i].universe(), channel[i].channel(), data[i]);

    }

    public static class QLCExecutionNodeBuilder {
        private UniverseChannel[] channel;
        private int[] data;
        private ScreenPoint[] screenPoints;
        private long holdTime;
        private int lengthChannels=0;

        QLCExecutionNodeBuilder() {
        }

        public QLCExecutionNodeBuilder channel(List<UniverseChannel[]> channel) {
            int n=0;
            for (UniverseChannel[] ints : channel) {
                if (ints.length>lengthChannels)
                    lengthChannels=ints.length;
                n += ints.length;
            }
            this.channel = new UniverseChannel[n];
            n=0;
            for (UniverseChannel[] ints : channel)
                for (UniverseChannel anInt : ints) this.channel[n++] = anInt;

            return this;
        }

        public QLCExecutionNodeBuilder channel(UniverseChannel[] channel) {
            this.channel = channel;
            return this;
        }

        public QLCExecutionNodeBuilder data(List<int[]> data) {
            if (data.size()==0) {
                this.data = new int[0];
                return this;
            }
            int n=0;
            for (int[] ints : data) {
                n += Math.min(ints.length, this.lengthChannels);

            }
            this.data = new int[n];
            n=0;
            for (int[] ints : data) {
                int index = 0;
                for (int anInt : ints) {
                    if (index++ < this.lengthChannels)
                        this.data[n++] = anInt;
                }
            }
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
