package cl.clillo.ilumination.config;

import cl.clillo.ilumination.config.mhlight.MHLightList;
import cl.clillo.ilumination.config.mhpositions.MHPositionsList;
import cl.clillo.ilumination.config.scenes.SceneList;
import cl.clillo.ilumination.executor.GenericExecutor;
import cl.clillo.ilumination.executor.Program;
import cl.clillo.ilumination.model.Step;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ProgramsConfig {

    @Autowired
    private GenericExecutor genericExecutor;

    @Autowired
    private List<MHPositionsList> mhPositionsLists;

    @Autowired
    private List<MHLightList> mhLightLists;

    @Autowired
    private List<SceneList> scenesLists;

    @Autowired
    private ConcreteFixtureConfig concreteFixtureConfig;

    @Bean
    public List<Program> getProgramList() {
        final List<Program> programs = new ArrayList<>();

  //      programs.add(buildProgramFromPositions());
   //     programs.add(buildProgramFromLights());
        programs.add(buildProgramFromScenes());

        return programs;
    }

    private Program buildProgramFromScenes(){
        final List<Step> stepListPos = new ArrayList<>();
        final Map<String, MHPositionsList> positionsMap = Maps.newHashMap();
        final Map<String, MHLightList> lightMap = Maps.newHashMap();

        for(MHPositionsList posList: mhPositionsLists){
            positionsMap.put(posList.getId(), posList);

        }

        for(MHLightList lightList: mhLightLists){
            lightMap.put(lightList.getId(), lightList);

        }

        for(SceneList sceneList: scenesLists){
            stepListPos.add(sceneList.buildStep(concreteFixtureConfig, positionsMap, lightMap));

        }

        return Program.builder()
                .name("Prueba 3")
                .stepExecutor(genericExecutor)
                .stepList(stepListPos)
                .executing(true)
                .build();
    }

    private Program buildProgramFromPositions(){
        final List<Step> stepListPos = new ArrayList<>();

        for(MHPositionsList posList: mhPositionsLists){
            stepListPos.add(posList.buildStep(concreteFixtureConfig));

        }

        return Program.builder()
                .name("Prueba 1")
                .stepExecutor(genericExecutor)
                .stepList(stepListPos)
                .executing(true)
                .build();
    }

    private Program buildProgramFromLights(){
        final List<Step> stepListLight = new ArrayList<>();

        for(MHLightList lightList: mhLightLists){
            stepListLight.add(lightList.buildStep(concreteFixtureConfig));

        }

        return Program.builder()
                .name("Prueba 2")
                .stepExecutor(genericExecutor)
                .stepList(stepListLight)
                .executing(true)
                .build();
    }
}
