package cl.clillo.utilities;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class UISettings {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private double scale = 1.0;      // 1.0 = 100%
    private boolean compact = true;  // modo compacto por defecto

    public double getScale() { return scale; }
    public void setScale(double scale) {
        double s = Math.max(0.6, Math.min(2.0, scale));
        double old = this.scale;
        if (Double.compare(old, s) != 0) {
            this.scale = s;
            pcs.firePropertyChange("scale", old, s);
        }
    }

    public boolean isCompact() { return compact; }

    public void setCompact(boolean compact) {
        boolean old = this.compact;
        if (old != compact) {
            this.compact = compact;
            pcs.firePropertyChange("compact", old, compact);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener l) { pcs.addPropertyChangeListener(l); }

    public void removePropertyChangeListener(PropertyChangeListener l) { pcs.removePropertyChangeListener(l); }
}
