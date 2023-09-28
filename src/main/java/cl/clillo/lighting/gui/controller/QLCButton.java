package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.midi.KeyData;
import cl.clillo.lighting.midi.MidiHandler;
import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.model.ShowCollection;

import javax.sound.midi.ShortMessage;
import javax.swing.JToggleButton;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

public class QLCButton implements ItemListener {

    private final JToggleButton button;
    private final String text;
    private final ShortMessage onMessage;
    private final ShortMessage offMessage;
    private final ShortMessage nullMessage;

    private final int matrixX;
    private final int matrixY;
    private boolean state;
    private final MidiHandler midiHandler;
    private final Show show;
    private int groupId;

    private List<QLCButton> toggleBrothers;

    public QLCButton(int matrixX, int matrixY, int showId) {
        this(matrixX, matrixY, showId, -1);
    }

    public QLCButton(int matrixX, int matrixY, int showId, int groupId) {
        this.matrixX = matrixX;
        this.matrixY = matrixY;
        this.button = new JToggleButton();
        this.groupId = groupId;
       // this.text = matrixX + "," + matrixY;

        midiHandler = MidiHandler.getInstance();
        onMessage = midiHandler.getShortMessage(this.matrixX, this.matrixY, KeyData.StateLight.RED_BLINK);
        offMessage = midiHandler.getShortMessage(this.matrixX, this.matrixY, KeyData.StateLight.RED);
        nullMessage = midiHandler.getShortMessage(this.matrixX, this.matrixY, KeyData.StateLight.OFF);

        button.setBounds(matrixX*120+ 20, (7-matrixY)*70 + 10, 110, 60);
        button.addItemListener(this);

        state = false;

        show = ShowCollection.getInstance().getShow(showId);
        text = show.getFunction().getPath()+"\n"+show.getFunction().getName();

        button.setText("<html><center><small>" + text.replaceAll("\\n", "<br>") + "</small></center></html>");
    }

    public JToggleButton getButton() {
        return button;
    }

    public String getText() {
        return text;
    }

    public void toggle(){
        button.setSelected(!state);
    }

    @Override
    public void itemStateChanged(final ItemEvent itemEvent) {
        this.state = itemEvent.getStateChange() == ItemEvent.SELECTED;
        refresh();
    }

    public void refresh(){
      //  System.out.println(text + "\tPrev: "+state);
        if (toggleBrothers!=null)
            for (QLCButton qlcButton: toggleBrothers)
                if(!qlcButton.getButton().getText().equals(this.getButton().getText()) && qlcButton.show.isExecuting()) {
                    qlcButton.show.setExecuting(false);
                    qlcButton.getButton().setSelected(false);
                }

        show.setExecuting(state);
        ShowCollection.getInstance().executeShow(show);
        if (state) {
            midiHandler.send(onMessage);
        }else
            midiHandler.send(offMessage);
    }

    public int getGroupId() {
        return groupId;
    }

    public void setToggleBrothers(List<QLCButton> toggleBrothers) {
        this.toggleBrothers = toggleBrothers;
    }

}
