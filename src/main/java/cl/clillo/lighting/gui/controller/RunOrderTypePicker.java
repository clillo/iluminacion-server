package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.model.QLCDirection;
import cl.clillo.lighting.model.QLCRunOrder;
import cl.clillo.lighting.model.Sequenceable;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.Vector;

public class RunOrderTypePicker extends JPanel implements ChangeListener {

    private final JRadioButton loopForward = new JRadioButton("Loop Forward");
    private final JRadioButton loopBackward = new JRadioButton("Loop Backward");
    private final JRadioButton pingPong = new JRadioButton("Ping Pong");
    private final JRadioButton random = new JRadioButton("Random");
    private final JRadioButton x1 = new JRadioButton("1x");
    private final JRadioButton x2 = new JRadioButton("2x");
    private final JRadioButton x3 = new JRadioButton("3x");
    private final JRadioButton x4 = new JRadioButton("4x");

    public RunOrderTypePicker() {
        setLayout(null);

        loopForward.setBounds(10, 10, 140, 30);
        loopBackward.setBounds(10, 40, 140, 30);
        pingPong.setBounds(10, 70, 100, 30);
        random.setBounds(10, 100, 100, 30);

        x1.setBounds(10, 150, 100, 20);
        x2.setBounds(50, 150, 100, 20);
        x3.setBounds(90, 150, 100, 20);
        x4.setBounds(130, 150, 100, 20);

        addToButtonGroup(this, loopForward, loopBackward, pingPong, random);
        addToButtonGroup(this, x1, x2, x3, x4);

        loopForward.setSelected(true);

    }

    private void addToButtonGroup(final JPanel panelDimmers, final JRadioButton... buttons) {
        javax.swing.ButtonGroup bg = new javax.swing.ButtonGroup();
        for (JRadioButton rb : buttons) {
            bg.add(rb);
            rb.addChangeListener(this);
            panelDimmers.add(rb);
        }

    }

    @Override
    public void stateChanged(ChangeEvent e) {

    }

    public void selectShow(final Sequenceable sequence) {
        switch (sequence.getRunOrder()) {
            case LOOP:
                switch (sequence.getDirection()) {
                    case FORWARD:
                        loopForward.setSelected(true);
                        break;
                    case BACKWARD:
                        loopBackward.setSelected(true);
                        break;
                }
                break;
            case PINGPONG:
                pingPong.setSelected(true);
                break;

            case RANDOM:
                random.setSelected(true);
                break;
        }

        switch (sequence.getSpeed()){
            case 1:
                x1.setSelected(true);
                break;
            case 2:
                x2.setSelected(true);
                break;
            case 3:
                x3.setSelected(true);
                break;
            case 4:
                x4.setSelected(true);
                break;
        }

    }

     public QLCRunOrder getRunOrderSelected(){
        if (loopForward.isSelected() || loopBackward.isSelected())
            return QLCRunOrder.LOOP;

        if (random.isSelected())
            return QLCRunOrder.RANDOM;

        return QLCRunOrder.PINGPONG;
    }

    public QLCDirection getDirectionSelected(){
        if (loopForward.isSelected())
            return QLCDirection.FORWARD;

        return QLCDirection.BACKWARD;
    }

    public int getSpeedSelected(){
        if (x1.isSelected())
            return 1;
        if (x4.isSelected())
            return 4;
        if (x3.isSelected())
            return 3;

        return 4;

    }
}
