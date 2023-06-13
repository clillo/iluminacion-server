package cl.clillo.lighting.gui;

import javax.swing.*;
import java.awt.*;

public class PositionsView {
    private JPanel panel1;
    private JList list1;

    public void init(){

    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("App");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        if (screenSize.getWidth()<1920)
            frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        else
            frame.setLocationRelativeTo(null);

        PositionsView positionsView = new PositionsView();
        positionsView.init();
        frame.setContentPane(positionsView.panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
