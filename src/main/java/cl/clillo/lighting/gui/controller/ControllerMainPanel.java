package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.executor.IOS2LEventListener;
import cl.clillo.lighting.external.midi.KeyData;
import cl.clillo.lighting.external.midi.MidiEvent;
import cl.clillo.lighting.external.midi.MidiHandler;
import cl.clillo.lighting.gui.movements.EFXMConfigureMainPanel;
import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.model.ShowCollection;
import cl.clillo.lighting.repository.StateRepository;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class ControllerMainPanel extends JPanel implements MidiEvent, ChangeListener, ActionListener, IOS2LEventListener  {

    private static final long serialVersionUID = -5869553409971473557L;

    public static final int WIDTH1 = 1364;
    public static final int HEIGHT1 = 900;

    private final List<JSlider> pnlListDimmer;
    private final MidiHandler midiHandler;
    private final JTabbedPane midiPages;
    private final JTabbedPane chaserPages;

    private final ControllerEditPanel[] controllerEditPanels;
    private int activeIndex = -1;

    private final JTextField txtActualBPM = new JTextField();
    private final JTextField txtTime = new JTextField();
    private final JTextField txtTimeX2 = new JTextField();

    private final JTextField txtPos = new JTextField();
    private final JTextField txtEvent = new JTextField();
    private final DimmerManager[] dimmers;

    public ControllerMainPanel() {

        pnlListDimmer = new ArrayList<>();
        midiHandler = MidiHandler.getInstance(this);
        midiPages = new JTabbedPane();
        midiPages.setBounds(0, 0, WIDTH1 + 280, HEIGHT1-280);
        cleanMatrix();
        add(midiPages);

        controllerEditPanels = new ControllerEditPanel[8];
        String []names = {"Collections", "Laser-Derby", "RGBW", "Moving Heads", "Spider", "MHead Beam", "MHead Spot", "MHead Spot + Beam"};
        for (int i=0; i<8; i++) {
            final ControllerEditPanel editPanel = new ControllerEditPanel(i+1, names[i]);
            controllerEditPanels[i] = editPanel;

            editPanel.setBounds(0, 0, WIDTH1 + 140, HEIGHT1-400);
            midiPages.addTab("<html><p style='padding:2px; font-family:\"Tahoma, sans-serif;\" font-size:10px;'>"+names[i]+"</p></html>", editPanel);
        }

        midiPages.addChangeListener(this);
        this.setBounds(0, 0, WIDTH1 + 200, HEIGHT1);
        this.setLayout(null);
        selectPanel(7);

        final JPanel panelDimmers = new JPanel();
        panelDimmers.setLayout(null);

        panelDimmers.setOpaque(true);
     //   panelDimmers.setBackground(Color.BLACK);
        panelDimmers.setBounds(0, HEIGHT1-290, WIDTH1 + 200, 400);

        for (int i=0; i<9; i++) {
            final JSlider slider = new JSlider();
            slider.setOrientation(JSlider.VERTICAL);
            slider.setBounds(i * 60, 10, 100, 380);
            slider.setMinimum(0);
            slider.setMaximum(255);
            panelDimmers.add(slider);
            pnlListDimmer.add(slider);
        }

        dimmers = new DimmerManager[4];
        for (int i=0; i<4; i++)
            dimmers[i] = new DimmerManager(pnlListDimmer.get(pnlListDimmer.size() - i - 2), i);

        this.add(panelDimmers);

        JButton btnSave = new JButton();
        btnSave.setText("Save");
        btnSave.setBounds(EFXMConfigureMainPanel.WIDTH1+ 20, 50, 120, 20);

        panelDimmers.add(btnSave);
        btnSave.addActionListener(this);

        txtActualBPM.setBounds(EFXMConfigureMainPanel.WIDTH1+ 20, 80, 120, 30);
        txtTime.setBounds(EFXMConfigureMainPanel.WIDTH1+ 20, 115, 120, 30);
        txtTimeX2.setBounds(EFXMConfigureMainPanel.WIDTH1+ 20, 150, 120, 30);
        txtPos.setBounds(EFXMConfigureMainPanel.WIDTH1+ 20, 185, 120, 30);
        txtEvent.setBounds(EFXMConfigureMainPanel.WIDTH1+ 20, 220, 120, 30);

        txtActualBPM.setFont(new Font("serif", Font.BOLD, 18));
        txtTime.setFont(new Font("serif", Font.BOLD, 18));
        txtTimeX2.setFont(new Font("serif", Font.BOLD, 18));
        txtPos.setFont(new Font("serif", Font.BOLD, 18));
        txtEvent.setFont(new Font("serif", Font.BOLD, 18));

        panelDimmers.add(txtActualBPM);
        panelDimmers.add(txtTime);
        panelDimmers.add(txtTimeX2);
        panelDimmers.add(txtPos);
        panelDimmers.add(txtEvent);

        chaserPages = new JTabbedPane();
        chaserPages.setBounds(580, 0, 850, 400);
        chaserPages.addTab("Chaser Executor",new ControllerChaser());
        chaserPages.addTab("Collections Editor",new ControllerCollections());
        panelDimmers.add(chaserPages);

        ShowCollection.getInstance().getOs2LScheduler().setIos2LEventListener(this);
    }

    @Override
    public void onKeyPress(final KeyData keyData) {
        if (keyData.getMidiInputType() == KeyData.MidiInputType.SIDE_BUTTON){
            selectPanel(keyData.getValue());
            return;
        }

        if (keyData.getMidiInputType() == KeyData.MidiInputType.MATRIX_BUTTON){
            ControllerEditPanel panel = (ControllerEditPanel) midiPages.getSelectedComponent();
            panel.toggleButton(keyData.getMatrixX(), keyData.getMatrixY());
            return;
        }

        if (keyData.getMidiInputType() == KeyData.MidiInputType.SLIDER_BUTTON ){

            for (DimmerManager dimmerManager: dimmers) {
                switch (keyData.getValue()) {
                    case 4:
                        selectSlider(dimmerManager, 3, keyData.getValue());
                        break;
                    case 5:
                        selectSlider(dimmerManager, 2, keyData.getValue());
                        break;
                    case 6:
                        selectSlider(dimmerManager, 1, keyData.getValue());
                        break;
                    case 7:
                        selectSlider(dimmerManager, 0, keyData.getValue());
                        break;
                }
            }
            return;
        }

    }

    private void selectSlider(DimmerManager dimmerManager, int index, int slider){
        if (dimmerManager.getIndex()==index) {
            dimmerManager.setActive(!dimmerManager.isActive());
            if (dimmerManager.isActive())
                midiHandler.sendSlider(slider, KeyData.StateLight.GREEN_BLINK);
            else
                midiHandler.sendSlider(slider, KeyData.StateLight.OFF);
        }
    }

    private void cleanMatrix(){
        for (int i=0; i<8; i++)
            for (int j=0; j<8; j++)
                midiHandler.send(i, j, KeyData.StateLight.OFF);
    }

    private void selectPanel(int index){
        activePanel(7-index);
        midiPages.removeChangeListener(this);
        midiPages.setSelectedIndex(7-index);
        midiPages.addChangeListener(this);
    }

    @Override
    public void onKeyRelease(KeyData keyData) {

    }

    @Override
    public void onSlide(final KeyData keyData) {
        for (DimmerManager dimmerManager: dimmers)
            if (dimmerManager.isActive())
                dimmerManager.onSlide(keyData, pnlListDimmer.get(keyData.getPosX()));
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

    private void save(){
        for (DimmerManager dimmerManager: dimmers)
            dimmerManager.save();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        save();
    }

    @Override
    public void changeBPM(double bpm) {
        txtActualBPM.setText(String.valueOf(bpm));
    }

    @Override
    public void changeTimes(long time, long timex2) {
        txtTime.setText(time + " ms");
        txtTimeX2.setText(timex2 + " ms 1/2");
    }

    @Override
    public void pos(int pos) {
        int beat = Math.abs((int)(pos % 16.0) + 1);
        txtPos.setText(pos + " - " + beat);
    }

    @Override
    public void event(int event) {
        Show show = StateRepository.getInstance().getEventShow(event);
        for (Show show1: ShowCollection.getInstance().getShowList()) {
            show1.setExecuting(false);
            if (show1.getFunction().isTotalBlackout())
                show1.setExecuteOneTime(true);
        }

        if (show==null){
            txtEvent.setText(event +" - undefined");
            return;
        }
        txtEvent.setText(event +" - "+show.getId());

        show.setExecuting(true);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        midiPages.removeChangeListener(this);
        activePanel(midiPages.getSelectedIndex());
        midiPages.addChangeListener(this);
    }

}