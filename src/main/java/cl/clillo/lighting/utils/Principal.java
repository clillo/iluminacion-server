package cl.clillo.lighting.utils;

import javax.swing.*;
import java.awt.*;

public class Principal {

    private static JFrame frame;

    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        frame = new JFrame();
        frame.validate();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

      //  if (screenSize.getWidth()<1920)
     //       frame.setExtendedState(Frame.MAXIMIZED_BOTH);
    //    else
            frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }

    public static JFrame getFrame() {
        return frame;
    }

    public static void main(String[] args) throws Exception {

    }
}
