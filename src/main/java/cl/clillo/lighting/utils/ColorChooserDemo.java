package cl.clillo.lighting.utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ColorChooserDemo extends JPanel implements ChangeListener {

    private static final long serialVersionUID = 2066941021336318125L;

    private final JColorChooser tcc;
    private final JLabel banner;
    private int n = 1;

    public ColorChooserDemo() {
        super(new BorderLayout());

        banner = new JLabel("", JLabel.CENTER);
        banner.setForeground(Color.yellow);
        banner.setBackground(Color.blue);
        banner.setOpaque(true);
        banner.setFont(new Font("SansSerif", Font.BOLD, 24));
        banner.setPreferredSize(new Dimension(100, 65));

        JPanel bannerPanel = new JPanel(new BorderLayout());
        bannerPanel.add(banner, BorderLayout.CENTER);
        bannerPanel.setBorder(BorderFactory.createTitledBorder("Ejemplo del Color"));

        tcc = new JColorChooser(banner.getBackground());
        tcc.getSelectionModel().addChangeListener(this);
        tcc.setBorder(BorderFactory.createTitledBorder("Elija el Color"));
        tcc.setPreviewPanel(new JPanel());

        add(bannerPanel, BorderLayout.CENTER);
        add(tcc, BorderLayout.PAGE_END);
    }

    public void stateChanged(ChangeEvent e) {
        Color newColor = tcc.getColor();
        banner.setBackground(newColor);
        System.out.println("<color id=\""+n+"\"><r>"+newColor.getRed()+"</r><g>"+newColor.getGreen()+"</g><b>"+newColor.getBlue()+"</b></color>");
        n++;
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Elegir Color");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.addWindowListener(new WindowAdapter() {
        });

        JComponent newContentPane = new ColorChooserDemo();
        newContentPane.setOpaque(true);
        frame.setContentPane(newContentPane);

        frame.pack();
        frame.setVisible(true);

    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(ColorChooserDemo::createAndShowGUI);
    }
}