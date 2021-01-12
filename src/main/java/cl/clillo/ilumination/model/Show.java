package cl.clillo.ilumination.model;

import cl.clillo.ilumination.config.scenes.Scene;
import cl.clillo.ilumination.executor.StepExecutor;
import cl.clillo.ilumination.executor.TipoGatillador;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Show {

    private static final long NEXT_EXECUTION_DEFAULT = 3000;

    private int id;

    private String name;
    @JsonIgnore
    private long nextExecutionTime;
    @JsonIgnore
    private boolean executing;
    @JsonIgnore
    private StepExecutor stepExecutor;
    @JsonIgnore
    private TipoGatillador tipoGatillador;

    @JsonIgnore
    private int pasoActual;
    @JsonIgnore
    private List<Step> stepList;

    private List<Scene> scenesLists;

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
