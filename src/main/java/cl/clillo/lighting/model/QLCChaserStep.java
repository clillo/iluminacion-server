package cl.clillo.lighting.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
@Setter
public class QLCChaserStep {

    private int id;
    private int fadeIn;
    private int hold;
    private int fadeOut;
    private final QLCFunction collection;

    QLCChaserStep(int id, int fadeIn, int hold, int fadeOut, QLCFunction collection) {
        this.id = id;
        this.fadeIn = fadeIn;
        this.hold = hold;
        this.fadeOut = fadeOut;
        this.collection = collection;
    }

    public static QLCStepBuilder builder() {
        return new QLCStepBuilder();
    }

    public static class QLCStepBuilder {
        private int id;
        private int fadeIn;
        private int hold;
        private int fadeOut;
        private QLCFunction collection;

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

        public QLCStepBuilder collection(QLCFunction collection) {
            this.collection = collection;
            return this;
        }

        public QLCChaserStep build() {
            return new QLCChaserStep(this.id, this.fadeIn, this.hold, this.fadeOut, collection);
        }


    }
}
