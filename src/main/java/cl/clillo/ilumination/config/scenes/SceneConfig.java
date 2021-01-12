package cl.clillo.ilumination.config.scenes;

import cl.clillo.ilumination.Utils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Log4j2
public class SceneConfig {

    private static final String prefix = "scene-";

    public List<Scene> getScenesLists(final String baseDir) {
        final List<Scene> scenes = new ArrayList<>();

        final List<File> sceneFiles = Utils.getFiles(baseDir, prefix);

        for (File f: sceneFiles) {
            final String id = StringUtils.substringBetween(f.getName(), prefix, ".yml");
            Scene scene = readFile(id, f.getAbsolutePath());
            scene.setId(id);
            scenes.add(scene);
        }

        return scenes;
    }

    private Scene readFile(final String id, final String fileName){
        log.info("id [{}] in file:  {}", id, fileName);

        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.findAndRegisterModules();
            return mapper.readValue(new File(fileName), Scene.class);
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
