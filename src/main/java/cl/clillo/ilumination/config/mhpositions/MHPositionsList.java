package cl.clillo.ilumination.config.mhpositions;

import cl.clillo.ilumination.config.ConcreteFixtureConfig;
import cl.clillo.ilumination.model.Step;
import cl.clillo.ilumination.fixture.dmx.Fixture;
import cl.clillo.ilumination.model.Point;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MHPositionsList {

	private String id;
	private MHPositionNodeElement[] positions;

	public Step buildStep(final ConcreteFixtureConfig concreteFixtureConfig){
		final List<Point> pointList = Lists.newArrayList();

		for(MHPositionNodeElement element: positions){
			final MHPositionNode position = element.getPosition();
			final Fixture fixture = concreteFixtureConfig.getFixture(position.getFixture());
			final Map<String, Integer> fixtureMap = fixture.getFixtureMap();

			pointList.addAll(position.getPointList(fixtureMap));

		}

		return Step.builder()
				.pointList(pointList)
				.build();
	}

	public List<Point> getPointList(final Fixture fixture){
		final List<Point> pointList = Lists.newArrayList();

		for(MHPositionNodeElement element: positions){
			final MHPositionNode position = element.getPosition();
			if (position.getFixture().equals(fixture.getId())) {
				final Map<String, Integer> fixtureMap = fixture.getFixtureMap();

				pointList.addAll(position.getPointList(fixtureMap));
			}
		}

		return pointList;
	}
}
