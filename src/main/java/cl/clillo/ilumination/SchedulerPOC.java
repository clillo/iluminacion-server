package cl.clillo.ilumination;

import cl.clillo.ilumination.executor.ProgramExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SchedulerPOC {

    @Autowired
    private ProgramExecutor programExecutor;

    @Scheduled(fixedRate = 100)
    public void tic() {
        programExecutor.tic();

    }
}
