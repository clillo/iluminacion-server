package cl.clillo.lighting.executor;

public interface IOS2LEventListener {

    enum Type{UNIVERSAL, BEAT_X_1, BEAT_X_2, BEAT_X_4}

    void changeBPM(double bpm);

    void changeTimes(long time, long timex2);

    void pos(int pos);
}
