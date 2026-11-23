package cl.clillo.lighting.external.midi;

public interface MidiConnectionListener {

    void onMidiConnected(String deviceName);

    void onMidiDisconnected();
}
