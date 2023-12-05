package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.external.midi.KeyData;
import cl.clillo.lighting.model.QLCFunction;
import cl.clillo.lighting.model.QLCScene;
import cl.clillo.lighting.model.ShowCollection;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class MidiButtonFunctionRepository {

    private static final class InstanceHolder {
        private static final MidiButtonFunctionRepository instance = new MidiButtonFunctionRepository();
    }

    public static MidiButtonFunctionRepository getInstance() {
        return InstanceHolder.instance;
    }

    private final Map<String, PageConfig> buttonGroupMapByCategory;

    MidiButtonFunctionRepository(){
        buttonGroupMapByCategory = new HashMap<>();

        createRows( 1, "Scene", "Laser", 0, 7,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, "Laser");

        ButtonGroup buttonGroup = createRows( 2, "Scene", "Derby", 0, 7,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW, "Derby");
        createRows( 2, "Sequence", "Derby", 0, 6,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW, buttonGroup, "Derby");

        buttonGroup = createRows( 6, "Sequence", "RGBW", 0, 7,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN, "RGBW");
        createRows( 6, "Scene", "RGBW", 0, 5,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN, buttonGroup, "RGBW");

        buttonGroup = createRows( 3, "Scene", "Spider", 0, 7,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW, "Spider");

        createRows( 3, "Sequence", "Spiders/Multiple", 5, 4,
                KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW, buttonGroup, "Spider");

        buttonGroup = createRows( 7, "Scene", "Spider Positions", 0, 1,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, "Spider");

        createRows( 7, "QLCEfxLine", "QLCEfx Spiders", 4, 0,
                KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, buttonGroup, "Spider");

        createRows( 5, "Scene", "Moving Head Beam + Spot Color", 0, 7,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN, "MHead Spot + Beam");
        createRows( 8, "Scene", "Moving Head Beam + Spot Gobos", 0, 5,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN, "MHead Spot + Beam");
        createRows( 9, "Scene", "Moving Head Beam + Spot Gobos Prism", 0, 3,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN, "MHead Spot + Beam");
        buttonGroup = createRows( 10, "Scene", "Moving Head Beam + Spot Positions", 0, 2,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN, "MHead Spot + Beam");
        createRows( 10, "EfxMultiLine", "Moving Head Beam + Spot EFX", 0, 1,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW, buttonGroup,"MHead Spot + Beam");
        createRows( 10, "EfxCircle", "Moving Head Beam + Spot EFX", 6, 1,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW,  buttonGroup,"MHead Spot + Beam");
        createRows( 10, "QLCEfxSpline", "Moving Head Beam + Spot EFX", 0, 1,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW,  buttonGroup,"MHead Spot + Beam");

        createRows( 20, "Scene", "Moving Head Beam Color", 0, 7,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW, "MHead Beam");
        buttonGroup = createRows( 21, "Scene", "Moving Head Beam Positions", 0, 4,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW, "MHead Beam");
        createRows( 21, "EfxMultiLine", "Moving Head Beam EFX", 0, 2,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW, buttonGroup,"MHead Beam");
        createRows( 21, "EfxCircle", "Moving Head Beam EFX", 6, 2,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW,  buttonGroup,"MHead Beam");
        createRows( 21, "QLCEfxSpline", "Moving Head Beam EFX", 1, 1,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW,  buttonGroup,"MHead Beam");

        createRows( 30, "Scene", "Moving Head Spot", 0, 7,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, "MHead Spot");
        buttonGroup = createRows( 31, "Scene", "Moving Head Spot Positions", 0, 4,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, "MHead Spot");
        createRows( 31, "EfxMultiLine", "Moving Head Spot EFX", 0, 2,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW, buttonGroup,"MHead Spot");
        createRows( 31, "EfxCircle", "Moving Head Spot EFX", 6, 2,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW,  buttonGroup,"MHead Spot");
        createRows( 31, "QLCEfxSpline", "Moving Head Spot EFX", 1, 1,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW,  buttonGroup,"MHead Spot");

        createRows( 32, "Collection", "", 0, 7,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, "AUTO");
        buttonGroup = createRows( 33, "Collection", "Sec1", 0, 5,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, "AUTO");
        createRows( 34, "Collection", "Sec2", 0, 3,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, buttonGroup, "AUTO");
        createRows(34, "Collection", "Sec 3", 0, 1,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, buttonGroup, "AUTO");

        consistencyCheck();
    }

    private void consistencyCheck(){
        for (Map.Entry<String, PageConfig> panel: buttonGroupMapByCategory.entrySet()){

            for (ButtonGroup buttonGroup: panel.getValue().getButtonGroups()) {
                QLCScene blackoutScene = null;
                if (buttonGroup.getGlobalOff()!=null)
                    blackoutScene = buttonGroup.getGlobalOff();

                if (blackoutScene == null)
                    log.debug("Panel: " + panel.getKey() + " buttonGroup "+ buttonGroup.getId() + " doesn't have blackout scene");
            }
        }
    }

    private ButtonGroup createRows(final int groupId, final String type, final String path, int matrixX,
                                   int matrixY, final KeyData.StateLight onState, final KeyData.StateLight offState, final String category) {
        return createRows(groupId, type, path,  matrixX, matrixY, onState,  offState, null, category );
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

    private ButtonGroup createRows(final int groupId, final String type, final String path, int matrixX,
                                   int matrixY, final KeyData.StateLight onState, final KeyData.StateLight offState,
                                   ButtonGroup buttonGroup, final String category){
        final List<QLCFunction> list = ShowCollection.getInstance().getFunctionList(type, path);
        int[] showIds = getShowsId(list);
        int i;

        QLCScene blackoutScene = null;
        for (QLCFunction function: list){
            if(function.isBlackout())
                blackoutScene = (QLCScene)function;
        }

        if (buttonGroup ==null)
            buttonGroup = new ButtonGroup(groupId, type, new ArrayList<>());

        if (buttonGroup.getGlobalOff()==null && blackoutScene!=null)
            buttonGroup.setGlobalOff(blackoutScene);

        final List<QLCButton> laserButtons = buttonGroup.getButtonList();

        for (i=0; i<showIds.length; i++){
            QLCButton qlcButton = new QLCButton(matrixX, matrixY, ShowCollection.getInstance().getShow(showIds[i]),1,
                    onState, offState, KeyData.StateLight.OFF);

            laserButtons.add(qlcButton);
            matrixX++;
            if (matrixX>7){
                matrixX=0;
                matrixY--;
            }
        }

        if (!buttonGroupMapByCategory.containsKey(category))
            buttonGroupMapByCategory.put(category, new PageConfig(new ArrayList<>()));

        buttonGroupMapByCategory.get(category).getButtonGroups().add(buttonGroup);
        return buttonGroup;
    }

    public PageConfig getButtonGroupMap(final String category) {
        if (!buttonGroupMapByCategory.containsKey(category))
            return new PageConfig(List.of());

        return buttonGroupMapByCategory.get(category);
    }
}
