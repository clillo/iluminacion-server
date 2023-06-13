package cl.clillo.ilumination;

import cl.clillo.ilumination.executor.ShowExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

//@Component
public class SchedulerPOC {

    @Autowired
    private ShowExecutor showExecutor;

    @Scheduled(fixedRate = 5)
    public void tic() {
        showExecutor.tic();

    }
}
