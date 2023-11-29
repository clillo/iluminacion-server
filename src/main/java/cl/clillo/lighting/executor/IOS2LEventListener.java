package cl.clillo.lighting.executor;

public interface IOS2LEventListener {

    enum Type{UNIVERSAL, BEAT_D_4, BEAT_D_2, BEAT_X_1, BEAT_X_2, BEAT_X_4, BEAT_X_8, BEAT_X_16}

    void changeBPM(double bpm);

    void changeTimes(long time, long timex2);

    void pos(int pos);
}
