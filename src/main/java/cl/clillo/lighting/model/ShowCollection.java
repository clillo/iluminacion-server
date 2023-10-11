package cl.clillo.lighting.model;

import cl.clillo.lighting.Scheduler;
import cl.clillo.lighting.config.QLCFixtureBuilder;
import cl.clillo.lighting.utils.FileUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShowCollection {

    private final List<Show> showList = new ArrayList<>();
    private final List<Show> showQLCEfxList = new ArrayList<>();
    private final QLCModel qlcModelOriginal;
    private final QLCFixtureBuilder qlcModel;
    private final Scheduler scheduler;

    private ShowCollection(){
        qlcModelOriginal = new QLCModel();
        qlcModel = new QLCFixtureBuilder(qlcModelOriginal.getFixtureModelList());
        scheduler = new Scheduler(showList);
        addFromDirectory("src/main/resources/qlc");
        scheduler.start();
    }

    private static final class InstanceHolder {
        private static ShowCollection instance;

        public static ShowCollection getInstance() {
            if (instance==null){
                instance = new ShowCollection();
            }
            return instance;
        }
    }

    public static ShowCollection getInstance() {
        return InstanceHolder.getInstance();
    }

    public void addQLCEfx(final QLCEfx qlcEfx){
        Show show = Show.builder()
                .name(qlcEfx.getName())
                .executing(false)
                .firstTimeExecution(true)
                .stepList(List.of())
                .function(qlcEfx)
                .build(qlcEfx.getId());
        show.setUniqueShow(showQLCEfxList);
        addShow(show);
        showQLCEfxList.add(show);
    }

    private void addQLCScene(final QLCScene qlcScene){
        final Show show = Show.builder()
                .name(qlcScene.getName())
                .executing(false)
                .firstTimeExecution(true)
                .stepList(List.of())
                .function(qlcScene)
                .build(qlcScene.getId());
        qlcScene.setShow(show);
        addShow(show);
    }

    private void addQLCSequence(final QLCSequence qlcSequence){
        final Show show = Show.builder()
                .name(qlcSequence.getName())
                .executing(false)
                .firstTimeExecution(true)
                .stepList(List.of())
                .function(qlcSequence)
                .build(qlcSequence.getId());
        qlcSequence.setShow(show);
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
        Collections.sort(showList);
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
                    addQLCScene(QLCScene.read(qlcModel, f));
                if (f.getName().startsWith("QLCSequence"))
                    addQLCSequence(QLCSequence.read(qlcModel, f));
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public List<QLCFunction> getOriginalFunctionList(final String type, final String path){
        final List<QLCFunction> functionList = new ArrayList<>();
        for (QLCFunction function: qlcModelOriginal.getFunctionList())
            if (type.equalsIgnoreCase(function.getType()) && path.equalsIgnoreCase(function.getPath()))
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

    public Show getShow(final int sceneId){
        for (Show show: showList)
            if (show.getId()==sceneId)
                return show;
        return null;
    }
}
