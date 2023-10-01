package cl.clillo.lighting.model;

import cl.clillo.lighting.config.scenes.Scene;
import cl.clillo.lighting.executor.IQLCStepExecutor;
import cl.clillo.lighting.executor.QLCEfxExecutor;
import cl.clillo.lighting.executor.QLCSceneExecutor;
import cl.clillo.lighting.executor.QLCSequenceExecutor;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class Show {

    private static final long NEXT_EXECUTION_DEFAULT = 3000;

    private int id;
    private String name;
    private long nextExecutionTime;
    private boolean executing;
    private IQLCStepExecutor stepExecutor;
    private int pasoActual;
    private List<Step> stepList;
    private List<Scene> scenesLists;
    private boolean firstTimeExecution;
    private QLCFunction function;
    private List<Show> uniqueShow;

    public <T extends QLCFunction> T getFunction() {
        return (T) function;
    }

    public static ShowBuilder builder() {
        return new ShowBuilder();
    }

    public Step nextStep() {
        pasoActual++;

        if (pasoActual >= stepList.size())
            pasoActual = 0;

        final Step step = stepList.get(pasoActual);

        if (step.isNextExecution()) {
            nextExecutionTime = System.currentTimeMillis() + step.getNextExecutionTime();
        } else {
            nextExecutionTime = System.currentTimeMillis() + NEXT_EXECUTION_DEFAULT;
        }
        return step;
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

    public IQLCStepExecutor getStepExecutor() {
        return this.stepExecutor;
    }

    public int getPasoActual() {
        return this.pasoActual;
    }

    public List<Scene> getScenesLists() {
        return this.scenesLists;
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
        this.nextExecutionTime =  -1;
        if (executing)
            setFirstTimeExecution(true);
    }

    public void setExecuteOneTime(boolean executing) {
        this.executing = executing;
        this.nextExecutionTime =  -1;
        if (executing)
            setFirstTimeExecution(true);
    }

    public void setStepExecutor(IQLCStepExecutor stepExecutor) {
        this.stepExecutor = stepExecutor;
    }

    public void setStepList(List<Step> stepList) {
        this.stepList = stepList;
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
        return "Show(id=" + this.getId() + ", name=" + this.getName() +")";
    }

    public static class ShowBuilder {
        private static int idCount = 1;

        private String name;
        private long nextExecutionTime;
        private boolean executing;
        private List<Step> stepList;
        private List<Scene> scenesLists;
        private boolean firstTimeExecution;
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

        public ShowBuilder stepList(List<Step> stepList) {
            this.stepList = stepList;
            return this;
        }

        public ShowBuilder scenesLists(List<Scene> scenesLists) {
            this.scenesLists = scenesLists;
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
            IQLCStepExecutor executor = null;

            Show show = new Show(id == -1 ? idCount++ : id, this.name, this.nextExecutionTime, this.executing, executor,
                     0, this.stepList, this.scenesLists,
                    this.firstTimeExecution, this.function, new ArrayList<>());

            if (function instanceof QLCSequence)
                show.setStepExecutor(new QLCSequenceExecutor(show));

            if (function instanceof QLCEfx)
                show.setStepExecutor(new QLCEfxExecutor(show));

            if (function instanceof QLCScene)
                show.setStepExecutor(new QLCSceneExecutor(show));
            return show;
        }

        public Show build() {
            return build(-1);
        }

    }
}
