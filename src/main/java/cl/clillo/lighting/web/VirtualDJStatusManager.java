package cl.clillo.lighting.web;

import cl.clillo.lighting.external.virtualdj.OS2LServer;
import cl.clillo.lighting.external.virtualdj.VDJBMPEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Mantiene un snapshot del estado de comunicación con VirtualDJ (OS2L).
 * <p>
 * Nota: Se registra como listener de {@link OS2LServer} de forma lazy para evitar fallas si el puerto ya está ocupado.
 */
public class VirtualDJStatusManager implements VDJBMPEvent {

    private static final int DEFAULT_OS2L_PORT = 4444;
    private static final long CONNECTED_THRESHOLD_MS = 10_000;

    private final AtomicBoolean registered = new AtomicBoolean(false);
    private final AtomicReference<String> registerError = new AtomicReference<>(null);

    private final AtomicReference<String> lastRemoteIp = new AtomicReference<>(null);
    private final AtomicLong lastEventAtMs = new AtomicLong(0);
    private final AtomicLong lastBeatAtMs = new AtomicLong(0);

    private volatile boolean lastBeatChange = false;
    private volatile int lastBeatPos = -1;
    private volatile double lastBeatBpm = 0.0;
    private volatile double lastBeatStrength = 0.0;

    private volatile int lastCommandId = -1;
    private volatile int lastCommandParam = -1;
    private volatile String lastButtonName = null;
    private volatile String lastButtonState = null;

    private void ensureRegistered() {
        if (registered.get() || registerError.get() != null) return;
        if (!registered.compareAndSet(false, true)) return;
        try {
            OS2LServer.getInstance().addListener(this);
        } catch (Throwable t) {
            registerError.set(t.getClass().getSimpleName() + ": " + (t.getMessage() == null ? "" : t.getMessage()));
        }
    }

    public Map<String, Object> getStatus() {
        ensureRegistered();

        long now = System.currentTimeMillis();
        String ip = lastRemoteIp.get();
        long lastSeen = lastEventAtMs.get();

        boolean connected = ip != null && lastSeen > 0 && (now - lastSeen) <= CONNECTED_THRESHOLD_MS;

        Map<String, Object> map = new HashMap<>();
        map.put("os2lPort", DEFAULT_OS2L_PORT);
        map.put("registered", registerError.get() == null); // true si pudo registrarse
        map.put("registerError", registerError.get());
        map.put("connected", connected);
        map.put("lastRemoteIp", ip);
        map.put("lastEventAtMs", lastSeen == 0 ? null : lastSeen);
        map.put("lastSeenAgoMs", lastSeen == 0 ? null : (now - lastSeen));

        Map<String, Object> lastBeat = new HashMap<>();
        lastBeat.put("atMs", lastBeatAtMs.get() == 0 ? null : lastBeatAtMs.get());
        lastBeat.put("change", lastBeatChange);
        lastBeat.put("pos", lastBeatPos);
        lastBeat.put("bpm", lastBeatBpm);
        lastBeat.put("strength", lastBeatStrength);
        map.put("lastBeat", lastBeat);

        Map<String, Object> lastCmd = new HashMap<>();
        lastCmd.put("id", lastCommandId);
        lastCmd.put("param", lastCommandParam);
        map.put("lastCommand", lastCmd);

        Map<String, Object> lastBtn = new HashMap<>();
        lastBtn.put("name", lastButtonName);
        lastBtn.put("state", lastButtonState);
        map.put("lastButton", lastBtn);

        return map;
    }

    private void touchEvent() {
        lastEventAtMs.set(System.currentTimeMillis());
    }

    @Override
    public void beat(boolean change, int pos, double bpm, double strength) {
        touchEvent();
        lastBeatAtMs.set(System.currentTimeMillis());
        lastBeatChange = change;
        lastBeatPos = pos;
        lastBeatBpm = bpm;
        lastBeatStrength = strength;
    }

    @Override
    public void remoteIp(String ip) {
        touchEvent();
        lastRemoteIp.set(ip);
    }

    @Override
    public void command(int id, int param) {
        touchEvent();
        lastCommandId = id;
        lastCommandParam = param;
    }

    @Override
    public void button(String name, String state) {
        touchEvent();
        lastButtonName = name;
        lastButtonState = state;
    }

    @Override public void beat(int beat) { touchEvent(); }
    @Override public void beat() { touchEvent(); }
    @Override public void beatX2() { touchEvent(); }
    @Override public void beatX2(int beat) { touchEvent(); }
    @Override public void beatX4() { touchEvent(); }
    @Override public void beatX4(int beat) { touchEvent(); }
    @Override public void beatX8() { touchEvent(); }
    @Override public void beatX8(int beat) { touchEvent(); }
    @Override public void beatX16() { touchEvent(); }
    @Override public void beatD2() { touchEvent(); }
    @Override public void beatD4() { touchEvent(); }
}


