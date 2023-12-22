package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.model.QLCDirection;
import cl.clillo.lighting.model.QLCRunOrder;
import cl.clillo.lighting.model.Sequenceable;
import cl.clillo.lighting.model.Show;

import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class RunOrderTypePicker extends JPanel {

    private final JRadioButton loopForward = new JRadioButton("Loop Forward");
    private final JRadioButton loopBackward = new JRadioButton("Loop Backward");
    private final JRadioButton pingPong = new JRadioButton("Ping Pong");
    private final JRadioButton random = new JRadioButton("Random");
    private final JRadioButton x1 = new JRadioButton("1x");
    private final JRadioButton x2 = new JRadioButton("2x");
    private final JRadioButton x3 = new JRadioButton("3x");
    private final JRadioButton x4 = new JRadioButton("4x");
    private JRadioButton previousRadioSelected = null;
    private Show showSelected = null;

    public RunOrderTypePicker() {
        setLayout(null);

        loopForward.setBounds(10, 10, 140, 30);
        loopBackward.setBounds(10, 40, 140, 30);
        pingPong.setBounds(10, 70, 100, 30);
        random.setBounds(10, 100, 100, 30);

        x1.setBounds(10, 150, 50, 20);
        x2.setBounds(50, 150, 50, 20);
        x3.setBounds(90, 150, 50, 20);
        x4.setBounds(130, 150, 50, 20);

        addToButtonGroup(this, loopForward, loopBackward, pingPong, random);
        addToButtonGroup(this, x1, x2, x3, x4);

        loopForward.setSelected(true);

    }

    private void addToButtonGroup(final JPanel panelDimmers, final JRadioButton... buttons) {
        final javax.swing.ButtonGroup bg = new javax.swing.ButtonGroup();
        for (JRadioButton rb : buttons) {
            bg.add(rb);
            rb.addChangeListener(e ->{
                if (!rb.isSelected())
                    return;
                if (previousRadioSelected!=null && previousRadioSelected==rb)
                    return;
                previousRadioSelected = rb;
                selectRadio();
            } );
            panelDimmers.add(rb);
        }

    }

    private void selectRadio(){
        int speed = getSpeedSelected();
        if (speed == -1)
            return;
        if (showSelected==null)
            return;

        ((Sequenceable)showSelected.getFunction()).setSpeed(speed);
    }

    public void selectShow(final Show show) {
        this.showSelected = show;
        final Sequenceable sequence = show.getFunction();

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
                previousRadioSelected=x1;
                x1.setSelected(true);
                break;
            case 2:
                previousRadioSelected=x2;
                x2.setSelected(true);
                break;
            case 3:
                previousRadioSelected=x3;
                x3.setSelected(true);
                break;
            case 4:
                previousRadioSelected=x4;
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
        if (x2.isSelected())
            return 2;
        if (x3.isSelected())
            return 3;
        if (x4.isSelected())
            return 4;

        return -1;

    }
}
