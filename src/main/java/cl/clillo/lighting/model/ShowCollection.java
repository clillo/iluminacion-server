package cl.clillo.lighting.model;

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
}
