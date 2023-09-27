package cl.clillo.lighting.midi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

public class KeyData {

    public static final int MAX_X = 8;
    public static final int MAX_Y = 8;
    private static final int[][] channelMatrix = new int[MAX_X][MAX_Y];

    public enum MidiInputType{MATRIX_BUTTON, SIDE_BUTTON, SHIFT_BUTTON, SLIDER_BUTTON, SLIDER}

    public enum StateLight{
        OFF(0),
        GREEN(1),
        GREEN_BLINK(2),
        RED(3),
        RED_BLINK(4),
        YELLOW(5),
        YELLOW_BLINK(6);

        private final int value;
        StateLight(int i) {
            this.value = i;
        }

        public int getValue() {
            return value;
        }
    }

    private int channel;
    private int matrixX, matrixY;
    private int posX;
    private int posY;
    private double value;

    private MidiInputType midiInputType;
    private ShortMessage shortMessageOff;
    private ShortMessage shortMessageGreen;
    private ShortMessage shortMessageGreenBlink;
    private ShortMessage shortMessageRed;
    private ShortMessage shortMessageRedBlink;
    private ShortMessage shortMessageYellow;
    private ShortMessage shortMessageYellowBlink;

    static {
        int index = 0;
        for (int j=0; j<MAX_X; j++)
            for (int i=0; i<MAX_X; i++)
                channelMatrix[i][j] = index++;
    }

    public static KeyData buildMatrixButton(final int matrixX, final int matrixY){
        final KeyData keyData = new KeyData();
        keyData.midiInputType = MidiInputType.MATRIX_BUTTON;
        keyData.matrixX = matrixX;
        keyData.matrixY = matrixY;
        keyData.channel = channelMatrix[matrixX][matrixY];
        buildShortMessage(keyData);

        return keyData;
    }

    public static KeyData buildSideButton(final int posY){
        final KeyData keyData = new KeyData();
        keyData.midiInputType = MidiInputType.SIDE_BUTTON;
        keyData.posY = posY;
        keyData.channel = 89 - posY;
        buildShortMessage(keyData);

        return keyData;
    }

    public static KeyData buildSliderButton(final int posX){
        final KeyData keyData = new KeyData();
        keyData.midiInputType = MidiInputType.SLIDER_BUTTON;
        keyData.posX = posX;
        keyData.channel = 64 + posX;
        buildShortMessage(keyData);

        return keyData;
    }

    public static KeyData buildSlider(final int posX){
        final KeyData keyData = new KeyData();
        keyData.midiInputType = MidiInputType.SLIDER;
        keyData.posX = posX;
        keyData.channel = 48 + posX;
        buildShortMessage(keyData);

        return keyData;
    }

    private static void buildShortMessage(final KeyData keyData) {
        keyData.shortMessageOff = buildShortMessage(keyData.channel, StateLight.OFF);
        keyData.shortMessageGreen = buildShortMessage(keyData.channel, StateLight.GREEN);
        keyData.shortMessageGreenBlink = buildShortMessage(keyData.channel, StateLight.GREEN_BLINK);
        keyData.shortMessageRed = buildShortMessage(keyData.channel, StateLight.RED);
        keyData.shortMessageRedBlink = buildShortMessage(keyData.channel, StateLight.RED_BLINK);
        keyData.shortMessageYellow = buildShortMessage(keyData.channel, StateLight.YELLOW);
        keyData.shortMessageYellowBlink = buildShortMessage(keyData.channel, StateLight.YELLOW_BLINK);
    }

    public static KeyData buildShiftButton(){
        final KeyData keyData = new KeyData();
        keyData.midiInputType = MidiInputType.SHIFT_BUTTON;
        keyData.channel = 98;

        keyData.shortMessageOff = buildShortMessage(keyData.channel, StateLight.OFF);
        keyData.shortMessageGreen = buildShortMessage(keyData.channel, StateLight.GREEN);
        keyData.shortMessageGreenBlink = buildShortMessage(keyData.channel, StateLight.GREEN_BLINK);
        keyData.shortMessageRed = buildShortMessage(keyData.channel, StateLight.RED);
        keyData.shortMessageRedBlink = buildShortMessage(keyData.channel, StateLight.RED_BLINK);
        keyData.shortMessageYellow = buildShortMessage(keyData.channel, StateLight.YELLOW);
        keyData.shortMessageYellowBlink = buildShortMessage(keyData.channel, StateLight.YELLOW_BLINK);

        return keyData;
    }

    private static ShortMessage buildShortMessage(final int channel, final StateLight stateLight){
        final ShortMessage myMsg = new ShortMessage();
        try {
            myMsg.setMessage(ShortMessage.NOTE_ON, 0, channel, stateLight.getValue());
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }
        return myMsg;
    }

    public int getChannel() {
        return channel;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public boolean isShiftButton(){
        return this.midiInputType == MidiInputType.SHIFT_BUTTON;
    }

    @Override
    public String toString() {
        if (midiInputType == MidiInputType.MATRIX_BUTTON)
            return "Button: "+matrixX+", "+matrixY;

        if (midiInputType == MidiInputType.SIDE_BUTTON)
            return "Side Button: "+ posY;

        if (midiInputType == MidiInputType.SLIDER_BUTTON)
            return "Slider Button: "+ posX;

        if (midiInputType == MidiInputType.SHIFT_BUTTON)
            return "Shift Button";

        if (midiInputType == MidiInputType.SLIDER)
            return "Slider: "+ posX+" : "+value;
        return "other";
    }

    public int getValue(){

        if (midiInputType == MidiInputType.SIDE_BUTTON)
            return posY;

        if (midiInputType == MidiInputType.SLIDER_BUTTON)
            return  posX;

        return -1;
    }

    public ShortMessage getMessage(final StateLight stateLight){
        switch (stateLight){
            case OFF:
                return shortMessageOff;
            case GREEN:
                return shortMessageGreen;
            case GREEN_BLINK:
                return shortMessageGreenBlink;
            case RED:
                return shortMessageRed;
            case RED_BLINK:
                return shortMessageRedBlink;
            case YELLOW:
                return shortMessageYellow;
            case YELLOW_BLINK:
                return shortMessageYellowBlink;
        }
        return null;
    }

    public MidiInputType getMidiInputType() {
        return midiInputType;
    }

    public int getMatrixX() {
        return matrixX;
    }

    public int getMatrixY() {
        return matrixY;
    }
}
