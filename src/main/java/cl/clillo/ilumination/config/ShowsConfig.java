package cl.clillo.ilumination.config;

import cl.clillo.ilumination.Utils;
import cl.clillo.ilumination.config.scenes.SceneConfig;
import cl.clillo.ilumination.config.scenes.Scene;
import cl.clillo.ilumination.config.scenes.SceneNode;
import cl.clillo.ilumination.config.scenes.SceneNodeElement;
import cl.clillo.ilumination.executor.GenericExecutor;
import cl.clillo.ilumination.model.SceneEntity;
import cl.clillo.ilumination.model.Show;
import cl.clillo.ilumination.model.ShowEntity;
import cl.clillo.ilumination.model.SceneNodeEntity;
import cl.clillo.ilumination.repository.ScenesRepository;
import cl.clillo.ilumination.repository.ShowsRepository;
import cl.clillo.ilumination.repository.SceneNodeRepository;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ShowsConfig {

    private static final String prefix = "show/";

    private final AtomicInteger atomicInteger = new AtomicInteger();

    @Autowired
    private GenericExecutor genericExecutor;

    @Autowired
    private ShowPostProcessor showPostProcessor;

    @Autowired
    private SceneConfig sceneConfig;

    @Autowired
    private ScenesRepository scenesRepository;

    @Autowired
    private SceneNodeRepository sceneNodeRepository;

    @Autowired
    private ShowsRepository showsRepository;

    @Bean
    public List<Show> getProgramList() {
        final List<Show> programs = Lists.newArrayList();

        final List<File> shows = Utils.getDirectories(prefix);

        for(File f: shows) {
            final Show show = buildShowFromScenes(f.getName());
            //save(show);
            programs.add(show);

        }

        return programs;
    }

    public Show buildShowFromScenes(final String name){
        final List<Scene> scenesLists = sceneConfig.getScenesLists(prefix+name);

        return Show.builder()
                .name(name)
                .stepExecutor(genericExecutor)
                .stepList(showPostProcessor.buildStepListFromScenes(scenesLists))
                .scenesLists(scenesLists)
                .executing(false)
                .build();
    }

    private void save(Show show){

        final Set<SceneEntity> scenes = Sets.newHashSet();

        for(Scene scene : show.getScenesLists()) {
            final Set<SceneNodeEntity> steps = Sets.newHashSet();
            final SceneEntity sceneEntity = SceneEntity.builder()
                    .name(show.getName())
                    .sceneNodes(steps)
                    .duration(scene.getDuration())
                    .build();

            scenes.add(sceneEntity);

            for(SceneNodeElement sceneNodeElement: scene.getScene()) {
                final SceneNode sceneNode = sceneNodeElement.getSceneNode();

                steps.add(SceneNodeEntity.builder()
                        .dimmer(sceneNode.getDimmer())
                        .fixture(sceneNode.getFixture())
                        .speed(sceneNode.getSpeed())
                        .lights(sceneNode.getLights())
                        .positions(sceneNode.getPositions())
                        .build());
            }

            sceneNodeRepository.saveAll(steps);
            scenesRepository.save(sceneEntity);
        }

        showsRepository.save(ShowEntity.builder()
                .name(show.getName())
                .scenes(scenes)
                .build());
    }
}