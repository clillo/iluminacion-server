package cl.clillo.lighting.config;

import cl.clillo.lighting.fixture.qlc.QLCFixture;
import cl.clillo.lighting.fixture.qlc.QLCFixtureModel;
import cl.clillo.lighting.fixture.qlc.QLCRoboticFixture;
import cl.clillo.lighting.fixture.qlc.QLCSimpleRoboticFixture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QLCFixtureBuilder implements FixtureListBuilder{

    private final Map<Integer, QLCFixture> fixtureMap;

    public QLCFixtureBuilder(List<QLCFixtureModel> fixtureModelList) {
        this.fixtureMap = new HashMap<>();

        QLCFixtureModel fixtureModelA = null;
        QLCFixtureModel fixtureModelB = null;
        QLCFixtureModel fixtureModelC = null;

        for (QLCFixtureModel fixtureModel1: fixtureModelList) {
            if ("MovingHead".equalsIgnoreCase(fixtureModel1.getManufacturer()))
               fixtureModelB = fixtureModel1;
            if ("Moving Head 2".equalsIgnoreCase(fixtureModel1.getModel()))
                fixtureModelA = fixtureModel1;
            if ("beam+spot".equalsIgnoreCase(fixtureModel1.getModel()))
                fixtureModelC = fixtureModel1;
        }

        List<QLCFixture> fixtureList = buildDefaultFixtures(fixtureModelA, fixtureModelB, fixtureModelC);
        for (QLCFixture fixture: fixtureList)
            fixtureMap.put(fixture.getId(), fixture);

    }

    public List<QLCFixture> buildDefaultFixtures(QLCFixtureModel fixtureModelA,
                                                 QLCFixtureModel fixtureModelB,
                                                 QLCFixtureModel fixtureModelC){
        final List<QLCFixture> list = new ArrayList<>();

        list.add(QLCRoboticFixture.build(101, 260, fixtureModelA));
        list.add(QLCRoboticFixture.build(102, 240, fixtureModelA));

        list.add(QLCRoboticFixture.build(201, 440, fixtureModelB));
        list.add(QLCRoboticFixture.build(202, 452, fixtureModelB));
        list.add(QLCRoboticFixture.build(203, 464, fixtureModelB));
        list.add(QLCRoboticFixture.build(204, 476, fixtureModelB));

        list.add(QLCRoboticFixture.build(8, 55, fixtureModelC));
        list.add(QLCRoboticFixture.build(10, 70, fixtureModelC));
        list.add(QLCRoboticFixture.build(11, 90, fixtureModelC));
        list.add(QLCRoboticFixture.build(18, 40, fixtureModelC));

        QLCFixtureModel fixtureSpider = new QLCFixtureModel("Generic", "Generic", "Spider", new String[]{
                "pan", "tilt", "dimmer", "strobo", "rojo.l", "verde.l", "azul.l", "blanco.l", "blanco.r", "azul.r", "verde.r", "rojo.r"}, false);

        list.add(QLCSimpleRoboticFixture.build(4, 300, fixtureSpider)); // spider
        list.add(QLCSimpleRoboticFixture.build(5, 320, fixtureSpider));

        QLCFixtureModel fixtureDerby = new QLCFixtureModel("Generic", "Generic", "Derby", new String[]{
                "master dimmer", "red", "green", "blue", "white", "strobe", "Gobo wheel", "auto"}, false);

        list.add(QLCFixture.build(7, 150, fixtureDerby)); // derby
        list.add(QLCFixture.build(6, 181, fixtureDerby));

        QLCFixtureModel fixtureLaser = new QLCFixtureModel("Generic", "Generic", "Laser", new String[24], false);

        list.add(QLCFixture.build(13, 10, fixtureLaser)); // laser
        return list;
    }

    @Override
    public <T extends QLCFixture> T getFixture(final int id){
        return (T)fixtureMap.get(id);
    }
}
