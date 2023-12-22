package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.executor.IOS2LEventListener;
import cl.clillo.lighting.model.QLCEfx;
import cl.clillo.lighting.model.QLCScene;
import cl.clillo.lighting.model.QLCSequence;
import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.model.ShowCollection;
import lombok.extern.log4j.Log4j2;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@Log4j2
public class ControllerSeqPanel extends JPanel implements ActionListener, ChangeListener {

    private final RunOrderTypePicker runOrderTypePicker = new RunOrderTypePicker();

    private final JRadioButton universalTime = new JRadioButton("Universal Time");
    private final JRadioButton beatD4 = new JRadioButton("Beat / 4");
    private final JRadioButton beatD2 = new JRadioButton("Beat / 2");
    private final JRadioButton beatX1 = new JRadioButton("Beat x 1");
    private final JRadioButton beatX2 = new JRadioButton("Beat x 2");
    private final JRadioButton beatX4 = new JRadioButton("Beat x 4");
    private final JRadioButton beatX8 = new JRadioButton("Beat x 8");
    private final JRadioButton beatX16 = new JRadioButton("Beat x 16");


    private ChangeDirectionRunOrderListener changeDirectionRunOrderListener;
    private final JButton btnSave = new JButton();
    private final JButton btnEdit = new JButton();

    private Show showSelected;

    public ControllerSeqPanel(final String fixtureGroupName) {
        setLayout(null);
        final Label lblTittle = new Label(fixtureGroupName);
        lblTittle.setBounds(10, 10, 60, 40);
        this.add(lblTittle);

        runOrderTypePicker.setBounds(10,70,140,130);
        this.add(runOrderTypePicker);

        universalTime.setBounds(10,250,140,30);
        beatD4.setBounds(10,280,140,30);
        beatD2.setBounds(10,310,140,30);
        beatX1.setBounds(10,340,140,30);
        beatX2.setBounds(10,370,140,30);
        beatX4.setBounds(10,400,140,30);
        beatX8.setBounds(10,430,140,30);
        beatX16.setBounds(10,460,140,30);

        addToButtonGroup(this, universalTime, beatD4, beatD2, beatX1, beatX2, beatX4, beatX8, beatX16);

        universalTime.setSelected(true);
        runOrderTypePicker.setVisible(false);

        btnSave.setText("Save");
        btnSave.setBounds(10, 500, 120, 20);
        btnEdit.setText("Edit");
        btnEdit.setBounds(10, 540, 120, 20);
        this.add(btnSave);
        this.add(btnEdit);
        btnSave.addActionListener(this);
        btnEdit.addActionListener(this);

    }

    public void setChangeDirectionRunOrderListener(ChangeDirectionRunOrderListener changeDirectionRunOrderListener) {
        this.changeDirectionRunOrderListener = changeDirectionRunOrderListener;
    }

    public void setShowSelected(final Show showSelected) {
        this.showSelected = showSelected;

        try{
            runOrderTypePicker.setVisible(true);
            runOrderTypePicker.selectShow(showSelected);
        }catch (Exception e) {
            runOrderTypePicker.setVisible(false);
        }
    }

    private void addToButtonGroup(final JPanel panelDimmers, final JRadioButton... buttons){
        javax.swing.ButtonGroup bg = new javax.swing.ButtonGroup();
        for (JRadioButton rb: buttons) {
            bg.add(rb);
            rb.addChangeListener(this);
            panelDimmers.add(rb);
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(btnSave))
            save();

        if (e.getSource().equals(btnEdit)) {
            editScene();
            editEFX();
        }

    }

    private void save(){
        if (showSelected==null || !(showSelected.getFunction() instanceof QLCSequence))
            return;

        final QLCSequence sequence = showSelected.getFunction();
        final String dir = ShowCollection.getInstance().getDirectory(sequence);
        sequence.writeToConfigFile(dir);
    }

    private void editScene(){
        if (showSelected==null ||  !(showSelected.getFunction() instanceof QLCScene))
            return;

        final QLCScene scene = showSelected.getFunction();
        if (scene.getQlcEfxScene()==null)
            return;

        EFXMConfigureJFrame efxmConfigureJFrame = new EFXMConfigureJFrame(showSelected);
        efxmConfigureJFrame.start();
    }


    private void editEFX(){
        if (showSelected==null ||  !(showSelected.getFunction() instanceof QLCEfx))
            return;

        EFXMConfigureJFrame efxmConfigureJFrame = new EFXMConfigureJFrame(showSelected);
        efxmConfigureJFrame.start();
    }


    @Override
    public void stateChanged(ChangeEvent e) {
        if (changeDirectionRunOrderListener!=null)
            changeDirectionRunOrderListener.change(runOrderTypePicker.getRunOrderSelected(),
                    runOrderTypePicker.getDirectionSelected(), getType(), runOrderTypePicker.getSpeedSelected());

    }

    private IOS2LEventListener.Type getType(){
        if (universalTime.isSelected())
            return IOS2LEventListener.Type.UNIVERSAL;

        if (beatX1.isSelected())
            return IOS2LEventListener.Type.BEAT_X_1;

        if (beatX2.isSelected())
            return IOS2LEventListener.Type.BEAT_X_2;

        if (beatX4.isSelected())
            return IOS2LEventListener.Type.BEAT_X_4;

        if (beatX8.isSelected())
            return IOS2LEventListener.Type.BEAT_X_8;

        if (beatX16.isSelected())
            return IOS2LEventListener.Type.BEAT_X_16;

        if (beatD2.isSelected())
            return IOS2LEventListener.Type.BEAT_D_2;

        if (beatD4.isSelected())
            return IOS2LEventListener.Type.BEAT_D_4;

        return null;
    }


}