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

        QLCButtonGroup qlcButtonGroup = createRows(2, 2, "Scene", "Derby", 0, 7,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW);
        createRows(2, 2, "Sequence", "Derby", 0, 6,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW, qlcButtonGroup);

        qlcButtonGroup = createRows(3, 6, "Sequence", "RGBW", 0, 7,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN);
        createRows(3, 6, "Scene", "RGBW", 1, 6,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN, qlcButtonGroup);

        qlcButtonGroup = createRows(4, 3, "Scene", "Spider", 0, 7,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW);

        createRows(4, 3, "Sequence", "Spiders/Multiple", 5, 4,
                KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW, qlcButtonGroup);

        qlcButtonGroup = createRows(4, 7, "Scene", "Spider Positions", 0, 1,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED);

        createRows(4, 7, "QLCEfxLine", "QLCEfx Spiders", 4, 0,
                KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, qlcButtonGroup);

        createRows(5, 5, "Scene", "Moving Head Beam + Spot Color", 0, 7,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN);

        createRows(5, 8, "Scene", "Moving Head Beam + Spot Gobos", 0, 5,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN);

        createRows(5, 9, "Scene", "Moving Head Beam + Spot Gobos Prism", 3, 4,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN);

        createRows(6, 10, "Scene", "Moving Head Beam Color", 0, 7,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW);
        createRows(6, 11, "Scene", "Moving Head Beam Positions", 0, 5,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW);

        createRows(6, 10, "Scene", "Moving Head Spot", 0, 3,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED);
        createRows(6, 11, "Scene", "Moving Head Spot Positions", 0, 1,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED);

        createRows(7, 12, "Collection", "", 0, 7,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED);

        qlcButtonGroup = createRows(7, 13, "Collection", "Sec1", 0, 5,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED);

        createRows(7, 14, "Collection", "Sec2", 0, 3,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, qlcButtonGroup);

        createRows(7, 14, "Collection", "Sec 3", 0, 1,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, qlcButtonGroup);

        consistencyCheck();
    }

    private void consistencyCheck(){
        for (Map.Entry<Integer, List<QLCButtonGroup>> panel: buttonGroupMap.entrySet()){

            for (QLCButtonGroup buttonGroup: panel.getValue()) {
                QLCScene blackoutScene = null;
                if (buttonGroup.getGlobalOff()!=null)
                    blackoutScene = buttonGroup.getGlobalOff();

                if (blackoutScene == null)
                    System.out.println("Panel: " + panel.getKey() + " buttonGroup "+ buttonGroup.getId() + " doesn't have blackout scene");
            }
        }
    }

    private QLCButtonGroup createRows(final int panelId, final int groupId, final String type, final String path, int matrixX,
                                      int matrixY, final KeyData.StateLight onState, final KeyData.StateLight offState) {
        return createRows(panelId, groupId, type, path,  matrixX, matrixY, onState,  offState,null );
    }

    private int[] getShowsId(final List<QLCFunction> list){
        final List<QLCFunction> finalList = new ArrayList<>();
        for (QLCFunction function: list){
            if(!function.isBlackout())
                finalList.add(function);
        }

        int[] showIds = new int[finalList.size()];
        int i=0;
        for (QLCFunction function: finalList)
            showIds[i++]=function.getId();

        return showIds;
    }

    private QLCButtonGroup createRows(final int panelId, final int groupId, final String type, final String path, int matrixX,
                            int matrixY, final KeyData.StateLight onState, final KeyData.StateLight offState,
                                      QLCButtonGroup qlcButtonGroup ){
        final List<QLCFunction> list = ShowCollection.getInstance().getFunctionList(type, path);
        int[] showIds = getShowsId(list);
        int i=0;

        QLCScene blackoutScene = null;
        for (QLCFunction function: list){
            if(function.isBlackout())
                blackoutScene = (QLCScene)function;
        }

        if (qlcButtonGroup==null)
            qlcButtonGroup = new QLCButtonGroup(panelId, groupId, type, new ArrayList<>());

        if (qlcButtonGroup.getGlobalOff()==null && blackoutScene!=null)
            qlcButtonGroup.setGlobalOff(blackoutScene);

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
