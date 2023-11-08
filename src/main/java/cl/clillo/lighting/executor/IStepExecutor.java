package cl.clillo.lighting.executor;

public interface IStepExecutor {

    void executeDefaultScheduler();

    default void executeOS2LScheduler(){}
}
