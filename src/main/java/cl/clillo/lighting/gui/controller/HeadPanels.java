package cl.clillo.lighting.gui.controller;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class HeadPanels {

    private final String []names = {"Collections", "Laser-Derby", "Moving Heads", "Spider", "MHead Beam", "MHead Spot", "MHead Spot + Beam", "MHead Bee Eye"};

    @Getter
    private final List<HeadPanel> headPanels;

    HeadPanels(){
        headPanels = new ArrayList<>();
        int i=0;
        for (String name: names)
            headPanels.add(new HeadPanel(i++, name));
    }

    public record HeadPanel(int index, String name){};

    public static HeadPanels getInstance() {
        return InstanceHolder.getInstance();
    }

    private static final class InstanceHolder {
        private static HeadPanels instance;

        public static HeadPanels getInstance() {
            if (instance==null){
                instance = new HeadPanels();
            }
            return instance;
        }
    }
}
