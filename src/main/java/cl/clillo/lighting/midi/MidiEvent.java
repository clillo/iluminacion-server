package cl.clillo.lighting.midi;

public interface MidiEvent {

    void onKeyPress(KeyData keyData);

    void onKeyRelease(KeyData keyData);

    void onSlide(KeyData keyData);
}
