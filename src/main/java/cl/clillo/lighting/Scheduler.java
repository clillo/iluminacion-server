package cl.clillo.lighting;

import cl.clillo.lighting.dmx.ArtNet;
import cl.clillo.lighting.model.Show;

import java.util.List;

public class Scheduler extends Thread {

    private final List<Show> showList;

    public Scheduler(List<Show> showList) {
        this.showList = showList;
    }

    public void run() {
        try {
            while (true) {
                long now = System.currentTimeMillis();

                for (Show show : showList) {
                    if (show.isExecuting() && show.getNextExecutionTime() < now) {
                        show.getStepExecutor().execute(show);
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
