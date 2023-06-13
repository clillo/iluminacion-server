package cl.clillo.ilumination.config;

import cl.clillo.ilumination.Utils;
import cl.clillo.ilumination.config.scenes.SceneConfig;
import cl.clillo.ilumination.config.scenes.Scene;
import cl.clillo.ilumination.config.scenes.SceneNode;
import cl.clillo.ilumination.config.scenes.SceneNodeElement;
import cl.clillo.ilumination.executor.BouncingExecutor;
import cl.clillo.ilumination.executor.GenericExecutor;
import cl.clillo.ilumination.model.*;
import cl.clillo.ilumination.repository.ScenesRepository;
import cl.clillo.ilumination.repository.ShowsRepository;
import cl.clillo.ilumination.repository.SceneNodeRepository;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${show.path}")
    private String baseDir;

    private final AtomicInteger atomicInteger = new AtomicInteger();

    @Autowired
    private GenericExecutor genericExecutor;

    @Autowired
    private BouncingExecutor bouncingExecutor;

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

    //@Bean
    public List<Show> geShowList() {
        final List<Show> showList = Lists.newArrayList();

        final List<File> shows = Utils.getDirectories(baseDir+prefix);

        for(File f: shows) {
            final Show show = buildShowFromScenes(f.getName());
            //save(show);
            showList.add(show);

        }

        final Show dummy = Show.builder()
                .name("bouncing-auto")
                .stepExecutor(bouncingExecutor)
                .executing(true)
                .firstTimeExecution(true)
                .build();

        showList.add(dummy);

        return showList;
    }

    public Show buildShowFromScenes(final String showName){
        final List<Scene> scenesLists = sceneConfig.getScenesLists(baseDir+prefix+showName, showName);

        return Show.builder()
                .name(showName)
                .stepExecutor(genericExecutor)
                .stepList(showPostProcessor.buildStepListFromScenes(scenesLists))
                .scenesLists(scenesLists)
                .executing(false)
                .build();
    }

    public void write(final Show show, final Scene scene){
        sceneConfig.write(baseDir+prefix, scene);
        show.setStepList(showPostProcessor.buildStepListFromScenes(show.getScenesLists()));
    }

    private void save(final Show show){

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
