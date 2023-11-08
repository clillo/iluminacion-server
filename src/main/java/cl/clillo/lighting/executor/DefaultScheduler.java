package cl.clillo.lighting.executor;

import cl.clillo.lighting.external.dmx.ArtNet;
import cl.clillo.lighting.model.Show;

import java.util.List;

public class DefaultScheduler extends Thread {

    private final List<Show> showList;

    public DefaultScheduler(final List<Show> showList) {
        this.showList = showList;
    }

    public void run() {
        try {
            while (true) {
                long now = System.currentTimeMillis();

                for (Show show : showList) {
                    if (show.isExecuting() && show.getNextExecutionTime() < now) {
                        show.getStepExecutor().executeDefaultScheduler();
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
