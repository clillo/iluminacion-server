package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.external.midi.KeyData;
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
        QLCButtonGroup qlcButtonGroup = createRows(1, 2, "Scene", "Derby", 0, 3,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW);
        createRows(1, 2, "Sequence", "Derby", 0, 2,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW, qlcButtonGroup);

        createRows(1, 6, "Sequence", "RGBW", 0, 1,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN);

        createRows(2, 3, "Scene", "Spider", 0, 7,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW);
        qlcButtonGroup = createRows(2, 4, "Scene", "Spider Positions", 0, 2,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED);
        createRows(2, 4, "Sequence", "Spiders/Multiple", 3, 1,
                KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, qlcButtonGroup);
        createRows(2, 4, "Sequence", "Spiders/Single", 6, 1,
                KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, qlcButtonGroup);

        createRows(3, 5, "Scene", "Moving Head Beam + Spot Color", 0, 7,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN);


    }

    private QLCButtonGroup createRows(final int panelId, final int groupId, final String type, final String path, int matrixX,
                                      int matrixY, final KeyData.StateLight onState, final KeyData.StateLight offState) {
        return createRows(panelId, groupId, type, path,  matrixX, matrixY, onState,  offState,null );
    }

    private QLCButtonGroup createRows(final int panelId, final int groupId, final String type, final String path, int matrixX,
                            int matrixY, final KeyData.StateLight onState, final KeyData.StateLight offState,
                                      QLCButtonGroup qlcButtonGroup ){
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

        if (blackoutScene==null)
            System.out.println(panelId+"\t"+groupId+"\tNo tiene off");

        if (qlcButtonGroup==null)
            qlcButtonGroup = new QLCButtonGroup(panelId, groupId, type, new ArrayList<>(), blackoutScene);

        final List<QLCButton> laserButtons = qlcButtonGroup.getButtonList();

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

        if (!buttonGroupMap.containsKey(panelId))
            buttonGroupMap.put(panelId, new ArrayList<>());

         buttonGroupMap.get(panelId).add(qlcButtonGroup);
        return qlcButtonGroup;
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
