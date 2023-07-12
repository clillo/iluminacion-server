package cl.clillo.lighting.model;

import cl.clillo.lighting.fixture.qlc.QLCRoboticFixture;
import lombok.Getter;

@Getter
public class QLCEfxFixtureData {

    private QLCRoboticFixture fixture;
    private boolean reverse;
    private double startOffset;

    QLCEfxFixtureData(QLCRoboticFixture fixture, boolean reverse, double startOffset) {
        this.fixture = fixture;
        this.reverse = reverse;
        this.startOffset = startOffset;
    }

    public static QLCEfxFixtureDataBuilder builder() {
        return new QLCEfxFixtureDataBuilder();
    }


    public static class QLCEfxFixtureDataBuilder {
        private QLCRoboticFixture fixture;
        private boolean reverse;
        private double startOffset;

        QLCEfxFixtureDataBuilder() {
        }

        public QLCEfxFixtureDataBuilder fixture(QLCRoboticFixture fixture) {
            this.fixture = fixture;
            return this;
        }

        public QLCEfxFixtureDataBuilder reverse(boolean reverse) {
            this.reverse = reverse;
            return this;
        }

        public QLCEfxFixtureDataBuilder reverse() {
            this.reverse = true;
            return this;
        }

        public QLCEfxFixtureDataBuilder startOffset(double startOffset) {
            this.startOffset = startOffset;
            return this;
        }

        public QLCEfxFixtureData build() {
            return new QLCEfxFixtureData(this.fixture, this.reverse, this.startOffset);
        }

        public String toString() {
            return "QLCEfxFixtureData.QLCEfxFixtureDataBuilder(fixture=" + this.fixture + ", reverse=" + this.reverse + ", startOffset=" + this.startOffset + ")";
        }
    }
}
