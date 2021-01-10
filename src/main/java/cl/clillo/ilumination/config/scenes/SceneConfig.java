package cl.clillo.ilumination.config.scenes;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
@Log4j2
public class SceneConfig {

    @Bean
    public List<SceneList> getScenesLists() {
        final List<SceneList> SceneLists = new ArrayList<>();
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final File file = new File(classLoader.getResource("escenas/").getFile());

        final File[] listFiles = file.listFiles();

        for (File f: listFiles) {
            final String id = StringUtils.substringBetween(f.getName(), "scene-", ".yml");
            if (StringUtils.isNoneEmpty(id)) {
                SceneList sceneList = readFile(id, f.getAbsolutePath());
                sceneList.setId(id);
                SceneLists.add(sceneList);

            }
        }
        return SceneLists;
    }

    private SceneList readFile(final String id, final String fileName){
        log.info("id [{}] in file:  {}", id, fileName);

        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.findAndRegisterModules();
            return mapper.readValue(new File(fileName), SceneList.class);
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
