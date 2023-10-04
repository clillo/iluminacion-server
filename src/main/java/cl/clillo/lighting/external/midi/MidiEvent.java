package cl.clillo.lighting.external.midi;

public interface MidiEvent {

    void onKeyPress(KeyData keyData);

    void onKeyRelease(KeyData keyData);

    void onSlide(KeyData keyData);
}
