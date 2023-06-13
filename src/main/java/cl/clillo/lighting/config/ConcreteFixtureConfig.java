package cl.clillo.lighting.config;

import cl.clillo.lighting.fixture.dmx.Fixture;
import cl.clillo.lighting.fixture.dmx.MovingHead;
import cl.clillo.lighting.fixture.dmx.MovingHead60;
import cl.clillo.lighting.fixture.dmx.MovingHead90;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.List;

@Configuration
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ConcreteFixtureConfig {

    private ImmutableMap<String, Fixture> fixturesMap;

    @PostConstruct
    public void init() {
        MovingHead60 movingHead60_1 = (MovingHead60)getFixtureFromFile("mh-60-1", MovingHead60.class);
        MovingHead60 movingHead60_2 = (MovingHead60)getFixtureFromFile("mh-60-2", MovingHead60.class);
        MovingHead90 movingHead90_1 = (MovingHead90)getFixtureFromFile("mh-90-1", MovingHead90.class);
        MovingHead90 movingHead90_2 = (MovingHead90)getFixtureFromFile("mh-90-2", MovingHead90.class);

        movingHead60_1.init();
        movingHead60_2.init();
        movingHead90_1.init();
        movingHead90_2.init();

        fixturesMap = new ImmutableMap.Builder<String, Fixture>()
                .put("mh-60-1", movingHead60_1)
                .put("mh-60-2", movingHead60_2)
                .put("mh-90-1", movingHead90_1)
                .put("mh-90-2", movingHead90_2)
                .build();
    }

    private Fixture getFixtureFromFile(final String id, final Class c){
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            File file = new File(classLoader.getResource("fixtures/"+id+".yaml").getFile());
            ObjectMapper om = new ObjectMapper(new YAMLFactory());

           return (Fixture)om.readValue(file, c);
        }catch (Exception e){
            e.printStackTrace();

        }
        return null;
    }

    public Fixture getFixture(String id){
        return fixturesMap.get(id);
    }

    public List<MovingHead> getMovingHeads() {
        final List<MovingHead> movingHeadList = Lists.newArrayList();

        for(Fixture fixture: fixturesMap.values()){
            if (fixture.getId().startsWith("mh-"))
                movingHeadList.add((MovingHead) fixture);
        }
        return movingHeadList;
    }

}
