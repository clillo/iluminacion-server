package cl.clillo.lighting.config.mhlight;

import cl.clillo.lighting.config.ConcreteFixtureConfig;
import cl.clillo.lighting.model.Step;
import cl.clillo.lighting.fixture.dmx.Fixture;
import cl.clillo.lighting.model.Point;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MHLightList {

	private String id;
	private MHLightNodeElement[] lights;

	public Step buildStep(final ConcreteFixtureConfig concreteFixtureConfig){
		final List<Point> pointList = Lists.newArrayList();

		for(MHLightNodeElement element: lights){
			final MHLightNode lightNode = element.getLight();
			final Fixture fixture = concreteFixtureConfig.getFixture(lightNode.getFixture());
			final Map<String, Integer> fixtureMap = fixture.getFixtureMap();

			pointList.addAll(lightNode.getPointList(fixtureMap));

		}

		return Step.builder()
				.pointList(pointList)
				.build();
	}

	public  List<Point> getPointList(final Fixture fixture){
		final List<Point> pointList = Lists.newArrayList();

		for(MHLightNodeElement element: lights){
			final MHLightNode lightNode = element.getLight();
			if (lightNode.getFixture().equals(fixture.getId())) {
				final Map<String, Integer> fixtureMap = fixture.getFixtureMap();

				pointList.addAll(lightNode.getPointList(fixtureMap));
			}

		}

		return pointList;
	}
}
