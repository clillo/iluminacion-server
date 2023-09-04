package cl.clillo.lighting.model;

import cl.clillo.lighting.config.QLCFixtureBuilder;
import cl.clillo.lighting.utils.FileUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ShowCollection {

    final List<Show> showList = new ArrayList<>();
    final List<Show> showQLCEfxList = new ArrayList<>();

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
                .build();
        show.setUniqueShow(showQLCEfxList);
        showList.add(show);
        showQLCEfxList.add(show);
    }

    public void addQLCScene(final QLCScene qlcScene){
        showList.add(Show.builder()
                .name(qlcScene.getName())
                .executing(false)
                .firstTimeExecution(true)
                .stepList(List.of())
                .function(qlcScene)
                .build());
    }

    public List<Show> getShowList() {
        return showList;
    }

    public void troggleShow(final Show show){
        boolean isExecuting = show.isExecuting();

        for (Show show1: show.getUniqueShow())
            show1.setExecuting(false);

        show.setExecuting(!isExecuting);
    }

    public void addFromDirectory(final QLCFixtureBuilder qlcModel){
        File file = new File("src/main/resources/qlc");
        final List<File> files = FileUtils.getFiles(file.getAbsolutePath(), "QLCEfx");

        try {
            for (File f: files){
                if (f.getName().startsWith("QLCEfxCircle"))
                    addQLCEfx(QLCEfxCircle.read(qlcModel, f.getName()));
                if (f.getName().startsWith("QLCEfxSpline"))
                    addQLCEfx(QLCEfxSpline.read(qlcModel, f.getName()));
                if (f.getName().startsWith("QLCEfxMultiLine"))
                    addQLCEfx(QLCEfxMultiLine.read(qlcModel, f.getName()));
                if (f.getName().startsWith("QLCEfxLine"))
                    addQLCEfx(QLCEfxLine.read(qlcModel, f.getName()));
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }

    }
}
