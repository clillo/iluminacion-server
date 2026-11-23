package cl.clillo.lighting.external.midi;

import lombok.extern.slf4j.Slf4j;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MidiHandler {

    private static final String DEVICE_NAME = "APC";
    private static final long WATCH_INTERVAL_SECONDS = 2;

    private static final class InstanceHolder {
        private static final MidiHandler instance = new MidiHandler();
    }

    public static MidiHandler getInstance() {
        return InstanceHolder.instance;
    }

    private final ApcMiniLayout layout = new ApcMiniLayout();
    private final List<MidiConnectionListener> connectionListeners = new CopyOnWriteArrayList<>();
    private volatile MidiEvent midiEvent;

    private volatile MidiDevice inputDevice;
    private MidiDevice outputDevice;
    private volatile Receiver outputReceiver;

    private MidiHandler() {
        checkDevice();

        final ScheduledExecutorService watcher = Executors.newSingleThreadScheduledExecutor(runnable -> {
            final Thread thread = new Thread(runnable, "midi-device-watcher");
            thread.setDaemon(true);
            return thread;
        });
        watcher.scheduleWithFixedDelay(this::checkDevice, WATCH_INTERVAL_SECONDS, WATCH_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    public void setMidiEvent(final MidiEvent midiEvent) {
        this.midiEvent = midiEvent;
    }

    public void addConnectionListener(final MidiConnectionListener listener) {
        connectionListeners.add(listener);
    }

    public boolean isConnected() {
        return inputDevice != null;
    }

    public String getDeviceName() {
        final MidiDevice device = inputDevice;
        return device == null ? null : device.getDeviceInfo().getName();
    }

    private synchronized void checkDevice() {
        try {
            if (inputDevice == null)
                connect();
            else if (!Arrays.asList(MidiSystem.getMidiDeviceInfo()).contains(inputDevice.getDeviceInfo()))
                disconnect();
        } catch (RuntimeException e) {
            // an exception would cancel the scheduled watcher
            log.error("Error checking MIDI device", e);
        }
    }

    private void connect() {
        MidiDevice inputCandidate = null;
        MidiDevice outputCandidate = null;
        for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
            if (!info.getName().contains(DEVICE_NAME))
                continue;

            final MidiDevice device;
            try {
                device = MidiSystem.getMidiDevice(info);
            } catch (MidiUnavailableException e) {
                continue;
            }
            if (device instanceof Sequencer || device instanceof Synthesizer)
                continue;

            if (inputCandidate == null && device.getMaxTransmitters() != 0)
                inputCandidate = device;
            if (outputCandidate == null && device.getMaxReceivers() != 0)
                outputCandidate = device;
        }

        if (inputCandidate == null)
            return;

        final String name = inputCandidate.getDeviceInfo().getName();
        try {
            inputCandidate.open();
            inputCandidate.getTransmitter().setReceiver(new InputReceiver());
            if (outputCandidate != null) {
                outputCandidate.open();
                outputReceiver = outputCandidate.getReceiver();
            } else
                log.warn("{} has no MIDI output, pad lights will not be updated", name);
        } catch (MidiUnavailableException e) {
            log.warn("Could not open MIDI device {}", name, e);
            outputReceiver = null;
            inputCandidate.close();
            if (outputCandidate != null)
                outputCandidate.close();
            return;
        }

        inputDevice = inputCandidate;
        outputDevice = outputCandidate;
        log.info("MIDI device {} connected", name);

        clearLights();
        for (MidiConnectionListener listener : connectionListeners)
            listener.onMidiConnected(name);
    }

    private void disconnect() {
        final String name = inputDevice.getDeviceInfo().getName();
        outputReceiver = null;
        if (outputDevice != null)
            outputDevice.close();
        inputDevice.close();
        outputDevice = null;
        inputDevice = null;
        log.info("MIDI device {} disconnected", name);

        for (MidiConnectionListener listener : connectionListeners)
            listener.onMidiDisconnected();
    }

    private void clearLights() {
        for (int matrixX = 0; matrixX < KeyData.MAX_X; matrixX++)
            for (int matrixY = 0; matrixY < KeyData.MAX_Y; matrixY++)
                send(matrixX, matrixY, KeyData.StateLight.OFF);

        for (int posY = 0; posY < KeyData.MAX_Y; posY++)
            sendSide(posY, KeyData.StateLight.OFF);

        for (int posX = 0; posX < KeyData.MAX_X; posX++)
            sendSlider(posX, KeyData.StateLight.OFF);
    }

    public ShortMessage getShortMessage(final int matrixX, final int matrixY, final KeyData.StateLight stateLight) {
        return layout.matrixButton(matrixX, matrixY).getMessage(stateLight);
    }

    public void send(final int matrixX, final int matrixY, final KeyData.StateLight stateLight) {
        send(getShortMessage(matrixX, matrixY, stateLight));
    }

    public void sendSide(final int posY, final KeyData.StateLight stateLight) {
        send(layout.sideButton(posY).getMessage(stateLight));
    }

    public void sendSlider(final int posX, final KeyData.StateLight stateLight) {
        send(layout.sliderButton(posX).getMessage(stateLight));
    }

    public void send(final ShortMessage message) {
        final Receiver receiver = outputReceiver;
        if (receiver == null)
            return;
        try {
            receiver.send(message, -1);
        } catch (IllegalStateException e) {
            log.debug("MIDI output is closed", e);
        }
    }

    private final class InputReceiver implements Receiver {

        @Override
        public void send(final MidiMessage message, final long timeStamp) {
            final MidiEvent listener = midiEvent;
            if (listener == null || !(message instanceof ShortMessage shortMessage))
                return;

            final KeyData keyData = layout.fromIncoming(shortMessage);
            if (keyData == null) {
                log.debug("Unmapped MIDI message data1: {} data2: {} command: {}",
                        shortMessage.getData1(), shortMessage.getData2(), shortMessage.getCommand());
                return;
            }

            if (shortMessage.getCommand() == ShortMessage.CONTROL_CHANGE) {
                keyData.setValue(shortMessage.getData2() / 127.0);
                listener.onSlide(keyData);
            } else if (shortMessage.getCommand() == ShortMessage.NOTE_ON)
                listener.onKeyPress(keyData);
            else if (keyData.isShiftButton())
                listener.onKeyRelease(keyData);
        }

        @Override
        public void close() {
        }
    }
}
