package cl.clillo.utilities;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** Grupo de canales/fixtures controlado por un submaster (0.0–1.0). */
public class FixtureGroup {
    private final String name;
    private final Set<Integer> channels = new HashSet<>();
    private double submaster = 1.0; // escala multiplicativa

    public FixtureGroup(String name) {
        this.name = name;
    }

    public String name() { return name; }
    public Set<Integer> channels() { return Collections.unmodifiableSet(channels); }

    public void addChannel(int ch) { channels.add(ch); }
    public void addChannels(Iterable<Integer> chs) { for (Integer c : chs) channels.add(c); }
    public void removeChannel(int ch) { channels.remove(ch); }

    public double submaster() { return submaster; }
    public void setSubmaster(double submaster) {
        this.submaster = Math.max(0.0, Math.min(1.0, submaster));
    }

    @Override
    public String toString() {
        return name + " (" + channels.size() + " ch, " + (int)(submaster*100) + "%)";
    }
}