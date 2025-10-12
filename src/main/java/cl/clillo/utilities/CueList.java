package cl.clillo.utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CueList {
    private final String name;
    private final List<Cue> cues = new ArrayList<>();
    private boolean loop;

    public CueList(String name, boolean loop) {
        this.name = name;
        this.loop = loop;
    }

    public String name() { return name; }
    public boolean loop() { return loop; }
    public void setLoop(boolean loop) { this.loop = loop; }

    public List<Cue> cues() { return Collections.unmodifiableList(cues); }
    public void addCue(Cue cue) { cues.add(cue); }
    public void insertCue(int index, Cue cue) { cues.add(index, cue); }
    public void removeCue(int index) { cues.remove(index); }
    public int size() { return cues.size(); }
    public Cue get(int index) { return cues.get(index); }

    public void moveUp(int index) {
        if (index > 0 && index < cues.size()) {
            Collections.swap(cues, index, index - 1);
        }
    }

    public void moveDown(int index) {
        if (index >= 0 && index < cues.size() - 1) {
            Collections.swap(cues, index, index + 1);
        }
    }
}