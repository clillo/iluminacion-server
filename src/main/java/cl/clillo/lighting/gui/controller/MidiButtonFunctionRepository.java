package cl.clillo.lighting.gui.controller;

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

    MidiButtonFunctionRepository(){
        buttonMap = new HashMap<>();
        List<QLCFunction> list = ShowCollection.getInstance().getFunctionList("Scene", "Laser");
        int[] showIds = new int[list.size()];
        int i=0;
        for (QLCFunction function: list){
            showIds[i++]=function.getId();
        }

        buttonMap.put("0-7", new QLCButton(0, 7, showIds[0],1));
        buttonMap.put("1-7", new QLCButton(1, 7, showIds[1],1));
        buttonMap.put("2-7", new QLCButton(2, 7, showIds[2],1));
        buttonMap.put("3-7", new QLCButton(3, 7, showIds[3],1));
        buttonMap.put("4-7", new QLCButton(4, 7, showIds[4],1));
        buttonMap.put("5-7", new QLCButton(5, 7, showIds[5],1));
        buttonMap.put("6-7", new QLCButton(6, 7, showIds[6],1));
        buttonMap.put("7-7", new QLCButton(7, 7, showIds[7],1));

        buttonMap.put("0-6", new QLCButton(0, 6, showIds[8],1));
        buttonMap.put("1-6", new QLCButton(1, 6, showIds[9],1));
        buttonMap.put("2-6", new QLCButton(2, 6, showIds[10],1));
        buttonMap.put("3-6", new QLCButton(3, 6, showIds[11],1));
        buttonMap.put("4-6", new QLCButton(4, 6, showIds[12],1));
        buttonMap.put("5-6", new QLCButton(5, 6, showIds[13],1));
        buttonMap.put("6-6", new QLCButton(6, 6, showIds[14],1));
        buttonMap.put("7-6", new QLCButton(7, 6, showIds[15],1));

        buttonMap.put("0-5", new QLCButton(0, 5, showIds[16],1));
        buttonMap.put("1-5", new QLCButton(1, 5, showIds[17],1));
        buttonMap.put("2-5", new QLCButton(2, 5, showIds[18],1));
        buttonMap.put("3-5", new QLCButton(3, 5, showIds[19],1));
        buttonMap.put("4-5", new QLCButton(4, 5, showIds[20],1));
        buttonMap.put("5-5", new QLCButton(5, 5, showIds[21],1));
        buttonMap.put("6-5", new QLCButton(6, 5, showIds[22],1));
        buttonMap.put("7-5", new QLCButton(7, 5, showIds[23],1));

        buttonMap.put("0-4", new QLCButton(0, 4, showIds[24],1));

        List<QLCButton> toggleBrothers = new ArrayList<>();

        for (Map.Entry<String, QLCButton> entry: buttonMap.entrySet())
            toggleBrothers.add(entry.getValue());

        for (QLCButton qlcButton: toggleBrothers)
            qlcButton.setToggleBrothers(toggleBrothers);
    }

    public Map<String, QLCButton> getButtonMap() {
        return buttonMap;
    }
}
