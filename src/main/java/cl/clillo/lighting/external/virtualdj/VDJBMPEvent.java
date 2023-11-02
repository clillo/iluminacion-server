package cl.clillo.lighting.external.virtualdj;

public interface VDJBMPEvent {

    void beat(boolean change, int pos, double bpm, double strength);

    void remoteIp(String ip);

    void command(int id, int param);

    void button(String name, String state);

    void beat(int beat);

    void beat();

    void beatX2();

    void beatX2(int beat);

    void beatX4();

    void beatX4(int beat);

    void beatX8();

    void beatX8(int beat);

    void beatX16();


}
