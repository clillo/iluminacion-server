package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.midi.KeyData;
import cl.clillo.lighting.model.QLCFunction;
import cl.clillo.lighting.model.QLCScene;
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

        createRows(1, 1, "Scene", "Laser", 0, 7,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED);
        createRows(1, 2, "Scene", "Derby", 0, 3,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW);

        createRows(2, 3, "Scene", "Spider", 0, 7,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW);
        createRows(2, 4, "Scene", "Spider Positions", 0, 2,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED);


    }

    private void createRows(final int panelId, final int groupId, final String type, final String path, int matrixX,
                            int matrixY, final KeyData.StateLight onState, final KeyData.StateLight offState){
        final List<QLCFunction> list = ShowCollection.getInstance().getFunctionList(type, path);
        int[] showIds = new int[list.size()];
        int i=0;

        QLCScene blackoutScene = null;
        for (QLCFunction function: list){
            if(!function.isBlackout())
                showIds[i++]=function.getId();
            else
                blackoutScene = (QLCScene)function;
        }

        final List<QLCButton> laserButtons = new ArrayList<>();

        for (i=0; i<showIds.length; i++){
            QLCButton qlcButton = new QLCButton(matrixX, matrixY, ShowCollection.getInstance().getShow(showIds[i]),1,
                    onState, offState, KeyData.StateLight.OFF);

            laserButtons.add(qlcButton);
            buttonMap.put(matrixX+"-"+matrixY, qlcButton);
            matrixX++;
            if (matrixX>7){
                matrixX=0;
                matrixY--;
            }
        }

        final QLCButtonGroup qlcButtonGroup = new QLCButtonGroup(panelId, groupId, type, laserButtons, blackoutScene);

        if (!buttonGroupMap.containsKey(panelId))
            buttonGroupMap.put(panelId, new ArrayList<>());

         buttonGroupMap.get(panelId).add(qlcButtonGroup);
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
