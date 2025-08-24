package cl.clillo.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class DmxModel {

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
        fireStructureChanged();
    }

    // --- NUEVO: setear descripciones desde un arreglo (más cómodo para ti)
    public void setDescriptions(String[] descs) {
        for (int i = 0; i < size(); i++) {
            String d = (descs != null && i < descs.length && descs[i] != null && !descs[i].isBlank())
                    ? descs[i]
                    : "Canal " + (i + 1);
            descriptions.set(i, d);
        }
        fireMetadataChanged(); // notifica a la UI que cambió el texto de títulos
    }

    // --- NUEVO: obtener descripción
    public String getDescription(int index) {
        return descriptions.get(index);
    }

    public void resetAll() {
        for (int i = 0; i < values.size(); i++) values.set(i, 0);
        fireAllChanged();
    }

    public void randomizeAll(long seed) {
        Random r = (seed == 0 ? new Random() : new Random(seed));
        for (int i = 0; i < values.size(); i++) values.set(i, r.nextInt(256));
        fireAllChanged();
    }

    public byte[] toByteArray() {
        byte[] b = new byte[values.size()];
        for (int i = 0; i < values.size(); i++) b[i] = (byte)(values.get(i) & 0xFF);
        return b;
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

    public void removeListener(ModelListener l) { listeners.remove(l); }

    private void fireChanged(int index, int newVal) {
        for (ModelListener l : List.copyOf(listeners)) l.valueChanged(index, newVal);
    }

    private void fireAllChanged() {
        for (ModelListener l : List.copyOf(listeners)) l.allValuesChanged();
    }

    private void fireStructureChanged() {
        for (ModelListener l : List.copyOf(listeners)) l.structureChanged();
    }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    public interface ModelListener {
        void valueChanged(int index, int newValue);
        void allValuesChanged();
        void structureChanged();

        default void metadataChanged() {}
    }

    private void fireMetadataChanged() {
        for (ModelListener l : List.copyOf(listeners)) l.metadataChanged();
    }
}
