package cl.clillo.lighting.gui.controller;

import lombok.extern.log4j.Log4j2;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class ControllerSeqPanel extends JPanel {

    private static final long serialVersionUID = -5869553409971473557L;

    public ControllerSeqPanel() {
        setLayout(null);

        this.setOpaque(true);
        this.setBackground(Color.black);
    }

}