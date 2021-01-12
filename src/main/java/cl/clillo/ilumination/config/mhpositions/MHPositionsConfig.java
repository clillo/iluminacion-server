package cl.clillo.ilumination.config.mhpositions;

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
import java.util.Objects;

@Configuration
@Log4j2
public class MHPositionsConfig {

    @Bean
    public List<MHPositionsList> getMHPositionsLists() {
        final List<MHPositionsList> MHPositionsLists = new ArrayList<>();
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final File file = new File(Objects.requireNonNull(classLoader.getResource("escenas/positions/")).getFile());

        final File[] listFiles = file.listFiles();

        for (File f: listFiles) {
            final String id = StringUtils.substringBetween(f.getName(), "pos-", ".yml");
            if (StringUtils.isNoneEmpty(id)) {
                MHPositionsList mhPositionsList = readFile(id, f.getAbsolutePath());
                mhPositionsList.setId(id);
                MHPositionsLists.add(mhPositionsList);

            }
        }
        return MHPositionsLists;
    }

    private MHPositionsList readFile(final String id, final String fileName){
        log.info("id [{}] in file:  {}", id, fileName);

        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.findAndRegisterModules();
            return mapper.readValue(new File(fileName), MHPositionsList.class);
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
