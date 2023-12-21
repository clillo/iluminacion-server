package cl.clillo.lighting.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@Setter
public class QLCChaserStep {

    private int id;
    private int fadeIn;
    private int hold;
    private int fadeOut;
    private final Show show;

    QLCChaserStep(final int id, final int fadeIn, final int hold, final int fadeOut, final Show show) {
        this.id = id;
        this.fadeIn = fadeIn;
        this.hold = hold;
        this.fadeOut = fadeOut;
        this.show = show;
    }

    public static QLCStepBuilder builder() {
        return new QLCStepBuilder();
    }

    public static class QLCStepBuilder {
        private int id;
        private int fadeIn;
        private int hold;
        private int fadeOut;
        private Show show;

        QLCStepBuilder() {
        }

        public QLCStepBuilder id(int id) {
            this.id = id;
            return this;
        }

        public QLCStepBuilder fadeIn(int fadeIn) {
            this.fadeIn = fadeIn;
            return this;
        }

        public QLCStepBuilder hold(int hold) {
            this.hold = hold;
            return this;
        }

        public QLCStepBuilder fadeOut(int fadeOut) {
            this.fadeOut = fadeOut;
            return this;
        }

        public QLCStepBuilder show(Show show) {
            this.show = show;
            return this;
        }

        public QLCChaserStep build() {
            return new QLCChaserStep(this.id, this.fadeIn, this.hold, this.fadeOut, show);
        }


    }
}
