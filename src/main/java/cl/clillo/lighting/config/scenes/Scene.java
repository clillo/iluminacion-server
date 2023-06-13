package cl.clillo.lighting.config.scenes;

import cl.clillo.lighting.config.ConcreteFixtureConfig;
import cl.clillo.lighting.config.mhlight.MHLightList;
import cl.clillo.lighting.config.mhpositions.MHPositionsList;
import cl.clillo.lighting.fixture.dmx.Fixture;
import cl.clillo.lighting.model.Point;
import cl.clillo.lighting.model.Step;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
public class Scene {

	@JsonIgnore
	private String id;
	@JsonIgnore
	private String showName;
	private SceneNodeElement[] scene;
	private long duration;

	public Step buildStep(final ConcreteFixtureConfig concreteFixtureConfig,
						  final Map<String, MHPositionsList> positionsMap,
						  final Map<String, MHLightList> lightMap ){
		final List<Point> pointList = Lists.newArrayList();
		final StringBuilder sb = new StringBuilder();

		for(SceneNodeElement element: scene){
			final SceneNode sceneNode = element.getSceneNode();
			final Fixture fixture = concreteFixtureConfig.getFixture(sceneNode.getFixture());
			final Map<String, Integer> fixtureMap = fixture.getFixtureMap();

			final String position = element.getSceneNode().getPositions();
			final String lights = element.getSceneNode().getLights();

			if (StringUtils.isNoneEmpty(position)) {
				final MHPositionsList posList = positionsMap.get(position);
				if (Objects.isNull(posList)){
					System.out.println("position id: ["+position+"] is not valid");
				}else {
					final List<Point> posDMXMap = posList.getPointList(fixture);
					pointList.addAll(posDMXMap);
				}
			}

			if (StringUtils.isNoneEmpty(lights)) {
				final List<Point> lightDMXMap = lightMap.get(lights).getPointList(fixture);
				pointList.addAll(lightDMXMap);
			}

			pointList.addAll(sceneNode.getPointList(fixtureMap));

		}

		return Step.builder()
				.pointList(pointList)
				.nextExecution(duration>0)
				.nextExecutionTime(duration)
				.description("scene: "+id)
				.build();
	}

	public void update(final Scene scene){
		this.duration = scene.getDuration();
		for (int i=0; i<getScene().length; i++){
			final SceneNode sceneNodeOriginal = getScene()[i].getSceneNode();
			final SceneNode sceneNode = scene.getScene()[i].getSceneNode();

			sceneNodeOriginal.setDimmer(sceneNode.getDimmer());
			sceneNodeOriginal.setFixture(sceneNode.getFixture());
			sceneNodeOriginal.setLights(sceneNode.getLights());
			sceneNodeOriginal.setPositions(sceneNode.getPositions());
			sceneNodeOriginal.setSpeed(sceneNode.getSpeed());
		}
	}
}
