package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.external.midi.KeyData;
import cl.clillo.lighting.external.midi.MidiShiftBank;
import cl.clillo.lighting.model.QLCFunction;
import cl.clillo.lighting.model.QLCScene;
import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.model.ShowCollection;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class MidiButtonFunctionRepository {

    private static final class InstanceHolder {
        private static final MidiButtonFunctionRepository instance = new MidiButtonFunctionRepository();
    }

    public static MidiButtonFunctionRepository getInstance() {
        return InstanceHolder.instance;
    }

    private final Map<Integer, QLCButton> buttonByShow;
    private final Map<String, PageConfig> buttonGroupMapByCategory;

    MidiButtonFunctionRepository(){
        buttonGroupMapByCategory = new HashMap<>();
        buttonByShow = new HashMap<>();
        ButtonGroup buttonGroup;

        createRows( 1, "Scene", "Laser", 0, 7,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, "Laser-Derby");

        buttonGroup = createRows( 2, "Scene", "Derby", 0, 3,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW, "Laser-Derby");
        createRows( 2, "Sequence", "Derby", 0, 0,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW, buttonGroup, "Laser-Derby");

        buttonGroup = createRows( 6, "Sequence", "RGBW", 0, 7,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN, "RGBW");
        createRows( 6, "Scene", "RGBW", 0, 5,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN, buttonGroup, "RGBW");

        buttonGroup = createRows( 3, "Scene", "Spider", 0, 7,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW, "Spider");

        createRows( 3, "Sequence", "Spiders/Multiple", 5, 4,
                KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW, buttonGroup, "Spider");

        buttonGroup = createRows( 7, "Scene", "Spider Positions", 0, 1,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, "Spider");

        createRows( 7, "QLCEfxLine", "QLCEfx Spiders", 4, 0,
                KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, buttonGroup, "Spider");

        buttonGroup = createRows( 5, "Scene", "Moving Head Beam + Spot Color On.Off", 0, 7,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN, "MHead Spot + Beam");
        createRows( 5, "Sequence", "Moving Head Beam + Spot Sequence", 6, 7,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, buttonGroup, "MHead Spot + Beam");
        createRows( 9, "Scene", "Moving Head Beam + Spot Gobos Prism", 1, 6,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN, "MHead Spot + Beam");

        createRows( 5, "Scene", "Moving Head Beam + Spot Color", 0, 5,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN, "MHead Spot + Beam");
        createRows( 8, "Scene", "Moving Head Beam + Spot Gobos", 2, 4,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN, "MHead Spot + Beam");

        buttonGroup = createRows( 10, "Scene", "Moving Head Beam + Spot Positions", 0, 2,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN, "MHead Spot + Beam");
        createRows( 10, "EfxMultiLine", "Moving Head Beam + Spot EFX", 0, 1,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW, buttonGroup,"MHead Spot + Beam");
        createRows( 10, "EfxCircle", "Moving Head Beam + Spot EFX", 6, 1,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW,  buttonGroup,"MHead Spot + Beam");
        createRows( 10, "QLCEfxSpline", "Moving Head Beam + Spot EFX", 0, 1,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW,  buttonGroup,"MHead Spot + Beam");

        buttonGroup = createRows( 19, "Scene", "Moving Head Beam On/Off", 0, 7,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, "MHead Beam");
        createRows( 19, "Sequence", "Moving Head Beam Sequence", 6, 7,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED,buttonGroup, "MHead Beam");
        createRows( 19, "Scene", "Moving Head Beam Strobe", 1, 6,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW, "MHead Beam");
        createRows( 20, "Scene", "Moving Head Beam Color", 0, 5,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW, "MHead Beam");
        buttonGroup = createRows( 21, "Scene", "Moving Head Beam Positions", 0, 3,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW, "MHead Beam");
        createRows( 21, "EfxMultiLine", "Moving Head Beam EFX", 0, 2,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW, buttonGroup,"MHead Beam");
        createRows( 21, "EfxCircle", "Moving Head Beam EFX", 6, 2,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW,  buttonGroup,"MHead Beam");
        createRows( 21, "QLCEfxSpline", "Moving Head Beam EFX", 1, 1,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW,  buttonGroup,"MHead Beam");

        buttonGroup = createRows( 30, "Scene", "Moving Head Spot On/Off", 0, 7,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, "MHead Spot");
        createRows( 30, "Sequence", "Moving Head Spot Sequence", 4, 7,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, buttonGroup,"MHead Spot");
        createRows( 30, "Scene", "Moving Head Spot Strobe", 5, 7,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, buttonGroup,"MHead Spot");
        createRows( 30, "Scene", "Moving Head Spot", 0, 6,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, "MHead Spot");
        createRows( 30, "Scene", "Moving Head Spot Cristal", 0, 3,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, "MHead Spot");
        createRows( 30, "Scene", "Moving Head Spot Gobos", 0, 4,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, "MHead Spot");
        createRows( 30, "Scene", "Moving Head Spot Prism", 1, 5,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW, "MHead Spot");
        buttonGroup = createRows( 31, "Scene", "Moving Head Spot Positions", 0, 2,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, "MHead Spot");
        createRows( 31, "EfxMultiLine", "Moving Head Spot EFX", 1, 1,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW, buttonGroup,"MHead Spot");
        createRows( 31, "EfxCircle", "Moving Head Spot EFX", 7, 1,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW,  buttonGroup,"MHead Spot");
        createRows( 31, "QLCEfxSpline", "Moving Head Spot EFX", 2, 0,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW,  buttonGroup,"MHead Spot");

        buttonGroup = createRows( 32, "Collection", "Mirror Ball", 0, 7,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, "Collections");
        createRows( 32, "Scene", "Generic", 7, 7,  KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW, buttonGroup, "Collections");
        createRows( 33, "Collection", "Sec1", 0, 5,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, buttonGroup, "Collections");
        createRows( 34, "Collection", "Sec2", 0, 4,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, buttonGroup, "Collections");
        createRows(34, "Collection", "Sec 3", 0, 3,  KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, buttonGroup, "Collections");
        createRows(34, "Chaser", "Generales", 0, 0,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN, buttonGroup, "Collections");
        createRows(34, "Chaser", "Laser", 7, 0,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN, buttonGroup, "Collections");



        buttonGroup = createRows( 50, "Scene", "Moving Head On.Off", 0, 7,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN, "Moving Heads");
        createRows( 50, "Sequence", "Moving Head Sequence", 6, 7,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN,buttonGroup, "Moving Heads");

        buttonGroup = createRows( 50, "Scene", "Moving Head Color", 0, 5,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN, "Moving Heads");

        buttonGroup = createRows( 50, "QLCEfxLine", "Moving Head EFX", 0, 2,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN, "Moving Heads");

        createRows( 60, "Scene", "Moving Head Bee Eye Dimmer", 0, 7,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN,"MHead Bee Eye");
        createRows( 60, "Scene", "Moving Head Bee Eye Strobe", 2, 7,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN,"MHead Bee Eye");
        createRows( 60, "Scene", "Moving Head Bee Eye Focus", 5, 7,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN,"MHead Bee Eye");
        createRows( 60, "Scene", "Moving Head Bee Eye Rotating", 1, 6,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN,"MHead Bee Eye");
        buttonGroup = createRows( 60, "Scene", "Moving Head Bee Eye Positions", 2, 5,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN,"MHead Bee Eye");
        createRows( 60, "EfxMultiLine", "Moving Head Bee Eye EFX", 2, 4,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN,buttonGroup,"MHead Bee Eye");
        createRows( 60, "EfxCircle", "Moving Head Bee Eye EFX", 0, 3,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN,buttonGroup,"MHead Bee Eye");
        createRows( 60, "QLCEfxSpline", "Moving Head Bee Eye EFX", 2, 3,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN,buttonGroup,"MHead Bee Eye");


        buttonGroup = createRows( 61, "Scene", "Moving Head Bee Eye Color", 4, 3,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN, "MHead Bee Eye");
        createRows( 64, "Sequence", "Moving Head Bee Eye Sequence", 1, 2,  KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN,buttonGroup,"MHead Bee Eye");

        // Bank B: nuevas secuencias / looks (Shift)
        buttonGroup = createRowsForIds(100, new int[]{9000, 9001, 9002}, 0, 7,
                KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, null, "Laser-Derby", MidiShiftBank.BANK_B);
        createRowsForIds(100, new int[]{9003, 9004, 9005}, 0, 6,
                KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW, buttonGroup, "Laser-Derby", MidiShiftBank.BANK_B);
        createRowsForIds(100, new int[]{9012, 9013}, 0, 5,
                KeyData.StateLight.YELLOW_BLINK, KeyData.StateLight.YELLOW, buttonGroup, "Laser-Derby", MidiShiftBank.BANK_B);
        createRowsForIds(100, new int[]{9006, 9007, 9010, 9011}, 0, 4,
                KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN, buttonGroup, "Laser-Derby", MidiShiftBank.BANK_B);

        createRowsForIds(110, new int[]{9040, 9041, 9042, 9043}, 0, 7,
                KeyData.StateLight.RED_BLINK, KeyData.StateLight.RED, null, "Collections", MidiShiftBank.BANK_B);

        buttonGroup = createRowsForIds(120, new int[]{8425, 8426, 8427, 8428, 8429, 8430, 8431, 8432}, 0, 7,
                KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN, null, "MHead Bee Eye", MidiShiftBank.BANK_B);
        createRowsForIds(120, new int[]{8433, 8434, 8435, 8436, 8106, 8107, 8108, 8109}, 0, 6,
                KeyData.StateLight.GREEN_BLINK, KeyData.StateLight.GREEN, buttonGroup, "MHead Bee Eye", MidiShiftBank.BANK_B);

        consistencyCheck();
    }

    private void consistencyCheck(){
        for (Map.Entry<String, PageConfig> panel: buttonGroupMapByCategory.entrySet()){
            for (int bank = MidiShiftBank.BANK_A; bank <= MidiShiftBank.BANK_B; bank++) {
                for (ButtonGroup buttonGroup: panel.getValue().getButtonGroups(bank)) {
                    if (buttonGroup.getGlobalOff() == null)
                        log.debug("Panel: " + panel.getKey() + " bank " + bank
                                + " buttonGroup "+ buttonGroup.getId() + " doesn't have blackout scene");

                    for (QLCButton button: buttonGroup.getButtonList())
                        if (button.getShow() != null)
                            buttonByShow.put(button.getShow().getId(), button);
                }
            }
        }
    }

    private ButtonGroup createRows(final int groupId, final String type, final String path, int matrixX,
                                   int matrixY, final KeyData.StateLight onState, final KeyData.StateLight offState, final String category) {
        return createRows(groupId, type, path,  matrixX, matrixY, onState,  offState, null, category, MidiShiftBank.BANK_A);
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
        return createRows(groupId, type, path, matrixX, matrixY, onState, offState, buttonGroup, category, MidiShiftBank.BANK_A);
    }

    private ButtonGroup createRows(final int groupId, final String type, final String path, int matrixX,
                                   int matrixY, final KeyData.StateLight onState, final KeyData.StateLight offState,
                                   ButtonGroup buttonGroup, final String category, final int bank){
        final List<QLCFunction> list = ShowCollection.getInstance().getFunctionList(type, path);
        QLCScene blackoutScene = null;
        for (QLCFunction function: list){
            if(function.isBlackout())
                blackoutScene = (QLCScene)function;
        }
        return placeShows(groupId, type, getShowsId(list), matrixX, matrixY, onState, offState,
                buttonGroup, category, bank, blackoutScene);
    }

    private ButtonGroup createRowsForIds(final int groupId, final int[] showIds, int matrixX, int matrixY,
                                         final KeyData.StateLight onState, final KeyData.StateLight offState,
                                         final ButtonGroup buttonGroup, final String category, final int bank) {
        return placeShows(groupId, "BankB", showIds, matrixX, matrixY, onState, offState,
                buttonGroup, category, bank, null);
    }

    private ButtonGroup placeShows(final int groupId, final String type, final int[] showIds, int matrixX, int matrixY,
                                   final KeyData.StateLight onState, final KeyData.StateLight offState,
                                   ButtonGroup buttonGroup, final String category, final int bank,
                                   final QLCScene blackoutScene) {
        if (buttonGroup == null)
            buttonGroup = new ButtonGroup(groupId, type, new ArrayList<>());

        if (buttonGroup.getGlobalOff() == null && blackoutScene != null)
            buttonGroup.setGlobalOff(blackoutScene);

        final List<QLCButton> buttons = buttonGroup.getButtonList();
        for (int showId : showIds) {
            final Show show = ShowCollection.getInstance().getShow(showId);
            if (show == null) {
                log.warn("MIDI map: show {} not found (category={}, bank={})", showId, category, bank);
                continue;
            }
            buttons.add(new QLCButton(matrixX, matrixY, show, 1, onState, offState, KeyData.StateLight.OFF));
            matrixX++;
            if (matrixX > 7) {
                matrixX = 0;
                matrixY--;
            }
        }

        if (!buttonGroupMapByCategory.containsKey(category))
            buttonGroupMapByCategory.put(category, new PageConfig(new ArrayList<>()));

        final PageConfig pageConfig = buttonGroupMapByCategory.get(category);
        final List<ButtonGroup> bankGroups = pageConfig.getButtonGroups(bank);
        if (!bankGroups.contains(buttonGroup))
            bankGroups.add(buttonGroup);
        return buttonGroup;
    }

    public PageConfig getButtonGroupMap(final String category) {
        if (!buttonGroupMapByCategory.containsKey(category))
            return new PageConfig(List.of());

        return buttonGroupMapByCategory.get(category);
    }

    public QLCButton getButton(int showId){
        return buttonByShow.get(showId);
    }
}
