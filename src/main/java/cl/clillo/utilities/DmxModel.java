package cl.clillo.utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class DmxModel {
    private static final int UNIVERSE_SIZE = 8;
    private final int[] universe = new int[UNIVERSE_SIZE];
    private final List<FixtureGroup> groups = new ArrayList<>();
    private final List<Integer> values = new ArrayList<>();
    private final List<String> descriptions = new ArrayList<>();
    private final List<ModelListener> listeners = new ArrayList<>();

    public DmxModel(int channels) {
        resize(channels);
    }

    public int size() { return values.size(); }

    public int get(int index) {
        return values.get(index);
    }

    public void set(int index, int value) {
        int v = clamp(value);
        if (!values.get(index).equals(v)) {
            values.set(index, v);
            fireChanged(index, v);
        }
    }

    public void resize(int channels) {
        channels = Math.min(512, Math.max(1, channels));
        int old = values.size();
        if (channels > old) {
            for (int i = old; i < channels; i++) {
                values.add(0);
                descriptions.add("Canal " + (i + 1)); // <--- default
            }
        } else if (channels < old) {
            values.subList(channels, old).clear();
            descriptions.subList(channels, old).clear();
        }
    }

    public void setDescriptions(String[] descs) {
        for (int i = 0; i < size(); i++) {
            String d = (descs != null && i < descs.length && descs[i] != null && !descs[i].isBlank())
                    ? descs[i]
                    : "Canal " + (i + 1);
            descriptions.set(i, d);
        }
    }

    public String getDescription(int index) {
        return descriptions.get(index);
    }

    public void resetAll() {
        Collections.fill(values, 0);
        fireAllChanged();
    }

    public void randomizeAll(long seed) {
        Random r = (seed == 0 ? new Random() : new Random(seed));
        values.replaceAll(ignored -> r.nextInt(256));
        fireAllChanged();
    }

    public String toCsv() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(values.get(i));
        }
        return sb.toString();
    }

    public String toSpaceSeparated() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(' ');
            sb.append(values.get(i));
        }
        return sb.toString();
    }

    public void addListener(ModelListener l) { listeners.add(l); }

    private void fireChanged(int index, int newVal) {
        for (ModelListener l : List.copyOf(listeners))
            l.valueChanged(index, newVal);
    }

    private void fireAllChanged() {
        for (ModelListener l : List.copyOf(listeners))
            for (int i = 0; i < values.size(); i++)
                l.valueChanged(i, values.get(i));
    }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    public interface ModelListener {
        void valueChanged(int index, int newValue);
    }

    // --- DMX Addons hooks ---
    public java.util.Map<Integer,Integer> getCurrentUniverseSnapshot() {
        java.util.Map<Integer,Integer> map = new java.util.HashMap<>();
        for (int i = 0; i < UNIVERSE_SIZE; i++) {
            if (universe[i] != 0) map.put(i+1, universe[i]); // canales 1..512
        }
        return map;
    }


    public int getChannelValue(int channel) {
        if (channel < 1 || channel > UNIVERSE_SIZE) return 0;
        return universe[channel - 1];
    }


    public void setChannelValue(int channel, int value) {
        if (channel < 1 || channel > UNIVERSE_SIZE) return;
        int base = Math.max(0, Math.min(255, value));
        double scale = computeGroupScaleForChannel(channel);
        int scaled = (int)Math.round(base * scale);
        universe[channel - 1] = Math.max(0, Math.min(255, scaled));
        // aquí puedes notificar listeners / repintar paneles
    }


    public void flushIfNeeded() {
        // no-op por defecto. Si tienes salida DMX real, envíala aquí.
    }


    public void addGroup(FixtureGroup g) { groups.add(g); }


    public void removeGroup(FixtureGroup g) { groups.remove(g); }


    public java.util.List<FixtureGroup> getGroups() { return java.util.Collections.unmodifiableList(groups); }


    public void setGroups(java.util.List<FixtureGroup> gs) {
        groups.clear();
        if (gs != null) groups.addAll(gs);
    }


    private double computeGroupScaleForChannel(int ch) {
        double scale = 1.0;
        for (FixtureGroup g : groups) {
            if (g.channels().contains(ch)) scale *= g.submaster();
        }
        return scale;
    }
}
