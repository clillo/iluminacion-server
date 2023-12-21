package cl.clillo.lighting.executor;

import cl.clillo.lighting.model.Show;

public interface ChaserExecutorShowListener {

    void startExecuting(Show show);

    void stopExecuting(Show show);
}
