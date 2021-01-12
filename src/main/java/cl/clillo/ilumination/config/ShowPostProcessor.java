package cl.clillo.ilumination.config;

import cl.clillo.ilumination.config.mhlight.MHLightList;
import cl.clillo.ilumination.config.mhpositions.MHPositionsList;
import cl.clillo.ilumination.config.scenes.Scene;
import cl.clillo.ilumination.model.Step;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ShowPostProcessor {

    @Autowired
    private ConcreteFixtureConfig concreteFixtureConfig;

    @Autowired
    private List<MHPositionsList> mhPositionsLists;

    @Autowired
    private List<MHLightList> mhLightLists;

    public List<Step> buildStepListFromScenes(final List<Scene> scenesLists){
        final List<Step> stepListPos = new ArrayList<>();
        final Map<String, MHPositionsList> positionsMap = Maps.newHashMap();
        final Map<String, MHLightList> lightMap = Maps.newHashMap();

        for(MHPositionsList posList: mhPositionsLists){
            positionsMap.put(posList.getId(), posList);

        }

        for(MHLightList lightList: mhLightLists){
            lightMap.put(lightList.getId(), lightList);

        }

        for(Scene scene : scenesLists){
            stepListPos.add(scene.buildStep(concreteFixtureConfig, positionsMap, lightMap));

        }

        return stepListPos;
    }
}
