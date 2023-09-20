package cl.clillo.lighting.config.scenes;

import cl.clillo.lighting.utils.FileUtils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
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
    private static final String suffix = ".yml";

    public List<Scene> getScenesLists(final String baseDir, final String showName) {
        final List<Scene> scenes = new ArrayList<>();

        final List<File> sceneFiles = FileUtils.getFiles(baseDir, prefix, suffix);

        for (File f: sceneFiles) {
            final String id = StringUtils.substringBetween(f.getName(), prefix, suffix);
            Scene scene = readFile(id, f.getAbsolutePath());
            scene.setId(id);
            scene.setShowName(showName);
            scenes.add(scene);
        }

        return scenes;
    }

    public void write(final String baseDir, final Scene scene) {
        writeFile(scene, FileUtils.getFile(baseDir+ scene.getShowName()+"/", prefix+scene.getId()+ suffix).getAbsolutePath());
    }

    private Scene readFile(final String id, final String fileName){
        log.info("id [{}] in file:  {}", id, fileName);

        try {
            final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.findAndRegisterModules();
            return mapper.readValue(new File(fileName), Scene.class);
        } catch (JsonParseException e) {
           log.error(e);
        } catch (JsonMappingException e) {
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        }

        return null;
    }

    private void writeFile(final Scene scene, final String fileName){
        log.info("Write file id [{}] in file:  {}", scene.getId(), fileName);

        try {
            final ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
            mapper.findAndRegisterModules();
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.writeValue(new File(fileName), scene);

        } catch (JsonParseException e) {
            log.error(e);
        } catch (JsonMappingException e) {
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        }
    }
}
