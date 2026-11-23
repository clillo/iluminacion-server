package cl.clillo.lighting.external.dmx;

public interface ArtNetConnectionListener {

    void onArtNetConnected(String nodeAddress);

    void onArtNetDisconnected();
}
