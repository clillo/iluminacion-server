package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.midi.KeyData;
import cl.clillo.lighting.model.QLCFunction;
import cl.clillo.lighting.model.ShowCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MidiButtonFunctionRepository {

    private static final class InstanceHolder {
        private static final MidiButtonFunctionRepository instance = new MidiButtonFunctionRepository();
    }

    public static MidiButtonFunctionRepository getInstance() {
        return InstanceHolder.instance;
    }

    private final Map<String, QLCButton> buttonMap;
    private final Map<Integer, List<QLCButtonGroup>> buttonGroupMap;

    MidiButtonFunctionRepository(){
        buttonMap = new HashMap<>();
        buttonGroupMap = new HashMap<>();
        final List<QLCFunction> list = ShowCollection.getInstance().getFunctionList("Scene", "Laser");
        int[] showIds = new int[list.size()];
        int i=0;

        for (QLCFunction function: list){
            showIds[i++]=function.getId();
        }

        final List<QLCButton> laserButtons = new ArrayList<>();
        int matrixX=0;
        int matrixY=7;
        for (i=0; i<showIds.length; i++){
            QLCButton qlcButton = new QLCButton(matrixX, matrixY, ShowCollection.getInstance().getShow(showIds[i]),1,
                    KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, KeyData.StateLight.OFF);

            laserButtons.add(qlcButton);
            buttonMap.put(matrixX+"-"+matrixY, qlcButton);
            matrixX++;
            if (matrixX>7){
                matrixX=0;
                matrixY--;
            }
        }

        final QLCButtonGroup qlcButtonGroup = new QLCButtonGroup(1,1, "Laser", laserButtons, laserButtons.get(1).getShow().getFunction());
        buttonGroupMap.put(1, List.of(qlcButtonGroup));

    }

    public Map<String, QLCButton> getButtonMap() {
        return buttonMap;
    }

    public List<QLCButtonGroup> getButtonGroupMap(final int panel) {
        if (!buttonGroupMap.containsKey(panel))
            return List.of();

        return buttonGroupMap.get(panel);
    }
}
