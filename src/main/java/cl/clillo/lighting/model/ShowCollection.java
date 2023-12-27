package cl.clillo.lighting.model;

import cl.clillo.lighting.config.QLCFixtureBuilder;
import cl.clillo.lighting.executor.DefaultScheduler;
import cl.clillo.lighting.executor.OS2LScheduler;
import cl.clillo.lighting.repository.StateRepository;
import cl.clillo.lighting.utils.FileUtils;
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
    private final List<Show> showList = new ArrayList<>();
    private QLCModel qlcModelOriginal;
    private QLCFixtureBuilder qlcModel;
    private final StateRepository stateRepository = StateRepository.getInstance();
    private OS2LScheduler os2LScheduler;
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
        System.out.println("Starting default scheduler");
        scheduler.start();

        System.out.println("Starting OS2L scheduler");
        os2LScheduler = new OS2LScheduler(showList);
        os2LScheduler.start();
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

    public OS2LScheduler getOs2LScheduler() {
        return os2LScheduler;
    }

    public int getRealDMXValue(final int dmxChannel, final int dmxValue){
        int maxValue = stateRepository.getMaxValue(dmxChannel);
   //    System.out.println(dmxChannel+"\t"+dmxValue + "\t" +maxValue+"\t"+Math.min(dmxValue, maxValue));
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

    public List<Show> getShowList() {
        return showList;
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

    public QLCFixtureBuilder getQlcModel() {
        return qlcModel;
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

    public List<QLCPoint> getBlackoutPointList() {
        return blackoutPointList;
    }
}
