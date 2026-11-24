package cl.clillo.lighting.external.midi;

import javax.sound.midi.ShortMessage;
import java.util.HashMap;
import java.util.Map;

public class ApcMiniLayout {

    private final Map<String, KeyData> keyDataByPos = new HashMap<>();
    private final Map<Integer, KeyData> noteKeys = new HashMap<>();
    private final Map<Integer, KeyData> sliderKeys = new HashMap<>();

    public ApcMiniLayout() {
        for (int matrixX = 0; matrixX < KeyData.MAX_X; matrixX++)
            for (int matrixY = 0; matrixY < KeyData.MAX_Y; matrixY++)
                addNoteKey(matrixX + "-" + matrixY, KeyData.buildMatrixButton(matrixX, matrixY));

        for (int posY = 0; posY < KeyData.MAX_Y; posY++)
            addNoteKey("Y-" + posY, KeyData.buildSideButton(posY));

        for (int posX = 0; posX < KeyData.MAX_X; posX++)
            addNoteKey("X-" + posX, KeyData.buildSliderButton(posX));

        for (int posX = 0; posX < KeyData.MAX_X + 1; posX++) {
            final KeyData keyData = KeyData.buildSlider(posX);
            keyDataByPos.put("SL-" + posX, keyData);
            sliderKeys.put(keyData.getChannel(), keyData);
        }

        addNoteKey("SHIFT", KeyData.buildShiftButton());
    }

    private void addNoteKey(final String pos, final KeyData keyData) {
        keyDataByPos.put(pos, keyData);
        noteKeys.put(keyData.getChannel(), keyData);
    }

    public KeyData matrixButton(final int matrixX, final int matrixY) {
        return keyDataByPos.get(matrixX + "-" + matrixY);
    }

    public KeyData sideButton(final int posY) {
        return keyDataByPos.get("Y-" + posY);
    }

    public KeyData sliderButton(final int posX) {
        return keyDataByPos.get("X-" + posX);
    }

    public KeyData shiftButton() {
        return keyDataByPos.get("SHIFT");
    }

    public KeyData fromIncoming(final ShortMessage message) {
        if (message.getCommand() == ShortMessage.CONTROL_CHANGE)
            return sliderKeys.get(message.getData1());
        return noteKeys.get(message.getData1());
    }
}
