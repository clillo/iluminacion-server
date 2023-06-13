package cl.clillo.lighting;

import cl.clillo.lighting.executor.ShowExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

//@Component
public class SchedulerPOC {

    @Autowired
    private ShowExecutor showExecutor;

    @Scheduled(fixedRate = 5)
    public void tic() {
        showExecutor.tic();

    }
}
