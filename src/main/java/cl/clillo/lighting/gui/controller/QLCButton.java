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
    private String text;
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

    public QLCButton(final int matrixX, final int matrixY, final Show show) {
        this(matrixX, matrixY, show, -1, KeyData.StateLight.OFF, KeyData.StateLight.OFF, KeyData.StateLight.OFF);
    }

    public QLCButton(final int matrixX, final int matrixY, final Show show, final int groupId,
                     final KeyData.StateLight onState, final KeyData.StateLight offState, final KeyData.StateLight nullState) {
        this.matrixX = matrixX;
        this.matrixY = matrixY;
        this.button = new JToggleButton();
        this.groupId = groupId;
        this.text = matrixX + "," + matrixY;

        midiHandler = MidiHandler.getInstance();
        onMessage = midiHandler.getShortMessage(this.matrixX, this.matrixY, onState);
        offMessage = midiHandler.getShortMessage(this.matrixX, this.matrixY, offState);
        nullMessage = midiHandler.getShortMessage(this.matrixX, this.matrixY, nullState);

        button.setBounds(matrixX*175+ 20, (7-matrixY)*70 + 10, 165, 60);
        button.addItemListener(this);

        state = false;

        this.show = show;
        if (show!=null)
            text = show.getFunction().getPath()+"\n"+show.getFunction().getName();

        button.setText("<html><center>" + text.replaceAll("\\n", "<br>") + "</center></html>");
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
        if (show==null)
            return;

      //  System.out.println(text + "\tPrev: "+state);
        if (toggleBrothers!=null)
            for (QLCButton qlcButton: toggleBrothers)
                if(!qlcButton.getButton().getText().equals(this.getButton().getText()) && qlcButton.show!=null && qlcButton.show.isExecuting()) {
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

    public Show getShow() {
        return show;
    }

    public String getMapKey(){
        return matrixX + "-" + matrixY;
    }
}
