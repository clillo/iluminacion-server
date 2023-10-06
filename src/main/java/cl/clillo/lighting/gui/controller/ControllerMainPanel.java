package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.external.midi.KeyData;
import cl.clillo.lighting.external.midi.MidiEvent;
import cl.clillo.lighting.external.midi.MidiHandler;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.ArrayList;
import java.util.List;

public class ControllerMainPanel extends JPanel implements MidiEvent, ChangeListener {

    private static final long serialVersionUID = -5869553409971473557L;

    public static final int WIDTH1 = 1424;
    public static final int HEIGHT1 = 900;

    private final List<ControllerEditPanel> pnlList;
    private final MidiHandler midiHandler;
    private final JTabbedPane tabbedPane;
    private final ControllerEditPanel[] controllerEditPanels;
    private int activeIndex = -1;

    public ControllerMainPanel() {

        pnlList = new ArrayList<>();
        midiHandler = MidiHandler.getInstance(this);
        tabbedPane = new JTabbedPane(JTabbedPane.RIGHT);
        tabbedPane.setBounds(0, 0, WIDTH1 + 200, HEIGHT1);
        cleanMatrix();
        add(tabbedPane);
        controllerEditPanels = new ControllerEditPanel[8];
        for (int i=0; i<8; i++) {
            final ControllerEditPanel editPanel = buildPanel(i+1);
            controllerEditPanels[i] = editPanel;
            editPanel.setBounds(0, 0, WIDTH1 + 200, HEIGHT1);
            tabbedPane.addTab(editPanel.getName(), editPanel);
            pnlList.add(editPanel);
        }

        tabbedPane.addChangeListener(this);
        this.setBounds(0, 0, WIDTH1 + 200, HEIGHT1);
        this.setLayout(null);
        selectPanel(7);

    }

    private ControllerEditPanel buildPanel(final int index) {
        return new ControllerEditPanel(index){ };
    }

    @Override
    public void onKeyPress(final KeyData keyData) {
        if (keyData.getMidiInputType() == KeyData.MidiInputType.SIDE_BUTTON){
            selectPanel(keyData.getValue());
        }

        if (keyData.getMidiInputType() == KeyData.MidiInputType.MATRIX_BUTTON){
            ControllerEditPanel panel = (ControllerEditPanel) tabbedPane.getSelectedComponent();
            panel.toggleButton(keyData.getMatrixX(), keyData.getMatrixY());
        }
    }

    private void cleanMatrix(){
        for (int i=0; i<8; i++)
            for (int j=0; j<8; j++)
                midiHandler.send(i, j, KeyData.StateLight.OFF);
    }

    private void selectPanel(int index){
        activePanel(7-index);
        tabbedPane.removeChangeListener(this);
        tabbedPane.setSelectedIndex(7-index);
        tabbedPane.addChangeListener(this);
    }

    @Override
    public void onKeyRelease(KeyData keyData) {

    }

    @Override
    public void onSlide(KeyData keyData) {

    }

    @Override
    public void stateChanged(ChangeEvent e) {
        tabbedPane.removeChangeListener(this);
        activePanel(tabbedPane.getSelectedIndex());
        tabbedPane.addChangeListener(this);
    }

    private void activePanel(int index){
        if (activeIndex==index)
            return;
        cleanMatrix();
        for (int i=0; i<8; i++)
            midiHandler.sendSide(i, KeyData.StateLight.OFF);
        midiHandler.sendSide(7-index, KeyData.StateLight.RED);
        controllerEditPanels[index].activePanel();
        activeIndex = index;
    }
}