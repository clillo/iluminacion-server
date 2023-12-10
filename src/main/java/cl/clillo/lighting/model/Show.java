package cl.clillo.lighting.model;

import cl.clillo.lighting.executor.IOS2LEventListener;
import cl.clillo.lighting.executor.AbstractExecutor;
import cl.clillo.lighting.executor.QLCCollectionExecutor;
import cl.clillo.lighting.executor.QLCEfxExecutor;
import cl.clillo.lighting.executor.QLCSceneExecutor;
import cl.clillo.lighting.executor.QLCSequenceExecutor;

import java.util.ArrayList;
import java.util.List;

public class Show implements Comparable<Show> {

    private int id;
    private String name;
    private long nextExecutionTime;
    private boolean executing;
    private AbstractExecutor stepExecutor;
    private boolean firstTimeExecution;
    private QLCFunction function;
    private List<Show> uniqueShow;
    private int[] dimmerChannels;
    private IOS2LEventListener.Type vdjType;

    public Show(final int id, final String name, final QLCFunction function, final List<Show> uniqueShow) {
        this.id = id;
        this.name = name;

        this.executing = false;

        this.firstTimeExecution = true;
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

    public AbstractExecutor getStepExecutor() {
        return this.stepExecutor;
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
        boolean isAlreadyExecuting = this.executing;
        this.executing = executing;
        this.nextExecutionTime = -1;
        if (executing && !isAlreadyExecuting)
            setFirstTimeExecution(true);
    }

    public void setExecuteOneTime(boolean executing) {
        boolean isAlreadyExecuting = this.executing;
        this.executing = executing;
        this.nextExecutionTime = -1;
        if (executing && !isAlreadyExecuting)
            setFirstTimeExecution(true);
    }

    public void setStepExecutor(AbstractExecutor stepExecutor) {
        this.stepExecutor = stepExecutor;
        if (function!=null && function instanceof QLCEfxSpline)
            ((QLCEfxExecutor)stepExecutor).setSpeed(100);
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

    public void setVdjType(IOS2LEventListener.Type vdjType) {
        this.vdjType = vdjType;
    }

    public IOS2LEventListener.Type getVdjType() {
        return vdjType;
    }

    @Override
    public int compareTo(Show o) {
        return this.id - o.getId();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Show))
            return false;
        return ((Show)obj).getId() == this.id;
    }

    public static class ShowBuilder {
        private static int idCount = 1;

        private String name;
        private QLCFunction function;

        ShowBuilder() {
        }

        public ShowBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ShowBuilder function(QLCFunction function) {
            this.function = function;
            return this;
        }

        public Show build(int id) {
            Show show = new Show(id == -1 ? idCount++ : id, this.name, this.function, new ArrayList<>());

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
