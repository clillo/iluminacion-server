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
            if ("Moving Head 1".equalsIgnoreCase(fixtureModel1.getModel()))
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
        list.add(new QLCRoboticFixture(this.manufacturer, this.model, this.mode, 101, this.name, this.universe, 259, 11, fixtureModelA));
        list.add(new QLCRoboticFixture(this.manufacturer, this.model, this.mode, 102, this.name, this.universe, 239, 11, fixtureModelA));

        list.add(new QLCRoboticFixture(this.manufacturer, this.model, this.mode, 201, this.name, this.universe, 439, 11, fixtureModelB));
        list.add(new QLCRoboticFixture(this.manufacturer, this.model, this.mode, 202, this.name, this.universe, 451, 11, fixtureModelB));
        list.add(new QLCRoboticFixture(this.manufacturer, this.model, this.mode, 203, this.name, this.universe, 463, 11, fixtureModelB));
        list.add(new QLCRoboticFixture(this.manufacturer, this.model, this.mode, 204, this.name, this.universe, 475, 11, fixtureModelB));

        list.add(new QLCRoboticFixture(this.manufacturer, this.model, this.mode, 301, this.name, this.universe, 54, 11, fixtureModelC));
        list.add(new QLCRoboticFixture(this.manufacturer, this.model, this.mode, 302, this.name, this.universe, 69, 11, fixtureModelC));
        list.add(new QLCRoboticFixture(this.manufacturer, this.model, this.mode, 303, this.name, this.universe, 89, 11, fixtureModelC));
        list.add(new QLCRoboticFixture(this.manufacturer, this.model, this.mode, 304, this.name, this.universe, 39, 11, fixtureModelC));

        return list;
    }

    public <T extends QLCFixture> T getFixture(final int id){
        return (T)fixtureMap.get(id);
    }
}
