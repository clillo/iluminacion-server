package cl.clillo.utilities;

import java.util.Map;

/**
 * Un Cue es un snapshot con tiempos de transición.
 * channelValues: mapa channelId -> valor (0-255)
 */
public class Cue {
    private final String id;
    private final String name;
    private final Map<Integer, Integer> channelValues;
    private final int fadeMs; // tiempo de fundido hacia este cue
    private final int holdMs; // tiempo de permanencia antes del siguiente

    public Cue(String id, String name, Map<Integer, Integer> channelValues, int fadeMs, int holdMs) {
        this.id = id;
        this.name = name;
        this.channelValues = Map.copyOf(channelValues);
        this.fadeMs = Math.max(0, fadeMs);
        this.holdMs = Math.max(0, holdMs);
    }

    public String id() { return id; }
    public String name() { return name; }
    public Map<Integer, Integer> channelValues() { return channelValues; }
    public int fadeMs() { return fadeMs; }
    public int holdMs() { return holdMs; }

    @Override
    public String toString() {
        return name + " (fade " + fadeMs + "ms, hold " + holdMs + "ms)";
    }
}