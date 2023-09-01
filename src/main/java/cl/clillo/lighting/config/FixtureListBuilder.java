package cl.clillo.lighting.config;

import cl.clillo.lighting.fixture.qlc.QLCFixture;

public interface FixtureListBuilder {

    <T extends QLCFixture> T getFixture(int id);
}
