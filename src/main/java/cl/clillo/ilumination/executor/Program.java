package cl.clillo.ilumination.executor;

import cl.clillo.ilumination.model.Step;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Program {

    private static final long NEXT_EXECUTION_DEFAULT = 3000;

    private String name;
    private long nextExecutionTime;
    private boolean executing;
    private StepExecutor stepExecutor;
    private TipoGatillador tipoGatillador;

    private int pasoActual;
    private List<Step> stepList;

    public Step nextStep(){
        pasoActual++;
        if (pasoActual >= stepList.size())
            pasoActual = 0;

        final Step step = stepList.get(pasoActual);

        if (step.isNextExecution()) {
            nextExecutionTime = System.currentTimeMillis() + step.getNextExecutionTime();
        }else {
            nextExecutionTime = System.currentTimeMillis() + NEXT_EXECUTION_DEFAULT;
        }
        return step;
    }

}
