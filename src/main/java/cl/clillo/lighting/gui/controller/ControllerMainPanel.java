package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.midi.KeyData;
import cl.clillo.lighting.midi.MidiEvent;
import cl.clillo.lighting.midi.MidiHandler;
import cl.clillo.lighting.model.QLCEfx;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

public class ControllerMainPanel extends JPanel implements MidiEvent {

    private static final long serialVersionUID = -5869553409971473557L;

    public static final int WIDTH1 = 1024;
    public static final int HEIGHT1 = 900;

    private final List<ControllerEditPanel> pnlList;
    private final MidiHandler midiHandler;
    private final JTabbedPane tabbedPane;
    private final ControllerEditPanel[] controllerEditPanels;

    public ControllerMainPanel() {
        pnlList = new ArrayList<>();
        midiHandler = MidiHandler.getInstance(this);
        tabbedPane = new JTabbedPane(JTabbedPane.RIGHT);
        tabbedPane.setBounds(0, 0, WIDTH1 + 200, HEIGHT1);

        add(tabbedPane);
        controllerEditPanels = new ControllerEditPanel[8];
        for (int i=0; i<8; i++) {
            final ControllerEditPanel editPanel = buildPanel(i+1);
            controllerEditPanels[i] = editPanel;
            editPanel.setBounds(0, 0, WIDTH1 + 200, HEIGHT1);
            tabbedPane.addTab(editPanel.getName(), editPanel);
            pnlList.add(editPanel);
        }

        this.setBounds(0, 0, WIDTH1 + 200, HEIGHT1);
        this.setLayout(null);
        selectPanel(7);
        cleanMatrix();
    }

    private ControllerEditPanel buildPanel(final int index) {

        return new ControllerEditPanel(midiHandler, index){

            @Override
            protected void drawCanvas(Graphics g) {

            }

            @Override
            protected void setQlcEfx(QLCEfx qlcEfx) {

            }
        };
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
        tabbedPane.setSelectedIndex(7-index);
        for (int i=0; i<8; i++)
            midiHandler.sendSide(i, KeyData.StateLight.OFF);
        midiHandler.sendSide(index, KeyData.StateLight.RED);
        controllerEditPanels[7-index].activePanel();
    }

    @Override
    public void onKeyRelease(KeyData keyData) {

    }

    @Override
    public void onSlide(KeyData keyData) {

    }
}