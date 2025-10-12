package cl.clillo.utilities;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

/**
 * Reproduce una CueList usando el DmxModel como destino.
 * Interpola valores (fade) con un Timer de swing (thread-safe con UI).
 *
 * Requisitos mínimos en DmxModel:
 *  - Map<Integer,Integer> getCurrentUniverseSnapshot()
 *  - int getChannelValue(int channel)
 *  - void setChannelValue(int channel, int value)
 *  - void flushIfNeeded()
 */
public class ChasePlayer {

    private final DmxModel model;
    private CueList cueList;
    private int index = -1;
    private Timer tickTimer;          // 40ms ~ 25fps
    private long phaseStartMillis;
    private Cue current;
    private Cue next;
    private boolean running;

    private static final int TICK_MS = 40;

    // Estado de fade
    private Map<Integer, Integer> startValues;
    private Map<Integer, Integer> targetValues;

    public ChasePlayer(DmxModel model) {
        this.model = model;
    }

    public void setCueList(CueList cueList) {
        stop();
        this.cueList = cueList;
    }

    public boolean isRunning() { return running; }

    public void start() {
        if (cueList == null || cueList.size() == 0 || running) return;
        running = true;
        index = -1;
        scheduleNext();
        tickTimer = new Timer(TICK_MS, new Tick());
        tickTimer.start();
    }

    public void stop() {
        running = false;
        if (tickTimer != null) {
            tickTimer.stop();
            tickTimer = null;
        }
        current = null;
        next = null;
    }

    public void nextCue() {
        if (cueList == null || cueList.size() == 0) return;
        index++;
        if (index >= cueList.size()) {
            if (cueList.loop()) {
                index = 0;
            } else {
                stop();
                return;
            }
        }
        current = next; // por si interesa mantener
        next = cueList.get(index);
        // preparar fade
        startValues = model.getCurrentUniverseSnapshot(); // channel -> val actual
        targetValues = next.channelValues();
        phaseStartMillis = System.currentTimeMillis();
    }

    private void scheduleNext() {
        nextCue();
    }

    private class Tick implements ActionListener {
        private boolean holding = false;
        private long holdStart = 0;

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!running || next == null) return;

            long now = System.currentTimeMillis();
            int fade = Math.max(0, next.fadeMs());
            if (!holding) {
                if (fade == 0) {
                    // salto directo
                    applySnapshot(targetValues);
                    holding = true;
                    holdStart = now;
                    return;
                }
                double t = Math.min(1.0, (now - phaseStartMillis) / (double) fade);
                applyInterpolated(t);
                if (t >= 1.0) {
                    holding = true;
                    holdStart = now;
                }
            } else {
                // hold
                if (now - holdStart >= next.holdMs()) {
                    holding = false;
                    scheduleNext();
                }
            }
        }
    }

    private void applyInterpolated(double t) {
        // Interpola por canal
        for (Map.Entry<Integer, Integer> entry : targetValues.entrySet()) {
            int ch = entry.getKey();
            int target = entry.getValue();
            int start = startValues.getOrDefault(ch, model.getChannelValue(ch));
            int val = (int) Math.round(start + (target - start) * t);
            model.setChannelValue(ch, clamp(val));
        }
        model.flushIfNeeded(); // opcional, depende de tu implementación
    }

    private void applySnapshot(Map<Integer, Integer> snap) {
        for (Map.Entry<Integer, Integer> entry : snap.entrySet()) {
            model.setChannelValue(entry.getKey(), clamp(entry.getValue()));
        }
        model.flushIfNeeded();
    }

    private int clamp(int v) {
        if (v < 0) return 0;
        if (v > 255) return 255;
        return v;
    }
}