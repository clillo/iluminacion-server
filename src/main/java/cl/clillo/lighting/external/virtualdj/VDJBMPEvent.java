package cl.clillo.lighting.external.virtualdj;

public interface VDJBMPEvent {

    void beat(boolean change, int pos, double bpm, double strength);

    void remoteIp(String ip);
}
