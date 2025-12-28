package cl.clillo.lighting.model;

import cl.clillo.lighting.config.QLCFixtureBuilder;
import cl.clillo.lighting.executor.DefaultScheduler;
import cl.clillo.lighting.executor.OS2LScheduler;
import cl.clillo.lighting.fixture.qlc.QLCFixture;
import cl.clillo.lighting.repository.StateRepository;
import cl.clillo.lighting.utils.FileUtils;
import lombok.Getter;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowCollection {

    public static final String BASE_DIR = "src/main/resources/qlc";
    @Getter
    private final List<Show> showList = new ArrayList<>();
    private QLCModel qlcModelOriginal;
    @Getter
    private QLCFixtureBuilder qlcModel;
    private final StateRepository stateRepository = StateRepository.getInstance();
    @Getter
    private OS2LScheduler os2LScheduler;
    @Getter
    private final List<QLCPoint> blackoutPointList = new ArrayList<>();

    private ShowCollection(){

    }

    private void init(){
        System.out.println("Building original Model");
        qlcModelOriginal = new QLCModel();
        System.out.println("Building new Model");
        qlcModel = new QLCFixtureBuilder(qlcModelOriginal.getFixtureModelList());
        blackoutPointList.addAll(qlcModel.getBlackoutPointList());
        DefaultScheduler scheduler = new DefaultScheduler(showList);
        System.out.println("Reading new shows");
        addFromDirectory(BASE_DIR);
        System.out.println("Reading JSON scenes");
        addJsonScenes();
        System.out.println("Reading JSON sequences");
        addJsonSequences();
        System.out.println("Starting default scheduler");
        scheduler.start();

        System.out.println("Starting OS2L scheduler");
        os2LScheduler = new OS2LScheduler(showList);
        os2LScheduler.start();

        initShowEvent();
    }

    private static final class InstanceHolder {
        private static ShowCollection instance;

        public static ShowCollection getInstance() {
            if (instance==null){
                instance = new ShowCollection();
                instance.init();
            }
            return instance;
        }
    }

    public static ShowCollection getInstance() {
        return InstanceHolder.getInstance();
    }

    public int getRealDMXValue(final int universe, final int dmxChannel, final int dmxValue){
        int maxValue = stateRepository.getMaxValue(universe, dmxChannel);
        if (maxValue>0)
            return Math.min(dmxValue, maxValue);

        return dmxValue;
    }

    public void addQLCEfx(final QLCEfx qlcEfx){
        Show show = Show.builder()
                .name(qlcEfx.getName())
                .function(qlcEfx)
                .build(qlcEfx.getId());

        addShow(show);
    }

    public void toggleShow(final Show show){
        boolean isExecuting = show.isExecuting();

        for (Show show1: show.getUniqueShow())
            show1.setExecuting(false);

        show.setExecuting(!isExecuting);
    }

    public void executeShow(final Show show){
        boolean isExecuting = show.isExecuting();
        if (!isExecuting && show.getStepExecutor().isExecuting())
            show.getStepExecutor().stop();

        for (Show show1: show.getUniqueShow())
            show1.setExecuting(false);

        show.setExecuting(isExecuting);
    }

    public void addFromDirectory(final String path){
        File file = new File(path);
        final List<File> directories = FileUtils.getDirectories(file.getAbsolutePath());
        addFromDirectory(file);

        for(File dir: directories)
            if (dir.listFiles()!=null)
                addFromDirectory(dir);

        for(File dir: directories)
            if (dir.listFiles()!=null)
                addCollectionFromDir(dir);

        for(File dir: directories)
            if (dir.listFiles()!=null)
                addChaserFromDir(dir);
    }


    public List<QLCFunction> getOriginalFunctionList(final String type, final String path){
        final List<QLCFunction> functionList = new ArrayList<>();
        for (QLCFunction function: qlcModelOriginal.getFunctionList())
            if (type.equalsIgnoreCase(function.getType()) && path.equalsIgnoreCase(function.getPath()))
                functionList.add(function);

        return functionList;
    }

    public List<QLCFunction> getOriginalFunctionList(final String type){
        final List<QLCFunction> functionList = new ArrayList<>();
        for (QLCFunction function: qlcModelOriginal.getFunctionList())
            if (type.equalsIgnoreCase(function.getType()) )
                functionList.add(function);

        return functionList;
    }

    public List<QLCFunction> getFunctionList(final String type, final String path){
        final List<QLCFunction> functionList = new ArrayList<>();
        for (Show show: showList) {
            QLCFunction function = show.getFunction();
            if (type.equalsIgnoreCase(function.getType()) && path.equalsIgnoreCase(function.getPath()))
                functionList.add(function);
        }

        return functionList;
    }

    public Map<Integer, QLCFunction> getFunctionMap(){
        final Map<Integer, QLCFunction> functionMap = new HashMap<>();
        for (Show show: showList) {
            QLCFunction function = show.getFunction();
            functionMap.put(function.getId(), function);
        }

        return functionMap;
    }

    public Show getShow(final int showId){
        for (Show show: showList)
            if (show.getId()==showId)
                return show;
        return null;
    }

    public void save(){
        for (Show show: showList) {
            QLCFunction function = show.getFunction();
            String dir = getDirectory(function);
            System.out.println(dir);
            function.writeToConfigFile(dir);
        }
    }

    public String getDirectory(final QLCFunction function){
        return FileUtils.getDirectory(BASE_DIR+"/"+function.getClass().getSimpleName()+"."+function.getPath()).getAbsolutePath();
    }

    public void globalBlackout() {
        System.out.println("globalBlackout");
        Show blackoutShow = getShow(301);
        blackoutShow.setExecuteOneTime(true);
    }

    public QLCFixture getFixture(int fixtureId){
        return getQlcModel().getFixture(fixtureId);
    }

    /**
     * Recarga los fixtures desde la configuración YAML.
     * Útil después de actualizar la configuración desde la interfaz web.
     */
    public void reloadFixtures() {
        try {
            System.out.println("Recargando fixtures desde configuración YAML...");
            QLCModel qlcModelOriginal = new QLCModel();
            QLCFixtureBuilder newQlcModel = new QLCFixtureBuilder(qlcModelOriginal.getFixtureModelList());
            
            // Actualizar referencias
            this.qlcModel = newQlcModel;
            
            // Limpiar y actualizar blackout points
            blackoutPointList.clear();
            blackoutPointList.addAll(newQlcModel.getBlackoutPointList());
            
            System.out.println("Fixtures recargados. Total: " + newQlcModel.getFixtureList().size());
        } catch (Exception e) {
            System.err.println("Error al recargar fixtures: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addQLCFunction(final QLCFunction chaser){
        final Show show = Show.builder()
                .name(chaser.getName())
                .function(chaser)
                .build(chaser.getId());
        chaser.setShow(show);
        addShow(show);
    }

    private void addShow(final Show show){
        if (show.getId()<=0) {
            System.out.println("Show con id 0: "+show.getName());
            System.exit(0);
        }
        if (show.getFunction().getId()<=0) {
            System.out.println("Show con function id 0: "+show.getName());
            System.exit(0);
        }

        for (Show show1: showList) {
            if (show1.getId() == show.getId()) {
                System.out.println("Show con id : " + show.getId() + " repetido\t" + show.getName() + "\t" + show1.getName());
                System.exit(0);
            }
            if (show1.getFunction().getId() == show.getFunction().getId()) {
                System.out.println("Show con function id : " + show.getFunction().getId() + " repetido\t" + show.getName() + "\t" + show1.getName());
                System.exit(0);
            }
        }

        showList.add(show);
    }

    private void addFromDirectory(final File file){
        final List<File> files = FileUtils.getFiles(file.getAbsolutePath(), "QLC", ".xml");

        try {
            for (File f: files){
                if (f.getName().startsWith("QLCEfxCircle"))
                    addQLCEfx(QLCEfxCircle.read(qlcModel, f));
                if (f.getName().startsWith("QLCEfxSpline"))
                     addQLCEfx(QLCEfxSpline.read(qlcModel, f));
                 if (f.getName().startsWith("QLCEfxMultiLine"))
                    addQLCEfx(QLCEfxMultiLine.read(qlcModel, f));
                if (f.getName().startsWith("QLCEfxLine"))
                    addQLCEfx(QLCEfxLine.read(qlcModel, f));
                 if (f.getName().startsWith("QLCScene"))
                     addQLCFunction(QLCScene.read(qlcModel, f));
                if (f.getName().startsWith("QLCSequence"))
                    addQLCFunction(QLCSequence.read(qlcModel, f));

            }

        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }

        Collections.sort(showList);
    }

    /**
     * Carga escenas desde archivos JSON en el directorio de efectos.
     * Solo carga escenas que no existan ya (por ID) para evitar duplicados con XML.
     */
    private void addJsonScenes() {
        final File effectsDir = new File(BASE_DIR + "/effects");
        if (!effectsDir.exists() || !effectsDir.isDirectory()) {
            return;
        }

        final List<File> jsonFiles = FileUtils.getFiles(effectsDir.getAbsolutePath(), "", ".json");
        for (File f : jsonFiles) {
            if (f.getName().contains("Scene") || f.getName().contains("scene")) {
                try {
                    final QLCScene jsonScene = QLCScene.readFromJson(qlcModel, f);
                    // Verificar si ya existe una escena con este ID
                    boolean exists = false;
                    for (Show show : showList) {
                        if (show.getFunction().getId() == jsonScene.getId()) {
                            exists = true;
                            System.out.println("Scene " + jsonScene.getId() + " already loaded from XML, skipping JSON: " + f.getName());
                            break;
                        }
                    }
                    if (!exists) {
                        addQLCFunction(jsonScene);
                      //  System.out.println("Loaded JSON scene: " + jsonScene.getId() + " - " + jsonScene.getName() + " from " + f.getName());
                    }
                } catch (Exception e) {
                    System.err.println("Error reading JSON scene from " + f.getName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        Collections.sort(showList);
    }

    /**
     * Carga secuencias desde archivos JSON en el directorio de efectos.
     * Solo carga secuencias que no existan ya (por ID) para evitar duplicados con XML.
     */
    private void addJsonSequences() {
        final File effectsDir = new File(BASE_DIR + "/effects");
        if (!effectsDir.exists() || !effectsDir.isDirectory()) {
            return;
        }

        final List<File> jsonFiles = FileUtils.getFiles(effectsDir.getAbsolutePath(), "", ".json");
        for (File f : jsonFiles) {
            if (f.getName().contains("Sequence") || f.getName().contains("sequence")) {
                try {
                    final QLCSequence jsonSequence = QLCSequence.readFromJson(qlcModel, f);
                    // Verificar si ya existe una secuencia con este ID
                    boolean exists = false;
                    for (Show show : showList) {
                        if (show.getFunction().getId() == jsonSequence.getId()) {
                            exists = true;
                            System.out.println("Sequence " + jsonSequence.getId() + " already loaded from XML, skipping JSON: " + f.getName());
                            break;
                        }
                    }
                    if (!exists) {
                        addQLCFunction(jsonSequence);
                       // System.out.println("Loaded JSON sequence: " + jsonSequence.getId() + " - " + jsonSequence.getName() + " from " + f.getName());
                    }
                } catch (Exception e) {
                    System.err.println("Error reading JSON sequence from " + f.getName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        Collections.sort(showList);
    }

    private void addCollectionFromDir(final File file){
        final List<File> files = FileUtils.getFiles(file.getAbsolutePath(), "QLC", ".xml");
        try {
            for (File f: files){
                if (f.getName().startsWith("QLCCollection"))
                    addQLCFunction(QLCCollection.read(this, f));
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }

        Collections.sort(showList);
    }

    private void addChaserFromDir(final File file){
        final List<File> files = FileUtils.getFiles(file.getAbsolutePath(), "Chaser", ".xml");
        try {
            for (File f: files)
                addQLCFunction(Chaser.read(this, f));

        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }

        Collections.sort(showList);
    }

    private void initShowEvent(){
        for (Show show: showList) {
            QLCFunction function = show.getFunction();
            if (function instanceof QLCScene scene){
                if (scene.isInitEventTrigger())
                    show.setExecuteOneTime(true);
            }
        }
    }

    private void endShowEvent(){

    }


}
