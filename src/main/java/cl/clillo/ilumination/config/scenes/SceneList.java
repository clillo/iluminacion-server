package cl.clillo.ilumination.config.scenes;

import cl.clillo.ilumination.config.ConcreteFixtureConfig;
import cl.clillo.ilumination.config.mhlight.MHLightList;
import cl.clillo.ilumination.config.mhpositions.MHPositionsList;
import cl.clillo.ilumination.fixture.dmx.Fixture;
import cl.clillo.ilumination.model.Point;
import cl.clillo.ilumination.model.Step;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
public class SceneList {

	private String id;
	private SceneNodeElement[] scene;
	private long duration;

	public Step buildStep(final ConcreteFixtureConfig concreteFixtureConfig,
						  final Map<String, MHPositionsList> positionsMap,
						  final Map<String, MHLightList> lightMap ){
		final List<Point> pointList = Lists.newArrayList();

		for(SceneNodeElement element: scene){
			final SceneNode sceneNode = element.getSceneNode();
			final Fixture fixture = concreteFixtureConfig.getFixture(sceneNode.getFixture());
			final Map<String, Integer> fixtureMap = fixture.getFixtureMap();

			final String position = element.getSceneNode().getPositions();
			final String lights = element.getSceneNode().getLights();

			if (!Objects.isNull(position)) {
				final List<Point> posDMXMap = positionsMap.get(position).getPointList(fixture);
				pointList.addAll(posDMXMap);
			}

			if (!Objects.isNull(lights)) {
				final List<Point> lightDMXMap = lightMap.get(lights).getPointList(fixture);
				pointList.addAll(lightDMXMap);
			}

			pointList.addAll(sceneNode.getPointList(fixtureMap));

		}

		return Step.builder()
				.pointList(pointList)
				.nextExecution(duration>0)
				.nextExecutionTime(duration)
				.build();
	}
}
