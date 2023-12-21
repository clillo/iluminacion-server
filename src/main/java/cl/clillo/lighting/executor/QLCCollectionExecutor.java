package cl.clillo.lighting.executor;

import cl.clillo.lighting.gui.controller.MidiButtonFunctionRepository;
import cl.clillo.lighting.model.Chaser;
import cl.clillo.lighting.model.QLCCollection;
import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.model.ShowCollection;
import lombok.extern.log4j.Log4j2;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Log4j2
public class QLCCollectionExecutor extends AbstractExecutor {

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
            start();
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

    @Override
    public void start() {
        super.start();
        final List<Show> allShows = ShowCollection.getInstance().getShowList();
        final Set<Show> collectionShows = new HashSet<>(collection.getShowList());
        final MidiButtonFunctionRepository midiButtonFunctionRepository = MidiButtonFunctionRepository.getInstance();
        for (Show show: allShows)
            if (!collectionShows.contains(show) && show.getId()!=collection.getShow().getId() && !(show.getFunction() instanceof Chaser)) {
                show.setExecuting(false);
                if (midiButtonFunctionRepository.getButton(show.getId())!=null)
                    midiButtonFunctionRepository.getButton(show.getId()).setExecuting(false);
            }
    }

    private void execute(boolean executing){
        final MidiButtonFunctionRepository midiButtonFunctionRepository = MidiButtonFunctionRepository.getInstance();
        for (Show show: collection.getShowList()) {
            show.setExecuting(executing);
            midiButtonFunctionRepository.getButton(show.getId()).setExecuting(executing);
        }
    }
}
