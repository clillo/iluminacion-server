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
    private final List<QLCFixture> fixtureList;

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
        fixtureList = new ArrayList<>();
        fixtureList.addAll(buildDefaultFixtures(fixtureModelA, fixtureModelB, fixtureModelC));
        for (QLCFixture fixture: fixtureList) {
            fixtureMap.put(fixture.getId(), fixture);
        }
    }

    public List<QLCFixture> buildDefaultFixtures(QLCFixtureModel fixtureModelA,
                                                 QLCFixtureModel fixtureModelB,
                                                 QLCFixtureModel fixtureModelC){
        final List<QLCFixture> list = new ArrayList<>();

        QLCFixtureModel fixtureLaser = new QLCFixtureModel("Generic", "Generic", "Laser", new String[24], false);
        QLCFixtureModel fixtureDerby = new QLCFixtureModel("Generic", "Generic", "Derby", new String[]{
                "master dimmer", "red", "green", "blue", "white", "strobe", "Gobo wheel", "auto"}, false);
        QLCFixtureModel fixtureSpider = new QLCFixtureModel("Generic", "Generic", "Spider", new String[]{
                "pan", "tilt", "dimmer", "strobo", "rojo.l", "verde.l", "azul.l", "blanco.l", "blanco.r", "azul.r", "verde.r", "rojo.r"}, false);
        QLCFixtureModel fixtureRGBW = new QLCFixtureModel("Generic", "Generic", "RGBW", new String[]{
                "master dimmer", "red", "green", "blue", "white", "strobe", "color change"}, false);
        QLCFixtureModel movingHeadBeam = new QLCFixtureModel("Wild Pro", "Moving Head Spot", "Moving Head", new String[]{
                "pan", "pan fine", "tilt", "tilt fine", "titl/pan speed", "nc", "nc2", "color wheel", "dimmer", "strobo", "macro"}, false);

        //010 - 033	[13]	Laser	Generic
        list.add(QLCFixture.build(13, 10, fixtureLaser)); // laser

        //040 - 050	[8]	Moving Head	beam+spot
        //055 - 065	[10]	Moving Head	beam+spot
        //070 - 080	[11]	Moving Head	beam+spot
        //090 - 100	[18]	Moving Head	beam+spot
        list.add(QLCRoboticFixture.build(8, 40, fixtureModelC));
        list.add(QLCRoboticFixture.build(10, 55, fixtureModelC));
        list.add(QLCRoboticFixture.build(11, 70, fixtureModelC));
        list.add(QLCRoboticFixture.build(18, 90, fixtureModelC));

        //150 - 157	[7]	Derby	Generic
        //181 - 188	[6]	Derby	Generic
        list.add(QLCFixture.build(7, 150, fixtureDerby)); // derby
        list.add(QLCFixture.build(6, 181, fixtureDerby));

        //240 - 254	[9]	Moving Head	Moving Head 2
        //260 - 274	[12]	Moving Head	Moving Head 2
        list.add(QLCRoboticFixture.build(9, 240, fixtureModelA)); // ex 102
        list.add(QLCRoboticFixture.build(12, 260, fixtureModelA)); // ex 101

        //300 - 311	[4]	Spider	Generic
        //320 - 331	[5]	Spider	Generic
        list.add(QLCSimpleRoboticFixture.build(4, 300, fixtureSpider)); // spider
        list.add(QLCSimpleRoboticFixture.build(5, 320, fixtureSpider));

        //400 - 406	[0]	RGBW	Generic
        //410 - 416	[1]	RGBW	Generic
        //420 - 426	[2]	RGBW	Generic
        //430 - 436	[3]	RGBW	Generic
        list.add(QLCFixture.build(0, 400, fixtureRGBW));
        list.add(QLCFixture.build(1, 410, fixtureRGBW));
        list.add(QLCFixture.build(2, 420, fixtureRGBW));
        list.add(QLCFixture.build(3, 430, fixtureRGBW));

        //440 - 450	[14]	Moving Head	Beam
        //452 - 462	[15]	Moving Head	Beam
        //464 - 474	[16]	Moving Head	Beam
        //476 - 486	[17]	Moving Head	Beam
        list.add(QLCRoboticFixture.build(14, 440, movingHeadBeam)); // 201
        list.add(QLCRoboticFixture.build(15, 452, movingHeadBeam)); // 202
        list.add(QLCRoboticFixture.build(16, 464, movingHeadBeam)); // 203
        list.add(QLCRoboticFixture.build(17, 476, movingHeadBeam)); // 204

        return list;
    }

    @Override
    public <T extends QLCFixture> T getFixture(final int id){
        return (T)fixtureMap.get(id);
    }

    public List<QLCFixture> getFixtureList() {
        return fixtureList;
    }
}
