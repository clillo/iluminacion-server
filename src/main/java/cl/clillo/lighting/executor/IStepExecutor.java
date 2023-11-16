package cl.clillo.lighting.executor;

import cl.clillo.lighting.external.dmx.Dmx;
import cl.clillo.lighting.model.QLCDirection;
import cl.clillo.lighting.model.QLCPoint;
import cl.clillo.lighting.model.QLCRunOrder;
import cl.clillo.lighting.model.QLCStep;
import cl.clillo.lighting.model.Show;

import java.util.List;

public abstract class IStepExecutor {

    private final Dmx dmx = Dmx.getInstance();
    protected final Show show;
    private final List<QLCStep> stepList;
    protected int actualStep;
    private final int totalSteps;
    protected QLCDirection direction;
    protected QLCRunOrder runOrder;

    protected IStepExecutor(final Show show, final List<QLCStep> stepList) {
        this.show = show;
        this.stepList = stepList;
        totalSteps = stepList.size()-1;
        direction = QLCDirection.FORWARD;
        runOrder = QLCRunOrder.LOOP;
    }

    public void executeDefaultScheduler(){
        if (show.isFirstTimeExecution()){
            actualStep = 0;
            show.setFirstTimeExecution(false);
        }
    }

    protected void preExecuteDefaultScheduler(){
        if (show.isFirstTimeExecution()){
            actualStep = 0;
            show.setFirstTimeExecution(false);
        }
    }

    protected void postExecuteDefaultScheduler(final QLCStep step){
        for (QLCPoint point: step.getPointList())
            dmx.send(point);

        if (direction==QLCDirection.FORWARD)
            forward();
        else
            backward();

    }

    public void executeOS2LScheduler(){}

    private void forward(){
        actualStep++;
        if (actualStep==totalSteps && runOrder == QLCRunOrder.PINGPONG){
            direction = QLCDirection.BACKWARD;
            show.setNextExecutionTime(System.currentTimeMillis());
            return;
        }

        if (actualStep>totalSteps){
            actualStep=0;
        }
    }

    private void backward(){
        actualStep--;

        if (actualStep==0 && runOrder == QLCRunOrder.PINGPONG){
            direction = QLCDirection.FORWARD;
            show.setNextExecutionTime(System.currentTimeMillis());
            return;
        }

        if (actualStep<0){
            actualStep=totalSteps;
        }
    }
}
