package cl.clillo.lighting.model;

import cl.clillo.lighting.executor.QLCEfxExecutor;
import cl.clillo.lighting.executor.QLCSequenceExecutor;
import cl.clillo.lighting.config.scenes.Scene;
import cl.clillo.lighting.executor.StepExecutor;
import cl.clillo.lighting.executor.TipoGatillador;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Show {

    private static final long NEXT_EXECUTION_DEFAULT = 3000;

    private int id;
    private String name;
    private long nextExecutionTime;
    private boolean executing;
    private StepExecutor stepExecutor;
    private TipoGatillador tipoGatillador;
    private int pasoActual;
    private List<Step> stepList;
    private List<Scene> scenesLists;
    private boolean firstTimeExecution;
    private QLCFunction function;

    public <T extends QLCFunction> T getFunction() {
        return (T)function;
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

    public void setNextExec() {
        nextExecutionTime = System.currentTimeMillis() + NEXT_EXECUTION_DEFAULT;
    }

    public static class ShowBuilder {
        private int id;
        private String name;
        private long nextExecutionTime;
        private boolean executing;
        private TipoGatillador tipoGatillador;
        private int pasoActual;
        private List<Step> stepList;
        private List<Scene> scenesLists;
        private boolean firstTimeExecution;
        private QLCFunction function;

        ShowBuilder() {
        }

        public ShowBuilder id(int id) {
            this.id = id;
            return this;
        }

        public ShowBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ShowBuilder nextExecutionTime(long nextExecutionTime) {
            this.nextExecutionTime = nextExecutionTime;
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

        public Show build() {
            StepExecutor executor = null;

            if (function instanceof QLCSequence)
                executor = new QLCSequenceExecutor();

            if (function instanceof QLCEfx)
                executor = new QLCEfxExecutor();

            return new Show(this.id, this.name, this.nextExecutionTime, this.executing, executor,
                    this.tipoGatillador, this.pasoActual, this.stepList, this.scenesLists, this.firstTimeExecution, this.function);
        }

    }
}
