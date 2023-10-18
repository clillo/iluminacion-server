package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.external.dmx.Dmx;
import cl.clillo.lighting.external.midi.KeyData;
import cl.clillo.lighting.external.midi.MidiEvent;
import cl.clillo.lighting.external.midi.MidiHandler;
import cl.clillo.lighting.fixture.qlc.QLCFixture;
import cl.clillo.lighting.gui.movements.EFXMConfigureMainPanel;
import cl.clillo.lighting.model.Point;
import cl.clillo.lighting.model.ShowCollection;
import cl.clillo.lighting.repository.StateRepository;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class ControllerMainPanel extends JPanel implements MidiEvent, ChangeListener, ActionListener {

    private static final long serialVersionUID = -5869553409971473557L;

    public static final int WIDTH1 = 1424;
    public static final int HEIGHT1 = 900;

    private final List<ControllerEditPanel> pnlList;
    private final List<JSlider> pnlListDimmer;
    private final MidiHandler midiHandler;
    private final JTabbedPane tabbedPane;
    private final ControllerEditPanel[] controllerEditPanels;
    private int activeIndex = -1;
    private final JSlider masterDimmer;
    private final Dmx dmx = Dmx.getInstance();
    protected JButton btnSave;

    private final int[] masterDimmerChannels;
    final StateRepository stateRepository = StateRepository.getInstance();

    public ControllerMainPanel() {

        pnlList = new ArrayList<>();
        pnlListDimmer = new ArrayList<>();
        midiHandler = MidiHandler.getInstance(this);
        tabbedPane = new JTabbedPane(JTabbedPane.RIGHT);
        tabbedPane.setBounds(0, 0, WIDTH1 + 200, HEIGHT1-300);
        cleanMatrix();
        add(tabbedPane);
        controllerEditPanels = new ControllerEditPanel[8];
        for (int i=0; i<8; i++) {
            final ControllerEditPanel editPanel = buildPanel(i+1);
            controllerEditPanels[i] = editPanel;
            editPanel.setBounds(0, 0, WIDTH1 + 200, HEIGHT1-400);
            tabbedPane.addTab(editPanel.getName(), editPanel);
            pnlList.add(editPanel);
        }

        tabbedPane.addChangeListener(this);
        this.setBounds(0, 0, WIDTH1 + 200, HEIGHT1);
        this.setLayout(null);
        selectPanel(7);

        final JPanel panelDimmers = new JPanel();
        panelDimmers.setLayout(null);
     //   panelDimmers.setOpaque(true);
     //   panelDimmers.setBackground(Color.blue);
        panelDimmers.setBounds(0, HEIGHT1-300, WIDTH1 + 200, 400);

        for (int i=0; i<9; i++) {
            final JSlider slider = new JSlider();
            slider.setOrientation(JSlider.VERTICAL);
            slider.setBounds(i * 110, 10, 100, 380);
            slider.setMinimum(0);
            slider.setMaximum(255);
            panelDimmers.add(slider);
            pnlListDimmer.add(slider);
        }

        masterDimmer = pnlListDimmer.get(pnlListDimmer.size()-1);

        this.add(panelDimmers);

        final List<Integer> channels = new ArrayList<>();
        final ShowCollection showCollection = ShowCollection.getInstance();
        final List<QLCFixture> fixtureList = showCollection.getQlcModel().getFixtureList();
        for (QLCFixture fixture: fixtureList) {
            int dmxMasterDimmer = fixture.getDMXChannel(QLCFixture.ChannelType.MASTER_DIMMER);
            if (dmxMasterDimmer > 0 && "RGBW".equals(fixture.getFixtureModel().getType()))
                channels.add(dmxMasterDimmer);
        }
        masterDimmerChannels = new int[channels.size()];
        int i=0;
        for (int dmx: channels)
            masterDimmerChannels[i++]=dmx;


        btnSave = new JButton();
        btnSave.setText("Save");
        btnSave.setBounds(EFXMConfigureMainPanel.WIDTH1+ 20, 50, 120, 20);

        panelDimmers.add(btnSave);
        btnSave.addActionListener(this);

        masterDimmer.setValue(stateRepository.getRgbwMasterDimmer());
        adjustMasterDimmer();
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
    public void onSlide(final KeyData keyData) {
        double value = keyData.getSliderValue()*255.0;
        final JSlider slider = pnlListDimmer.get(keyData.getPosX());
        slider.setValue((int)value);
        if (slider==masterDimmer)
            adjustMasterDimmer();

        //System.out.println(value+"\t"+(keyData.getValue())+"\t"+((int)(keyData.getValue()*255.0)));
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

    private void adjustMasterDimmer(){
        for (int dmxMasterDimmer: masterDimmerChannels) {
            dmx.send(dmxMasterDimmer, masterDimmer.getValue());
        }
    }

    private void save(){

   /*     final List<Point> limitMasterDimmer = new ArrayList<>();
        for (int dmxMasterDimmer: masterDimmerChannels)
            limitMasterDimmer.add(Point.builder()
                            .canal(dmxMasterDimmer)
                            .dmx(masterDimmer.getValue())
                    .build());
        stateRepository.setLimitMasterDimmer(limitMasterDimmer);*/
        stateRepository.setRgbwMasterDimmer(masterDimmer.getValue());
        stateRepository.write();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        save();
    }
}