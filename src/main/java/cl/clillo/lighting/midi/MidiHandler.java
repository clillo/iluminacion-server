package cl.clillo.lighting.midi;

import org.springframework.boot.autoconfigure.session.SessionRepositoryUnavailableException;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MidiHandler {

    private final Map<String, KeyData> keyDataMapByPos;
    private MidiDevice mainMidiDevice;

    private static final class InstanceHolder {
        private static MidiHandler instance;

        public static MidiHandler getInstance(final MidiEvent midiEvent) {
            if (instance==null)
                instance = new MidiHandler(midiEvent);
            return instance;
        }

        public static MidiHandler getInstance() {
            if (instance==null)
                throw new RuntimeException("Not implemented");
            return instance;
        }
    }

    public static MidiHandler getInstance(final MidiEvent midiEvent) {
        return InstanceHolder.getInstance(midiEvent);
    }

    public static MidiHandler getInstance() {
        return InstanceHolder.getInstance();
    }

    private MidiHandler(final MidiEvent midiEvent) {
        keyDataMapByPos = new HashMap<>();
        Map<String, KeyData> keyDataMapByChannel = new HashMap<>();

        for (int matrixX=0; matrixX<KeyData.MAX_X; matrixX++)
            for (int matrixY=0; matrixY<KeyData.MAX_Y; matrixY++) {
                final KeyData keyData = KeyData.buildMatrixButton(matrixX, matrixY);
                keyDataMapByPos.put(matrixX + "-" + matrixY, keyData);
                keyDataMapByChannel.put("M-"+keyData.getChannel(), keyData);
            }

        for (int posY=0; posY<KeyData.MAX_Y; posY++)  {
            final KeyData keyData = KeyData.buildSideButton(posY);
            keyDataMapByPos.put("Y-"+posY, keyData);
            keyDataMapByChannel.put("M-"+keyData.getChannel(), keyData);

        }

        for (int posX=0; posX<KeyData.MAX_X; posX++)  {
            final KeyData keyData = KeyData.buildSliderButton(posX);
            keyDataMapByPos.put("X-"+posX, keyData);
            keyDataMapByChannel.put("M-"+keyData.getChannel(), keyData);
        }

        for (int posX=0; posX<KeyData.MAX_X+1; posX++)  {
            final KeyData keyData = KeyData.buildSlider(posX);
            keyDataMapByPos.put("SL-"+posX, keyData);
            keyDataMapByChannel.put("SL-"+keyData.getChannel(), keyData);

        }

        final KeyData keyData = KeyData.buildShiftButton();
        keyDataMapByPos.put("SHIFT", keyData);
        keyDataMapByChannel.put("M-"+keyData.getChannel(), keyData);

        MidiDevice device;
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();

        for (MidiDevice.Info info : infos) {
            try {
                device = MidiSystem.getMidiDevice(info);
                if (!"APC MINI".equals(info.getName()))
                    continue;

                List<Transmitter> transmitters = device.getTransmitters();

                for (Transmitter transmitter : transmitters) {
                    transmitter.setReceiver(
                            new MidiInputReceiver(device.getDeviceInfo().toString(), keyDataMapByChannel, midiEvent)
                    );
                }

                Transmitter trans = device.getTransmitter();
                trans.setReceiver(new MidiInputReceiver(device.getDeviceInfo().toString(), keyDataMapByChannel, midiEvent));

                mainMidiDevice = device;

            } catch (MidiUnavailableException e) {
                //e.printStackTrace();
            }
        }

        try {
            if (mainMidiDevice==null)
                return;
            mainMidiDevice.open();

            System.out.println(mainMidiDevice.getDeviceInfo()+" Was Opened");

            for (int i=0; i<KeyData.MAX_X; i++)
                for (int j=0; j<KeyData.MAX_X; j++)
                    this.send(i,j, KeyData.StateLight.OFF);
        } catch (MidiUnavailableException e) {
            throw new RuntimeException(e);
        }

    }

    public ShortMessage getShortMessage(final int matrixX, final int matrixY, final KeyData.StateLight stateLight){
        return keyDataMapByPos.get(matrixX+"-"+matrixY).getMessage(stateLight);
    }

    public void send(final int matrixX, final int matrixY, final KeyData.StateLight stateLight) {
        final Receiver receiver;
        try {
            receiver = MidiSystem.getReceiver();
        } catch (MidiUnavailableException e) {
            throw new RuntimeException(e);
        }
        final ShortMessage myMsg = keyDataMapByPos.get(matrixX+"-"+matrixY).getMessage(stateLight);
        receiver.send(myMsg, -1);

    }

    public void send(final ShortMessage myMsg) {
        final Receiver receiver;
        try {
            receiver = MidiSystem.getReceiver();
        } catch (MidiUnavailableException e) {
            throw new RuntimeException(e);
        }
        receiver.send(myMsg, -1);

    }

    public void sendSide(final int posY, final KeyData.StateLight stateLight) {
        final Receiver receiver;
        try {
            receiver = MidiSystem.getReceiver();
        } catch (MidiUnavailableException e) {
            throw new RuntimeException(e);
        }
        final ShortMessage myMsg = keyDataMapByPos.get("Y-"+posY).getMessage(stateLight);
        receiver.send(myMsg, -1);

    }

    public static class MidiInputReceiver implements Receiver {
        private final String name;
        private final Map<String, KeyData> keyDataMapByChannel;
        private final MidiEvent midiEvent;

        public MidiInputReceiver(final String name, final Map<String, KeyData> keyDataMapByChannel, final MidiEvent midiEvent) {
            this.name = name;
            this.keyDataMapByChannel = keyDataMapByChannel;
            this.midiEvent = midiEvent;
        }

        public void send(MidiMessage msg, long timeStamp) {
            if (msg instanceof ShortMessage) {
                final ShortMessage shortMessage = (ShortMessage) msg;
                final String prefix =shortMessage.getCommand() == 176?"SL-": "M-";
                final KeyData keyData = keyDataMapByChannel.get(prefix+ shortMessage.getData1());

                if (keyData!=null) {
                    // slider
                    if (shortMessage.getCommand() == 176) {
                        keyData.setValue(shortMessage.getData2()/127.0);
                        midiEvent.onSlide(keyData);
                        return;
                    }
                    if (shortMessage.getCommand() == 144)
                        midiEvent.onKeyPress(keyData);
                    else
                        if (keyData.isShiftButton())
                            midiEvent.onKeyRelease(keyData);

                    return;
                }

                System.out.println("data1: " + shortMessage.getData1() +
                        "\tdata2: "+shortMessage.getData2()+
                        "\tcommand: "+shortMessage.getCommand());


            }
        }
        public void close() {}
    }
}