package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.model.QLCDirection;
import cl.clillo.lighting.model.QLCRunOrder;
import lombok.extern.log4j.Log4j2;

import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Label;

@Log4j2
public class ControllerSeqPanel extends JPanel implements ChangeListener {

    private final String fixtureGroupName;

    private final JRadioButton loopForward = new JRadioButton("Loop Forward");
    private final JRadioButton loopBackward = new JRadioButton("Loop Backward");
    private final JRadioButton pingPong = new JRadioButton("Ping Pong");
    private final JRadioButton random = new JRadioButton("Random");

    private ChangeDirectionRunOrderListener changeDirectionRunOrderListener;

    public ControllerSeqPanel(final String fixtureGroupName) {
        this.fixtureGroupName = fixtureGroupName;
        setLayout(null);
        final Label lblTittle = new Label("<html><p style='display: block; text-align: center;font-family:\"Tahoma, sans-serif;\" font-size:30px;'>"+fixtureGroupName+"</p></html>");
        lblTittle.setBounds(10, 10, 300, 40);
        this.add(lblTittle);

        this.setOpaque(true);

        loopForward.setBounds(10,100,140,30);
        loopBackward.setBounds(10,130,140,30);
        pingPong.setBounds(10,160,100,30);
        random.setBounds(10,190,100,30);
        javax.swing.ButtonGroup bg = new javax.swing.ButtonGroup();
        bg.add(loopForward);
        bg.add(loopBackward);
        bg.add(pingPong);
        bg.add(random);
        loopForward.addChangeListener(this);
        loopBackward.addChangeListener(this);
        pingPong.addChangeListener(this);
        random.addChangeListener(this);
        this.add(loopForward);
        this.add(loopBackward);
        this.add(pingPong);
        this.add(random);

        loopForward.setSelected(true);
    }

    public void setChangeDirectionRunOrderListener(ChangeDirectionRunOrderListener changeDirectionRunOrderListener) {
        this.changeDirectionRunOrderListener = changeDirectionRunOrderListener;
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

    @Override
    public void stateChanged(ChangeEvent e) {
        if (changeDirectionRunOrderListener!=null)
            changeDirectionRunOrderListener.change();;
    }
}