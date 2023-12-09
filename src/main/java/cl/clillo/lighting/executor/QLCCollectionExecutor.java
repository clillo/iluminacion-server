package cl.clillo.lighting.executor;

import cl.clillo.lighting.gui.controller.MidiButtonFunctionRepository;
import cl.clillo.lighting.gui.controller.QLCButton;
import cl.clillo.lighting.model.QLCCollection;
import cl.clillo.lighting.model.Show;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class QLCCollectionExecutor extends IStepExecutor {

    private int sendNumber;
    private final QLCCollection collection;

    public QLCCollectionExecutor(final Show show) {
        super(show, List.of());
        collection = show.getFunction();
    }

    @Override
    public void executeDefaultScheduler() {
        if (show.isFirstTimeExecution()) {
            show.setFirstTimeExecution(false);
            sendNumber = 2;
        }
        if (sendNumber <= 0)
            return;

        executing = true;

        sendNumber--;
        log.info("executing {} collection {}:", show.getName(), collection.getName());

        execute(true);

        show.setNextExecutionTime(System.currentTimeMillis() + 100);
    }

    @Override
    public void stop() {
        super.stop();
        execute(false);


    }

    private void execute(boolean executing){
        final MidiButtonFunctionRepository midiButtonFunctionRepository = MidiButtonFunctionRepository.getInstance();
        for (Show show1: collection.getShowList()) {
            show1.setExecuting(executing);
            midiButtonFunctionRepository.getButton(show1.getId()).setExecuting(executing);
        }
    }
}
