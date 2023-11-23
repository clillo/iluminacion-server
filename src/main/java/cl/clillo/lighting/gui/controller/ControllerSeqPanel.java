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
public class ControllerSeqPanel extends JPanel {

    private final String fixtureGroupName;





    public ControllerSeqPanel(final String fixtureGroupName) {
        this.fixtureGroupName = fixtureGroupName;
        setLayout(null);
        final Label lblTittle = new Label("<html><p style='display: block; text-align: center;font-family:\"Tahoma, sans-serif;\" font-size:30px;'>"+fixtureGroupName+"</p></html>");
        lblTittle.setBounds(10, 10, 300, 40);
        this.add(lblTittle);

        this.setOpaque(true);

    }


}