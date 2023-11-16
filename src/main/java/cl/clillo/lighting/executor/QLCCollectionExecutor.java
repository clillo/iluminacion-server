package cl.clillo.lighting.executor;

import cl.clillo.lighting.model.QLCCollection;
import cl.clillo.lighting.model.Show;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class QLCCollectionExecutor extends IStepExecutor {

    public QLCCollectionExecutor(final Show show) {
        super(show, List.of());

    }

    @Override
    public void executeDefaultScheduler() {
        if (show.isFirstTimeExecution()){
            show.setFirstTimeExecution(false);
        }

        final QLCCollection sequence = show.getFunction();
        log.info("executing {} collection {}:", show.getName(), sequence.getName() );
    }
}
