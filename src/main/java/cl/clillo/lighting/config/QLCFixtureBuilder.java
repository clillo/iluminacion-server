package cl.clillo.lighting.config;

import cl.clillo.lighting.fixture.qlc.QLCFixture;
import cl.clillo.lighting.fixture.qlc.QLCFixtureModel;
import cl.clillo.lighting.fixture.qlc.QLCRoboticFixture;
import cl.clillo.lighting.fixture.qlc.QLCSimpleRoboticFixture;
import cl.clillo.lighting.model.QLCPoint;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QLCFixtureBuilder implements FixtureListBuilder{

    private final Map<Integer, QLCFixture> fixtureMap;
    @Getter
    private final List<QLCFixture> fixtureList;
    @Getter
    private final List<QLCPoint> blackoutPointList = new ArrayList<>();

    public QLCFixtureBuilder(List<QLCFixtureModel> fixtureModelList) {
        this.fixtureMap = new HashMap<>();

        QLCFixtureModel fixtureModelA = null;
        QLCFixtureModel fixtureModelC = null;

        for (QLCFixtureModel fixtureModel1: fixtureModelList) {
            if ("Moving Head 2".equalsIgnoreCase(fixtureModel1.getModel()))
                fixtureModelA = fixtureModel1;
            if ("beam+spot".equalsIgnoreCase(fixtureModel1.getModel()))
                fixtureModelC = fixtureModel1;
        }
        fixtureList = new ArrayList<>();
        

        try {
            fixtureList.addAll(buildFixturesFromYaml(fixtureModelList, fixtureModelA, fixtureModelC));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        
        for (QLCFixture fixture: fixtureList) {
            fixtureMap.put(fixture.getId(), fixture);
            blackoutPointList.addAll(fixture.getBlackoutPointList());
        }
    }
    
    /**
     * Construye los fixtures desde la configuración YAML.
     */
    private List<QLCFixture> buildFixturesFromYaml(List<QLCFixtureModel> fixtureModelList, 
                                                    QLCFixtureModel fixtureModelA, 
                                                    QLCFixtureModel fixtureModelC) {
        final List<QLCFixture> list = new ArrayList<>();
        FixturesConfigService configService = FixturesConfigService.getInstance();
        FixturesConfig config = configService.getConfig();
        FixturesTypesConfigService typesConfigService = FixturesTypesConfigService.getInstance();
        
        // Construir fixtures desde la configuración YAML
        for (FixtureConfig fixtureConfig : config.getFixtures()) {
            // Filtrar fixtures inactivos
          /*  if (!fixtureConfig.isActivo()) {
                continue;
            }
            */
            QLCFixtureModel model = getFixtureModel(fixtureConfig.getModel(), typesConfigService, fixtureModelA, fixtureModelC);
            
            if (model == null) {
                continue; // Saltar si no se encuentra el modelo
            }
            
            QLCFixture fixture;
            switch (fixtureConfig.getType()) {
                case "robotic":
                    fixture = QLCRoboticFixture.build(fixtureConfig.getId(), 
                            fixtureConfig.getUniverse(), 
                            fixtureConfig.getAddress(),
                            model);
                    break;
                case "simple-robotic":
                    fixture = QLCSimpleRoboticFixture.build(fixtureConfig.getId(), 
                            fixtureConfig.getAddress(), 
                            model, fixtureConfig.getUniverse());
                    fixture.setUniverse(fixtureConfig.getUniverse());
                    fixture.setAddress(fixtureConfig.getAddress() - 1); // Convertir a 0-based
                    break;
                default: // "simple"
                    fixture = QLCFixture.build(fixtureConfig.getId(), 
                            fixtureConfig.getAddress(), 
                            model, fixtureConfig.getUniverse());
                    fixture.setAddress(fixtureConfig.getAddress() - 1); // Convertir a 0-based
                    break;
            }
            
            list.add(fixture);
        }
        
        return list;
    }
    
    /**
     * Obtiene el modelo de fixture desde la configuración YAML o desde los modelos cargados desde QLC.
     */
    private QLCFixtureModel getFixtureModel(String modelName, 
                                            FixturesTypesConfigService typesConfigService,
                                            QLCFixtureModel fixtureModelA,
                                            QLCFixtureModel fixtureModelC) {
        // Primero intentar cargar desde la configuración YAML
        FixtureTypeConfig typeConfig = typesConfigService.getFixtureType(modelName);
        if (typeConfig != null) {
            return new QLCFixtureModel(
                    typeConfig.getManufacturer(),
                    typeConfig.getModel(),
                    typeConfig.getType(),
                    typeConfig.getChannelsArray(),
                    typeConfig.isRobotic()
            );
        }
        
        // Si no está en YAML, usar los modelos cargados desde QLC (Moving Head 2 y beam+spot)
        if ("Moving Head 2".equalsIgnoreCase(modelName)) return fixtureModelA;
        if ("beam+spot".equalsIgnoreCase(modelName)) return fixtureModelC;
        
        return null;
    }

    @Override
    public <T extends QLCFixture> T getFixture(final int id){
        return (T)fixtureMap.get(id);
    }

}
