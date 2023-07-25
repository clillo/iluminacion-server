package cl.clillo.lighting.config;

import cl.clillo.lighting.fixture.qlc.QLCFixture;
import cl.clillo.lighting.fixture.qlc.QLCFixtureModel;
import cl.clillo.lighting.fixture.qlc.QLCRoboticFixture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QLCFixtureBuilder {

    private final Map<Integer, QLCFixture> fixtureMap;
    private String manufacturer = "manufacturer";
    private String model = "model";
    private String mode = "mode";
    private String name = "name";
    private int universe = 0;


    public QLCFixtureBuilder(List<QLCFixtureModel> fixtureModelList) {
        this.fixtureMap = new HashMap<>();

        QLCFixtureModel fixtureModelA = null;
        QLCFixtureModel fixtureModelB = null;
        QLCFixtureModel fixtureModelC = null;
        for (QLCFixtureModel fixtureModel1: fixtureModelList) {
            if ("MovingHead".equalsIgnoreCase(fixtureModel1.getManufacturer()))
               fixtureModelB = fixtureModel1;
       //     if ("Moving Head 2".equalsIgnoreCase(fixtureModel1.getModel()))
        //        fixtureModelB = fixtureModel1;
            if ("Moving Head 2".equalsIgnoreCase(fixtureModel1.getModel()))
                fixtureModelA = fixtureModel1;
            if ("beam+spot".equalsIgnoreCase(fixtureModel1.getModel()))
                fixtureModelC = fixtureModel1;
        }

        List<QLCFixture> fixtureList = buildDefaultFixtures(fixtureModelA, fixtureModelB, fixtureModelC);
        for (QLCFixture fixture: fixtureList)
            fixtureMap.put(fixture.getId(), fixture);

    }

    public List<QLCFixture> buildDefaultFixtures(QLCFixtureModel fixtureModelA, QLCFixtureModel fixtureModelB, QLCFixtureModel fixtureModelC){
        final List<QLCFixture> list = new ArrayList<>();

        list.add(QLCRoboticFixture.build(101, 260, fixtureModelA));
        list.add(QLCRoboticFixture.build(102, 240, fixtureModelA));

        list.add(QLCRoboticFixture.build(201, 440, fixtureModelB));
        list.add(QLCRoboticFixture.build(202, 452, fixtureModelB));
        list.add(QLCRoboticFixture.build(203, 464, fixtureModelB));
        list.add(QLCRoboticFixture.build(204, 476, fixtureModelB));

        list.add(QLCRoboticFixture.build(301, 55, fixtureModelC));
        list.add(QLCRoboticFixture.build(302, 70, fixtureModelC));
        list.add(QLCRoboticFixture.build(303, 90, fixtureModelC));
        list.add(QLCRoboticFixture.build(304, 40, fixtureModelC));

        return list;
    }

    public <T extends QLCFixture> T getFixture(final int id){
        return (T)fixtureMap.get(id);
    }
}
