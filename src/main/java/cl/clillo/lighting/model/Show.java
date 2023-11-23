package cl.clillo.lighting.model;

import cl.clillo.lighting.executor.IStepExecutor;
import cl.clillo.lighting.executor.QLCCollectionExecutor;
import cl.clillo.lighting.executor.QLCEfxExecutor;
import cl.clillo.lighting.executor.QLCSceneExecutor;
import cl.clillo.lighting.executor.QLCSequenceExecutor;

import java.util.ArrayList;
import java.util.List;

public class Show implements Comparable<Show> {

    private static final long NEXT_EXECUTION_DEFAULT = 3000;

    private int id;
    private String name;
    private long nextExecutionTime;
    private boolean executing;
    private IStepExecutor stepExecutor;
    private int pasoActual;
    private boolean firstTimeExecution;
    private QLCFunction function;
    private List<Show> uniqueShow;
    private int[] dimmerChannels;

    public Show(int id, String name,  boolean executing, IStepExecutor stepExecutor,
                int pasoActual, boolean firstTimeExecution, QLCFunction function,
                List<Show> uniqueShow) {
        this.id = id;
        this.name = name;

        this.executing = executing;
        this.stepExecutor = stepExecutor;
        this.pasoActual = pasoActual;

        this.firstTimeExecution = firstTimeExecution;
        this.function = function;
        this.uniqueShow = uniqueShow;
        dimmerChannels = function.getDimmerChannels();
    }

    public Show() {
    }

    public boolean isStatic(){
        return !(getFunction() instanceof QLCSequence);
    }

    public <T extends QLCFunction> T getFunction() {
        return (T) function;
    }

    public static ShowBuilder builder() {
        return new ShowBuilder();
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public long getNextExecutionTime() {
        return this.nextExecutionTime;
    }

    public boolean isExecuting() {
        return this.executing;
    }

    public IStepExecutor getStepExecutor() {
        return this.stepExecutor;
    }

    public int getPasoActual() {
        return this.pasoActual;
    }

    public boolean isFirstTimeExecution() {
        return this.firstTimeExecution;
    }

    public List<Show> getUniqueShow() {
        return this.uniqueShow;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNextExecutionTime(long nextExecutionTime) {
        this.nextExecutionTime = nextExecutionTime;
    }

    public void setExecuting(boolean executing) {
        this.executing = executing;
        this.nextExecutionTime = -1;
        if (executing)
            setFirstTimeExecution(true);
    }

    public void setExecuteOneTime(boolean executing) {
        this.executing = executing;
        this.nextExecutionTime = -1;
        if (executing)
            setFirstTimeExecution(true);
    }

    public void setStepExecutor(IStepExecutor stepExecutor) {
        this.stepExecutor = stepExecutor;
    }

    public void setFirstTimeExecution(boolean firstTimeExecution) {
        this.firstTimeExecution = firstTimeExecution;
    }

    public void setFunction(QLCFunction function) {
        this.function = function;
    }

    public void setUniqueShow(List<Show> uniqueShow) {
        this.uniqueShow = uniqueShow;
    }

    public String toString() {
        return "Show(id=" + this.getId() + ", name=" + this.getName() + ")";
    }

    public int[] getDimmerChannels() {
        return dimmerChannels;
    }

    public void setDimmerChannels(int[] dimmerChannels) {
        this.dimmerChannels = dimmerChannels;
    }

    @Override
    public int compareTo(Show o) {
        return this.id - o.getId();
    }

    public static class ShowBuilder {
        private static int idCount = 1;

        private String name;
        private boolean executing = false;
        private boolean firstTimeExecution = true;
        private QLCFunction function;

        ShowBuilder() {
        }

        public ShowBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ShowBuilder executing(boolean executing) {
            this.executing = executing;
            return this;
        }

        public ShowBuilder firstTimeExecution(boolean firstTimeExecution) {
            this.firstTimeExecution = firstTimeExecution;
            return this;
        }

        public ShowBuilder function(QLCFunction function) {
            this.function = function;
            return this;
        }

        public Show build(int id) {
            IStepExecutor executor = null;

            Show show = new Show(id == -1 ? idCount++ : id, this.name,  this.executing, executor,
                    0,
                    this.firstTimeExecution, this.function, new ArrayList<>());

            if (function instanceof QLCSequence) {
                show.setStepExecutor(new QLCSequenceExecutor(show));

            }
            if (function instanceof QLCEfx)
                show.setStepExecutor(new QLCEfxExecutor(show));

            if (function instanceof QLCScene)
                show.setStepExecutor(new QLCSceneExecutor(show));

            if (function instanceof QLCCollection)
                show.setStepExecutor(new QLCCollectionExecutor(show));
            return show;
        }

        public Show build() {
            return build(-1);
        }

    }

}
