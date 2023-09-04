package cl.clillo.lighting;

import cl.clillo.lighting.dmx.ArtNet;
import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.model.ShowCollection;

import java.util.List;

public class Scheduler extends Thread {

    private final List<Show> showList;

    public Scheduler() {
        this.showList = ShowCollection.getInstance().getShowList();
    }

    public void run() {
        try {
            while (true) {
                long now = System.currentTimeMillis();

                for (Show show : showList) {
                    if (show.isExecuting() && show.getNextExecutionTime() < now) {
                        show.getStepExecutor().execute();
                    }

                }

                ArtNet.getInstance().broadCast();
                Thread.sleep(1);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
