package cl.clillo.ilumination.config.mhlight;

import cl.clillo.ilumination.config.mhpositions.MHPositionsList;
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
public class MHLightConfig {

    @Bean
    public List<MHLightList> getMHLightLists() {
        final List<MHLightList> MHLightLists = new ArrayList<>();
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final File file = new File(classLoader.getResource("escenas/").getFile());

        final File[] listFiles = file.listFiles();

        for (File f: listFiles) {
            final String id = StringUtils.substringBetween(f.getName(), "light-", ".yml");
            if (StringUtils.isNoneEmpty(id)) {
                MHLightList mhLightList = readFile(id, f.getAbsolutePath());
                mhLightList.setId(id);
                MHLightLists.add(mhLightList);

            }
        }
        return MHLightLists;
    }

    private MHLightList readFile(final String id, final String fileName){
        log.info("id [{}] in file:  {}", id, fileName);
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.findAndRegisterModules();
            return mapper.readValue(new File(fileName), MHLightList.class);
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
