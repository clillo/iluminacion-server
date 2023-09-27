package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.midi.KeyData;
import cl.clillo.lighting.midi.MidiHandler;

import javax.sound.midi.ShortMessage;
import javax.swing.JToggleButton;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

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

    public QLCButton(int matrixX, int matrixY) {
        this.matrixX = matrixX;
        this.matrixY = matrixY;
        this.button = new JToggleButton();
        this.text = matrixX + "," + matrixY;

        midiHandler = MidiHandler.getInstance();
        onMessage = midiHandler.getShortMessage(this.matrixX, this.matrixY, KeyData.StateLight.RED_BLINK);
        offMessage = midiHandler.getShortMessage(this.matrixX, this.matrixY, KeyData.StateLight.RED);
        nullMessage = midiHandler.getShortMessage(this.matrixX, this.matrixY, KeyData.StateLight.OFF);

        button.setText(text);
        button.setBounds(matrixX*120+ 20, (7-matrixY)*70 + 10, 110, 60);
        button.addItemListener(this);

        state = false;

       // midiHandler.send(offMessage);
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
        if (state)
            midiHandler.send(onMessage);
        else
            midiHandler.send(offMessage);
    }
}
