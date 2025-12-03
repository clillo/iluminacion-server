package cl.clillo.utilities;

import cl.clillo.lighting.gui.controller.BeeEyePanel;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Random;

public class BeeEyeDemo {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

			final JFrame frame = new JFrame("BeeEye Demo");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setLayout(new BorderLayout());

			JPanel strip = new JPanel(new GridLayout(1, 4, 8, 0));
			BeeEyePanel p0 = new BeeEyePanel();
			BeeEyePanel p1 = new BeeEyePanel();
			BeeEyePanel p2 = new BeeEyePanel();
			BeeEyePanel p3 = new BeeEyePanel();
			p0.setName("BeeEye 1");
			p1.setName("BeeEye 2");
			p2.setName("BeeEye 3");
			p3.setName("BeeEye 4");
			strip.add(p0);
			strip.add(p1);
			strip.add(p2);
			strip.add(p3);
			frame.add(strip, BorderLayout.CENTER);

		/*	applyExampleColors(p0, 7L);
			applyExampleColors(p1, 11L);
			applyExampleColors(p2, 13L);
			applyExampleColors(p3, 17L);*/

			// Base channel por fixture: 21, 71, 121, 171
			p0.setBaseChannel(21);
			p1.setBaseChannel(50 + 21 - 1);
			p2.setBaseChannel(100 + 21 - 1);
			p3.setBaseChannel(150 + 21 - 1);

			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}

	private static void applyExampleColors(BeeEyePanel panel, long seed) {
		// Colores de ejemplo: anillo con tonos variados y centro blanco
		Random rnd = new Random(seed);
		for (int i = 0; i < 6; i++) {
			int r = 80 + rnd.nextInt(176);
			int g = 80 + rnd.nextInt(176);
			int b = 80 + rnd.nextInt(176);
			int w = 40;
			panel.setLedColor(i, r, g, b, w);
		}
		panel.setLedColor(6, 0, 0, 0, 255); // centro blanco
	}
}


