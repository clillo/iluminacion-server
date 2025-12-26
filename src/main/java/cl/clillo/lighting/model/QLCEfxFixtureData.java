package cl.clillo.lighting.model;

import cl.clillo.lighting.fixture.qlc.QLCFixture;
import cl.clillo.lighting.fixture.qlc.QLCRoboticFixture;
import cl.clillo.lighting.fixture.qlc.QLCSimpleRoboticFixture;
import lombok.Getter;

@Getter
public class QLCEfxFixtureData {

    private final QLCRoboticFixture roboticFixture;
    private final QLCFixture fixture;
    private final boolean reverse;
    private final double startOffset;

    QLCEfxFixtureData(QLCRoboticFixture roboticFixture, QLCFixture fixture, boolean reverse, double startOffset) {
        this.roboticFixture = roboticFixture;
        this.fixture = fixture;
        this.reverse = reverse;
        this.startOffset = startOffset;
    }

    public UniverseChannel[] getChannels(){
        if (roboticFixture==null){
            if (fixture==null){
                System.out.println("null fixture");
            }

            final QLCSimpleRoboticFixture simpleRoboticFixture = (QLCSimpleRoboticFixture)fixture;
            return new UniverseChannel[]{
                    new UniverseChannel(fixture.getUniverse(), simpleRoboticFixture.getPanDmxChannel()),
                    new UniverseChannel(fixture.getUniverse(), simpleRoboticFixture.getTiltDmxChannel())};
        }
       return new UniverseChannel[]{
               new UniverseChannel(fixture.getUniverse(),  roboticFixture.getPanDmxChannel()),
               new UniverseChannel(fixture.getUniverse(), roboticFixture.getTiltDmxChannel()),
               new UniverseChannel(fixture.getUniverse(), roboticFixture.getPanFineDmxChannel()),
               new UniverseChannel(fixture.getUniverse(), roboticFixture.getTiltFineDmxChannel())};
    }

    public static QLCEfxFixtureDataBuilder builder() {
        return new QLCEfxFixtureDataBuilder();
    }

    public static class QLCEfxFixtureDataBuilder {
        private QLCRoboticFixture qlcRoboticFixture;
        private QLCFixture fixture;
        private boolean reverse;
        private double startOffset;

        QLCEfxFixtureDataBuilder() {
        }

        public QLCEfxFixtureDataBuilder roboticFixture(QLCRoboticFixture fixture) {
            this.qlcRoboticFixture = fixture;
            return this;
        }

        public QLCEfxFixtureDataBuilder fixture(QLCFixture fixture) {
            if (fixture instanceof QLCRoboticFixture)
                this.qlcRoboticFixture = (QLCRoboticFixture) fixture;
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
            return new QLCEfxFixtureData(this.qlcRoboticFixture, this.fixture, this.reverse, this.startOffset);
        }

    }
}
